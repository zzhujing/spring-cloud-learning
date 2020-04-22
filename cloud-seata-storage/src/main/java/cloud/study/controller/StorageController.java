package cloud.study.controller;


import cloud.study.domain.Storage;
import cloud.study.service.StorageService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StorageController {

    private final StorageService storageService;
    /**
     * 减少库存
     *
     * @param productId 商品id
     * @param count     购买数量
     */
    @PutMapping("/reduceStorage/{productId}/{count}")
    public void reduceStorage(@PathVariable Long productId, @PathVariable Integer count){
        Storage storage = storageService.getOne(Wrappers.<Storage>lambdaQuery().eq(Storage::getProductId, productId));
        if (storage == null) throw new RuntimeException("库存不存在");
        if (storage.getResidue() < count) throw new RuntimeException("库存不足");
        storage.setResidue(storage.getResidue() - count);
        storage.setUsed(storage.getUsed() + count);
        if (!storageService.updateById(storage)) throw new RuntimeException("扣取库存失败");
    }

}
