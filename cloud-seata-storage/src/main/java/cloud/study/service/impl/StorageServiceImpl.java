package cloud.study.service.impl;

import cloud.study.domain.Storage;
import cloud.study.mapper.StorageMapper;
import cloud.study.service.StorageService;
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
public class StorageServiceImpl extends ServiceImpl<StorageMapper, Storage> implements StorageService {

}
