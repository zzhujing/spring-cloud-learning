package cloud.study;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
@RestController
@RefreshScope
public class CloudProviderPaymentZkApplication {
    @Value("${server.port}")
    private String port;
    @Value("${config.info}")
    private String info;

    public static void main(String[] args) {
        SpringApplication.run(CloudProviderPaymentZkApplication.class, args);
    }

    @GetMapping("echo")
    public String echo() {
        return "Hello," + port;
    }

    @GetMapping("/config/info")
    public String getConfigInfo() {
        return info;
    }
    @GetMapping("/payment/echo1")
    public String echo1() throws InterruptedException {
        return "Hello, cloud-provider-payment services " + port;
    }

}
