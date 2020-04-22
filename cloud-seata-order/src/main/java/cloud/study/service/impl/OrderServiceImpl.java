package cloud.study.service.impl;

import cloud.study.domain.Order;
import cloud.study.mapper.OrderMapper;
import cloud.study.service.OrderService;
import cloud.study.service.clients.AccountClient;
import cloud.study.service.clients.StorageClient;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author hujing
 * @since 2020-04-04
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final StorageClient storageClient;
    private final AccountClient accountClient;

    /**
     * 下单
     *
     * @param order 订单相关信息
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void placeAnOrder(Order order) {

        //扣除库存
        storageClient.reduceStorage(order.getProductId(), order.getCount());
        //扣余额
        accountClient.reduceAccount(order.getUserId(), 10);
        //创建订单
        createOrder(order);
    }

    private void createOrder(Order order) {
        if (!save(order)) {
            log.error("添加订单失败");
            throw new RuntimeException();
        }
        if (order.getUserId()!=3) throw new RuntimeException();
    }
}
