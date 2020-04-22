package cloud.study.controller;


import cloud.study.domain.Payment;
import cloud.study.result.CommonResult;
import cloud.study.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author hujing
 * @since 2020-03-23
 */
@RestController
@RequestMapping("/payment/")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaymentController {

    private final Environment environment;

    private final PaymentService paymentService;

    private final DiscoveryClient discoveryClient;

    @PostMapping(path = "create", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResult create(Payment payment) {
        return paymentService.save(payment) ? CommonResult.ok("ok ： " + environment.getProperty("server.port")) : CommonResult.fail("fail", 444);
    }

    @GetMapping("get/{id}")
    public CommonResult getById(@PathVariable Long id) {
        return CommonResult.ok(paymentService.getById(id));
    }

    @GetMapping("discovery")
    public Object discovery(){
        discoveryClient.getServices().forEach(System.out::println);
        discoveryClient.getInstances("CLOUD-PROVIDER-PAYMENT").forEach(System.out::println);
        return discoveryClient;
    }
}
