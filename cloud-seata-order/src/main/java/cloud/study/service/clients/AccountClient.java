package cloud.study.service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * TODO
 **/
@FeignClient("seata-account")
public interface AccountClient {

    /**
     * 扣取余额
     * @param userId
     * @param money
     */
    @PutMapping("/reduceAccount/{userId}/{money}")
    void reduceAccount(@PathVariable("userId") Long userId, @PathVariable("money") Integer money);
}
