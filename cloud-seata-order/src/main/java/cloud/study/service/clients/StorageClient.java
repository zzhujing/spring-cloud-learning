package cloud.study.service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient("seata-storage")
public interface StorageClient {

    /**
     * 减少库存
     *
     * @param productId 商品id
     * @param count     购买数量
     */
    @PutMapping("/reduceStorage/{productId}/{count}")
    void reduceStorage(@PathVariable("productId") Long productId, @PathVariable("count") Integer count);
}
