package cloud.study.service.impl;

import cloud.study.domain.Account;
import cloud.study.mapper.AccountMapper;
import cloud.study.service.AccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hujing
 * @since 2020-04-04
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

}
