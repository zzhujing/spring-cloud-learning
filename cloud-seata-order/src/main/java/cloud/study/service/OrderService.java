package cloud.study.service;

import cloud.study.domain.Order;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author hujing
 * @since 2020-04-04
 */
public interface OrderService extends IService<Order> {


    void placeAnOrder(Order order);

}
