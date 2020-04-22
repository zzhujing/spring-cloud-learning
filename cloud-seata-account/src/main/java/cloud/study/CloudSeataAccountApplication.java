package cloud.study;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("cloud.study.mapper")
public class CloudSeataAccountApplication
{
    public static void main(String[] args) {
        SpringApplication.run(CloudSeataAccountApplication.class, args);
    }
}
