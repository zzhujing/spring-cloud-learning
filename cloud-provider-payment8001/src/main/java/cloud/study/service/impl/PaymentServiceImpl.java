package cloud.study.service.impl;

import cloud.study.domain.Payment;
import cloud.study.mapper.PaymentMapper;
import cloud.study.service.PaymentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hujing
 * @since 2020-03-23
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

}
