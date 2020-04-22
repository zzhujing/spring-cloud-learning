package cloud.study;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class NacosConsumerApplication {

    @Autowired
    private LoadBalancerClient client;
    @Autowired
    private RestTemplate restTemplate;



    public static void main(String[] args) {
        SpringApplication.run(NacosConsumerApplication.class, args);
    }

    @GetMapping("/consumer")
    public String consumer() {
        ServiceInstance instance = client.choose("nacos-provider-payment");
        String url = String.format("http://%s:%s/echo", instance.getHost(), instance.getPort());
        return restTemplate.getForObject(url, String.class);
    }


    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
