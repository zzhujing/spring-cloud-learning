package cloud.study.controller;


import cloud.study.domain.Account;
import cloud.study.service.AccountService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.websocket.AsyncChannelWrapperSecure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author hujing
 * @since 2020-04-04
 */
@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AccountController {

    private final AccountService accountService;

    /**
     * 扣取余额
     *
     * @param userId
     * @param money
     */
    @PutMapping("/reduceAccount/{userId}/{money}")
    @Transactional(rollbackFor = Exception.class)
    public void reduceAccount(@PathVariable Long userId, @PathVariable Integer money) {
        Account account = accountService.getOne(Wrappers.<Account>lambdaQuery().eq(Account::getUserId, userId));
        if (account == null) throw new RuntimeException("用户不存在");
        if (account.getResidue() < money) throw new RuntimeException("余额不足");
        account.setResidue(account.getResidue() - money);
        account.setUsed(account.getUsed() + money);
        if (!accountService.updateById(account)) throw new RuntimeException("扣取余额失败");
    }

}
