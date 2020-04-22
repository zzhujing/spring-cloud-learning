package cloud.study.controller;


import cloud.study.domain.Order;
import cloud.study.result.CommonResult;
import cloud.study.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author hujing
 * @since 2020-04-04
 */
@RestController
@RequestMapping("/order")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderController {


    private final OrderService orderService;

    @PostMapping("/placeAnOrder")
    public CommonResult placeAnOrder(@RequestBody Order order){
        orderService.placeAnOrder(order);
        return CommonResult.ok("success");
    }


}
