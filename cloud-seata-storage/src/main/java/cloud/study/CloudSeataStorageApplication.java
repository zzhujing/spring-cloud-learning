package cloud.study;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * TODO
 **/
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("cloud.study.mapper")
public class CloudSeataStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudSeataStorageApplication.class, args);
    }
}
