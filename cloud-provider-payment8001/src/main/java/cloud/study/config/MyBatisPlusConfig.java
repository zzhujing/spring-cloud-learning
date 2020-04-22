package cloud.study.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * Mybatis Plus配置
 **/
@MapperScan("cloud.study.mapper")
@Configuration
public class MyBatisPlusConfig {

}
