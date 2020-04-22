package cloud.study;

import cn.hutool.core.util.IdUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * Payment 启动类
 **/
//@SpringBootApplication
//@EnableDiscoveryClient
//@EnableCircuitBreaker
@SpringCloudApplication
@RestController
@EnableHystrixDashboard
@RequestMapping("/payment")
public class CloudProviderPaymentApplication {
    @Autowired
    private Environment environment;
    @Autowired
    private HttpServletRequest req;

    public static void main(String[] args) {
        SpringApplication.run(CloudProviderPaymentApplication.class, args);
    }

    @GetMapping("/echo/{id}")
    public String echo(@PathVariable Integer id) throws InterruptedException {
        if (id < 0) {
            throw new RuntimeException();
        }
        String uuid = IdUtil.simpleUUID();
        Thread.sleep(2000);
        return "Hello , visited success : " + uuid;
    }

    @GetMapping("/echo1")
    public String echo1() throws InterruptedException {
        return "Hello, cloud-provider-payment services " + environment.getProperty("server.port");
    }

    @GetMapping("/echoWithHystrix")
    @HystrixCommand(fallbackMethod = "echoFallback", commandProperties = {
            /**
             * {@link com.netflix.hystrix.HystrixCommandProperties} 设置HystrixProperty
             */
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000") //设置熔断超时时间
    })
    public String echoWithHystrix() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "执行成功O(∩_∩)O哈哈~";
    }

    @GetMapping("/fallback")
    public String fallback(@RequestHeader("ex-header") String exHeader,
                           @RequestHeader("ex-msg") String exMsg,
                           @RequestHeader("ex-root-type") String exRootType,
                           @RequestHeader("ex-type") String exType
    ) {
        return "ex-header:" + exHeader +
                " : ex-msg:" + exMsg +
                " : ex-root-type:" + exRootType +
                " : ex-type:" + exType;
    }

    /**
     * Hystrix fallBack method
     * 1.超时
     * 2.异常报错
     */
    public String echoFallback() {
        return "服务器繁忙o(╥﹏╥)o";
    }
}
