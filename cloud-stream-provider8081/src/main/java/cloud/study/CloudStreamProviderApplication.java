package cloud.study;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * TODO
 **/
@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class CloudStreamProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudStreamProviderApplication.class, args);
    }

    @Autowired
    private MessageProvider messageProvider;

    @GetMapping("send")
    public String send() {
        messageProvider.sendMsg();
        return UUID.randomUUID().toString();
    }
}
