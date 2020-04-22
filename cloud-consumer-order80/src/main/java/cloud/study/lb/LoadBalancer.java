package cloud.study.lb;

import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

/**
 * 自定义负载均衡抽象
 **/
public interface LoadBalancer {

    ServiceInstance getServiceInstance(List<ServiceInstance> instances);
}
