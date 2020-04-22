package cloud.study.client;

import feign.hystrix.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Payment服务OpenFeign Client
 **/
@FeignClient(value = "cloud-provider-payment" /*,fallback = PaymentClient.PaymentClientFallback.class*/)
public interface PaymentClient {

    @GetMapping("echo/{id}")
    String echo(@PathVariable("id") Integer id);


//    @Component
//    class PaymentClientFallback implements PaymentClient{
//        @Override
//        public String echo(Integer id) {
//            return "FeignClient FallBack";
//        }
//    }
//
//    @Component
//    class PaymentFallbackFactory implements FallbackFactory<PaymentClient> {
//
//        @Override
//        public PaymentClient create(Throwable cause) {
//            return () -> {
//                System.out.println(cause.getMessage());
//                return "fallbackFactory";
//            };
//        }
//    }
}
