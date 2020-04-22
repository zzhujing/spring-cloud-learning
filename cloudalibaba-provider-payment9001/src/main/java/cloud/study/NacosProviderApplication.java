package cloud.study;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.ServletRequestHandledEvent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
//@EnableDiscoveryClient
@RestController
public class NacosProviderApplication {

    @Value("${server.port}")
    private String port;
    private AtomicInteger incr = new AtomicInteger();

    public static void main(String[] args) {
        SpringApplication.run(NacosProviderApplication.class, args);
    }

    @GetMapping("/echo/a")
    public String echoA() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(500);
        System.out.println("echo-A -> " + incr.incrementAndGet());
        return " hi,NacosProvider-A" + port;
    }

    @GetMapping("/echo/b")
    public String echoB() {
        return " hi,NacosProvider-B" + port;
    }

    @Component
    static class ServletRequestListener implements ApplicationListener<ServletRequestHandledEvent> {
        @Override
        public void onApplicationEvent(ServletRequestHandledEvent event) {
            System.out.println(event.getMethod() + ": url : " + event.getRequestUrl() + " spend : " + event.getProcessingTimeMillis());
        }
    }

    @PostMapping("/echo/hotKey")
    @SentinelResource(value = "hotKey",blockHandler = "hotKeyBlocked",fallback = "fallback")
    //HotKey不支持复杂类型参数
    public String testEchoHotKey(@RequestBody HotKeyParam hotKeyParam) {
        if (hotKeyParam.getValue().equals("k1")) {
            System.out.println("error");
            throw new RuntimeException();
        }
        return "blocked";
    }

    public String hotKeyBlocked(HotKeyParam hotKeyParam, BlockException exception){
        return hotKeyParam+"blocked-> "+exception.getMessage();
    }

    public String fallback(HotKeyParam hotKeyParam, Throwable ex){
        return hotKeyParam+"fallback->" + ex.getMessage();
    }


    static class HotKeyParam{
        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Component
    static class CustomUrlBlockedHandler implements BlockExceptionHandler {
        @Override
        public void handle(HttpServletRequest httpServletRequest, HttpServletResponse response, BlockException e) throws Exception {
            response.setContentType("application/json;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            response.setStatus(500);
            response.getWriter().write("custom,blocked!!");
        }
    }
}
