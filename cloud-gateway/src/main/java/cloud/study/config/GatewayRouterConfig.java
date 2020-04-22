package cloud.study.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * 自定义Gateway 路由规则
 **/
@Configuration
public class GatewayRouterConfig
{

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r->r.path("/bl/**").uri("https://www.bilibili.com/"))
                .route("cloud-provider-payment",r->r.path("/payment/**")
                        .filters(f->f
                                .circuitBreaker(c->c.setName("fallback").setFallbackUri("forward:/payment/fallback"))
                                .modifyResponseBody(String.class,String.class,(exchange,s)->Mono.just(s.toUpperCase()))
                        )
                        .uri("lb://cloud-provider-payment"))
//                .route("fallback",r->r.path("/payment/fallback")
//                        .filters(f->f.fallbackHeaders(c->{
//                            c.setCauseExceptionMessageHeaderName("ex-header");
//                            c.setExecutionExceptionMessageHeaderName("ex-msg");
//                            c.setRootCauseExceptionTypeHeaderName("ex-root-type");
//                            c.setExecutionExceptionTypeHeaderName("ex-type");
//                        }))
//                        .uri("lb://cloud-provider-payment"))
                .build();
    }
}
