package ribbon;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//该扩展配置文件不能被SpringBoot 扫描到，不然会被Spring上下文扫描到，导致所有的实例名(当前配置对应的实例和其他所有实例都包括)
// 都会被该默认配置覆盖。
@Configuration
public class CustomRibbonConfig {
    @Bean
    public IRule rule(){
        return new RandomRule();
    }
}
