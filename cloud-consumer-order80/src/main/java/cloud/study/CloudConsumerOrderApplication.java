package cloud.study;

import cloud.study.client.PaymentClient;
import cloud.study.domain.Payment;
import cloud.study.lb.LoadBalancer;
import cloud.study.result.CommonResult;
import cn.hutool.core.util.IdUtil;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 订单消费主程序类
 **/
@SpringCloudApplication
@RestController
@RequestMapping("/consumer/")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@EnableFeignClients
public class CloudConsumerOrderApplication {

    private final RestTemplate restTemplate;
    public static final String PAYMENT_URL = "http://CLOUD-PROVIDER-PAYMENT";
    private final PaymentClient paymentClient;
    private final LoadBalancer loadBalancer;
    private final DiscoveryClient discoveryClient;

    public static void main(String[] args) {
        SpringApplication.run(CloudConsumerOrderApplication.class, args);
    }

    @GetMapping("create")
    public CommonResult create(Payment payment) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("serial", payment.getSerial());
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(paramMap, headers);
        return restTemplate.exchange(PAYMENT_URL + "/payment/create", HttpMethod.POST, httpEntity, CommonResult.class).getBody();
    }

    @GetMapping("get/{id}")
    public CommonResult getById(@PathVariable Long id) {
        return restTemplate.getForObject(PAYMENT_URL + "/payment/get/" + id, CommonResult.class);
    }

    @GetMapping("/echo1/{id}")
    @HystrixCommand(defaultFallback = "defaultFallback", commandProperties = {
        //开启断路器模式，默认true
        @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),
        //请求到达多少次才会触发熔断，默认20次
        @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
        //熔断触发时间窗口
        @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "30000"),
        //百分之10就进行熔断
        @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "10")
    })
    public String echo1(@PathVariable Integer id) {
        // use restTemplate way
        //return restTemplate.getForObject("http://payment8004/echo", String.class);
        return paymentClient.echo(id);
    }

    @GetMapping("/echo2")
    public String echo2() {
        List<ServiceInstance> instances = discoveryClient.getInstances("cloud-provider-payment");
        ServiceInstance serviceInstance = loadBalancer.getServiceInstance(instances);
        return restTemplate.getForObject(serviceInstance.getUri() + "/echo", String.class);
    }

    private String defaultFallback() {
        return "Global Fallback Method";
    }

}
