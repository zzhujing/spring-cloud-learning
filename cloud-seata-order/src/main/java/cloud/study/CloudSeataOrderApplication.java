package cloud.study;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("cloud.study.mapper")
public class CloudSeataOrderApplication
{
    public static void main(String[] args) {
        SpringApplication.run(CloudSeataOrderApplication.class, args);
    }
}
