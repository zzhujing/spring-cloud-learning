package cloud.study.lb;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义轮询负载均衡算法
 **/
@Component
public class MyLbStrategy implements LoadBalancer {

    private AtomicInteger index = new AtomicInteger();

    @Override
    public ServiceInstance getServiceInstance(List<ServiceInstance> instances) {
        int nextIndex = index.incrementAndGet() % instances.size();
        return instances.get(nextIndex);
    }
}
