## SpringCloud

#### CAP

- Consistency ： 一致性
- Availabiity : 可用性
- Parition Tolerance ： 分区容错性

一般是CP或者AP，在分布式环境下必须实现P，如下注册中心，Eureka就是AP，ZK和Consul是CP。

一般AP最后会通过BASE理论来完成数据最终一致性。比如底层NOSQL的数据同步等。

### 1、服务注册与发现

#### Eureka

##### server

###### 环境搭建

- 父pom

```xml
            <!--spring boot 2.2.2-->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>2.2.2.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!--spring cloud Hoxton.SR1-->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>Hoxton.SR1</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
```

- 添加maven依赖

```xml
        
       <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
```

- 添加配置

```yaml
server:
  port: 7001
eureka:
  instance:
    hostname: eureka7001.com #服务端实例名
  client:
    register-with-eureka: false #不注册自己
    fetch-registry: false #不需要拉取服务
    service-url:
      defaultZone: http://eureka7002.com:7002/eureka/ #集群配置，配置另外一server节点的地址
  server:
    enable-self-preservation: false #关闭自我保护机制，开发过程请关闭！生产打开保证高可用
    eviction-interval-timer-in-ms: 2000 #清除非正常服务周期

```

- 添加注解`@EnableEurekaServer`，启动即可

##### client

- maven依赖

```xml
       <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
```

- 添加注解`@EnableEurekaClient/@EnableDiscoveryClient`

- 添加配置

```yaml
      
eureka:
  client:
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7002.com:7002/eureka/
    register-with-eureka: true #注册自己
    fetch-registry: true #拉取服务信息，集群必须配置才能配置ribbon实现负载均衡
  instance:
    prefer-ip-address: true #使用ip注册
    lease-expiration-duration-in-seconds: 2 #最短超时周期，超过2s则超时
    lease-renewal-interval-in-seconds: 1 #最短心跳发送周期
```

#### Zookeeper

##### server

使用docker启动zk

```shell
d run --name zk01 -d  -p2181:2181 -p2888:28888 -p 3888:3888 zookeeper
```

##### client

- 添加maven依赖

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
        </dependency>
```

- 配置yaml

```yaml
spring:
  application:
    name: cloud-provider-payment
  cloud:
    zookeeper:
      connect-string: localhost:2181 #zk server地址
```

- 添加注解`@EnableDiscoveryClient`

#### Consul

##### Server

使用docker启动

```shell
d run --name consul -d -p8500:8500 -p8600:8600/udp consul
```

##### Client

- 添加maven依赖

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-consul-discovery</artifactId>
        </dependency>
```

- 添加配置

```yaml
spring:
  application:
    name: cloud-consumer-order
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        prefer-ip-address: true
```

- 添加注解`@EnableDiscoveryClient`

#### Nacos

##### Server

###### 	使用官方的nacos包进行集群搭建

- 在对应的`conf/application.properties`下面添加自定义的持久化MySQL数据库的配置，并使用官方的SQL初始化对应的数据库和表

```java
spring.datasource.platform=mysql
db.num=1 (MySQL配置的个数)
db.url.0=jdbc:mysql://127.0.0.1:13306/nacos_config?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true
db.user=root
db.password=hujing19960825
```

- 修改`conf/cluster.conf`的IP配置

```shell
192.168.88.15:3333
192.168.88.15:4444
192.168.88.15:5555  #集群ip
```

- 到bin目录下修改startup.sh用来支持自定义端口

```shell
export FUNCTION_MODE="all"
while getopts ":m:f:s:p:" opt #这里添加了:p代表端口
do
    case $opt in
        m)
            MODE=$OPTARG;;
        f)
            FUNCTION_MODE=$OPTARG;;
        s)
            SERVER=$OPTARG;;
#######  添加内容     ############
        p)
            PORT=$OPTARG;;
#######  添加内容     ############
        ?)
        echo "Unknown parameter"
        exit 1;;
    esac
done
#最后执行的时候添加--server.port参数
nohup $JAVA ${JAVA_OPT} --server.port=${PORT} nacos.nacos >> ${BASE_DIR}/logs/start.out 2>&1 &
```

- 使用`sh startup.sh -p `启动即可
- 配置nginx进行反向代理

```nginx
    upstream cluster {
        server 192.168.1.20:8848;
        server 192.168.1.20:8849;
        server 192.168.1.20:8850;
    }
    server {
        listen       80;
        server_name  localhost;
        location / {
           proxy_pass http://cluster;
        }
   }
```

###### 	基于docker搭建Nacos-Server

- 修改`docker-compose.yml`

  有下面几个注意点

  - 官方默认MySQL是支持mysql5，这里使用数据卷将MySQL8.x的jar包设置到nacos容器中
  - nacos版本使用的是1.2.0

```yaml
version: "2"
services:
  nacos1:
    image: nacos/nacos-server:latest
    container_name: nacos1
    networks:
      nacos_net:
        ipv4_address: 172.16.238.10
    volumes:
      - ./plugins/mysql/:/home/nacos/plugins/mysql/
      - ./cluster-logs/nacos1:/home/nacos/logs
    ports:
      - "8848:8848"
      - "9555:9555"
    env_file:
      - ../env/nacos-ip.env
    restart: on-failure
  nacos2:
    image: nacos/nacos-server:latest
    container_name: nacos2
    networks:
      nacos_net:
        ipv4_address: 172.16.238.11
    volumes:
      - ./plugins/mysql/:/home/nacos/plugins/mysql/
      - ./cluster-logs/nacos2:/home/nacos/logs
    ports:
      - "8849:8848"
    env_file:
      - ../env/nacos-ip.env
    restart: always
  nacos3:
    image: nacos/nacos-server:latest
    container_name: nacos3
    networks:
      nacos_net:
        ipv4_address: 172.16.238.12
    volumes:
      - ./plugins/mysql/:/home/nacos/plugins/mysql/
      - ./cluster-logs/nacos2:/home/nacos/logs
    ports:
      - "8850:8848"
    env_file:
      - ../env/nacos-ip.env
    restart: always
networks:
  nacos_net:
    driver: bridge
    ipam:
      driver: default
      config:
        - subnet: 172.16.238.0/24

```

- 配置`env/nacos-ip.yml`

```shell
NACOS_SERVERS=172.16.238.10:8848 172.16.238.11:8848 172.16.238.12:8848 #设置docker集群地址
MYSQL_SERVICE_HOST=192.168.1.20 #数据库ip
MYSQL_SERVICE_DB_NAME=nacos_config #数据库名称
MYSQL_SERVICE_PORT=3307
MYSQL_SERVICE_USER=hujing
MYSQL_SERVICE_PASSWORD=hujing19960825
```

##### Client

- maven配置

```xml
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
```

- yml

```yaml
spring:
  application:
    name: nacos-provider-payment
  cloud:
    nacos:
      server-addr: localhost:8848 # nacos服务地址
```

### 2、远程调用

#### Ribbon+RestTemplate

```java
@Configuration
class Config{
  
  @LoadBalanced //开启ribbon负载均衡
  @Bean
  public RestTemplate restTemplate(){
    return new RestTemplate();
  }
}
```

- 可以通过@RibbonClient来扩展ribbon配置，但是不能被SpringBoot扫描到，否则会被Spring上下文扫描到变成全局配置

```java
//该扩展配置文件不能被SpringBoot 扫描到，不然会被Spring上下文扫描到，导致所有的实例名(当前配置对应的实例和其他所有实例都包括)
// 都会被该默认配置覆盖。
@Configuration
public class CustomRibbonConfig {
    @Bean
    public IRule rule(){ //配置负载均衡策略
        return new RandomRule();
    }
}
```

- 在yaml中配置ribbon,详细配置项可以看`CommonClientConfigKey`

```yaml
<service name>:
	ribbon:
		xxxx: yyyy

---

ribbon:
	ReadTimeout: 5000 //配置超时时间
	ConnectTimeout: 5000
```

#### Feign+Ribbon

```java
@FeignClient(value = "cloud-provider-payment",configuartion="xxxFeign扩展配置")
public interface PaymentClient {

    @GetMapping("echo/{id}")
    String echo(@PathVariable("id") Integer id);


```

- yaml配置全局feign配置

```yaml
feign:
  client:
    config:
      default:
        logger-level: full #日志级别，可查看FeignClientConfiguration
        connect-timeout: 5000 #feignClient粒度的超时时间配置
        read-timeout: 5000
  hystrix:
    enabled: true #开启feign的hystrix支持
logging:
  level:
    cloud.study: debug
```

### 3、服务容错、熔断、限流

#### Hystrix

- 集成依赖

```xml
       <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
```

- 开启注解`@EnableCircuitBreker`

##### 容错支持`@HystrixCommand`

```java
    @GetMapping("/echoWithHystrix")
    @HystrixCommand(fallbackMethod = "echoFallback", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000") //设置熔断超时时间
    })
    public String echoWithHystrix() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "执行成功O(∩_∩)O哈哈~";
    }

    /**
     * Hystrix fallBack method
     * 1.超时
     * 2.异常报错
     */
    public String echoFallback() {
        return "服务器繁忙o(╥﹏╥)o";
    }
```

更多HystrixCommand属性可以参考`HystrixCommandProperties`

##### 熔断，断路器模式

hystrix工作流程图 

- 是否缓存，缓存直接返回
- 是否在断路开启状态，是直接返回fallback
- 不是则查看线程池/信号量是否满了
- 执行方法查看是否执行失败或者超时

![image-20200328175345561](/Users/hujing/Library/Application Support/typora-user-images/image-20200328175345561.png)

```java
    @GetMapping("/echo1/{id}")
    @HystrixCommand(defaultFallback = "defaultFallback", commandProperties = {
        //开启断路器模式，默认true
        @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),
        //请求到达多少次才会触发熔断，默认20次
        @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
        //熔断触发时间窗口
        @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "30000"),
        //百分之10就进行熔断
        @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "10")
    })
    public String echo1(@PathVariable Integer id) {
        return paymentClient.echo(id);
    }
```

- 在一段时间内达到请求次数，并且达到错误率则进入熔断状态
- 在一个窗口时间结束后，切换为半开状态，若下次请求还是失败，继续进入熔断
- 若下次请求成功则关闭熔断

##### 解决太多熔断方法膨胀代码？

```java
@DefaultProperties(defaultFallback = "defaultFallback") //公用fallback
```

##### 解决fallback方法和业务耦合在一起

```java
@FeignClient(value = "cloud-provider-payment" ,fallback = PaymentClient.PaymentClientFallback.class) //使用fallback,fallbackFactory来处理耦合
public interface PaymentClient {

    @GetMapping("echo/{id}")
    String echo(@PathVariable("id") Integer id);

		//可以防止到单独类中进行fallback处理
    @Component
    class PaymentClientFallback implements PaymentClient{
        @Override
        public String echo(Integer id) {
            return "FeignClient FallBack";
        }
    }

    @Component
    class PaymentFallbackFactory implements FallbackFactory<PaymentClient> {

        @Override
        public PaymentClient create(Throwable cause) {
            return () -> {
                System.out.println(cause.getMessage());
                return "fallbackFactory";
            };
        }
    }
}
```

##### 整合DashBoard

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
        </dependency>
```

#### Sentinel

- maven

```xml
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
        </dependency>
	<!-- sentinel-nacos--> 持久化规则
        <dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-datasource-nacos</artifactId>
        </dependency>
```

- yml

```yml
spring:
  application:
    name: nacos-provider-payment
  cloud:
    nacos:
      server-addr: localhost:8848
    sentinel: #sentinel-dashboard控制台
      transport:
        dashboard: localhost:8080
        port: 8719 # 心跳监听端口，重复会自增
      filter: # filter拦截的HttpRequest路径
        url-patterns: /**
      eager: true # 关闭懒加载
      datasource: # 持久化相关配置
        flow:
          nacos:
            serverAddr: localhost:8848
            dataId: ${spring.application.name}-flow-rule # nacos上面的dataId配置名称
            dataType: json #默认
            ruleType: flow #参考RuleType # 控制规则
```

**指定nacos配置之后，只能通过在nacos配置中心上面添加配置才能动态持久化配置**

配置属性对应具体的规则。eg : FlowRule , DegradeRule

```json
[
    {
        "resource": "/helloworld", //资源名称
        "limitApp": "default", //流控来源
        "grade": 1, //限流阈值类型 0->线程数 1-> QPS
        "count": 2, //阈值
        "strategy": 0, //流控模式 0 直接 1 关联 2 链路
      	"refResource": "xx", //关联资源获取链路入口
        "controlBehavior": 0, //控制效果 0快速失败 1 warm up 2 排队等待
        "clusterMode": false //是否集群模式
    }
]

```

- 具体控制台的参数使用

  

  ![image-20200405183153103](/Users/hujing/Library/Application Support/typora-user-images/image-20200405183153103.png)

  - 流控规则
    - QPS/线程数
    - 直接/关联(关联资源达到阈值，自己限流，比如支付扛不住了，就限流下单接口)/链路（限制链路入口的阈值，达到则限流自己）
    - 快速失败 / 预热 （在配置的时间内逐渐达到阈值，防止大流量直接击垮服务）/ 排队等待 （进入队列等待，超时失败）

  ![image-20200405183221026](/Users/hujing/Library/Application Support/typora-user-images/image-20200405183221026.png)

  - 降级规则
    - RT ： 超时时间
    - 异常比例 :  qps>=5的时候，达到异常比例则进入熔断
    - 异常数 ： 达到异常数进入熔断

  ![image-20200405183233949](/Users/hujing/Library/Application Support/typora-user-images/image-20200405183233949.png)

  - 热点规则

    > 只支持基本类型和String类型

    可以为资源的某个参数的所有值/某写特殊值进行限流

  ![image-20200405183322213](/Users/hujing/Library/Application Support/typora-user-images/image-20200405183322213.png)

  - 系统规则
    - 在对应系统规则达到阈值则进行限流

- `@SentinelResource`的使用

> 声明Sentinel资源的注解接口，类似于HystrixCommand

```java
   @SentinelResource(value = "hotKey",blockHandler = "hotKeyBlocked",fallback="xx"，
                    exceptionsToIgnore = "customException")
    //HotKey不支持复杂类型参数
    public String testEchoHotKey(@RequestBody HotKeyParam hotKeyParam) {
        return "blocked";
    }
    public String hotKeyBlocked(HotKeyParam hotKeyParam, BlockException exception){
        return hotKeyParam+"blocked-> "+exception.getMessage();
    }
```

- blockHandler ： 用来处理所有在sentinel-dashboard配置的参数达到阈值的兜底方法，不处理程序中的异常
- fallback : 用来处理Java程序中发生异常的兜底方法
- exceptionsToIgnore ： 忽略的异常

- 核心参数
  - SphU.entry() ： 资源入口
  - ContextUtil : 上下文
  - Trace : 统计组件

### 4、网关

#### Zuul

#### Gateway

> 新一代微服务网关，代替zuul1.x

##### Maven依赖

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
```

##### 路由 : routing

##### 断言 : predicates

> 在请求路由之前进行断言，若不符合则不通过路由转发，都是RoutePredicateFactory的实现类

```yaml
spring:
	cloud:
    gateway:
      discovery:
        locator:
          enabled: true #开启使用服务名进行路由
      routes:
        - id: cloud-provider-payment
          #  uri: http://localhost:8001 可以写死url
          uri: lb://cloud-provider-payment #使用负载均衡服务名进行动态路由匹配
          predicates: #参考RoutePredicateFactory的实现类
            - Path=/payment/echo/{segment},/payment/**
# 支持的path映射,其中segment可以在GatewayFilter中通ServerWebExchangeUtils#getUriTemplateVariables()获取
              - Header=token,\d+ #下游服务必须要有对应的header
              - Cookie=MyCookieName,MyCookieValue #下游必须有cookie
              - Method=GET,POST #下游支持的方法
              - Query=requestParamName #下游请求参数
              - RemoteAddr=192.168.1.1/24 #规定下游的ip地址范围
              - Weight=group1,8 #访问权重，group为组名，后面为权重
```

##### 过滤 : filters

> 主要功能是在请求之前和之后做一些增强操作，都是GatewayFilterFactory的实现类

```yaml
spring:
	cloud:
    gateway:
      discovery:
        locator:
          enabled: true #开启使用服务名进行路由
      routes:
        - id: cloud-provider-payment
          #  uri: http://localhost:8001 可以写死url
          uri: lb://cloud-provider-payment #使用负载均衡服务名进行动态路由匹配
          filters:
            - AddRequestHeader=customRequestHeader,header-{segment} #这里segment可以获取上面Path断言中传递的uriTemplatesVariables
            - AddRequestParameter=customRequestParameter,xxx  #请求参数添加
            - AddResponseHeader=xxx,yyy #添加响应头
            - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin #在网关和下游都添加有CORS跨域请求头的时候，然后重复的请求头
            - name: CircuitBreaker #
              args:
                name: fallback
                fallbackUri: forward:/payment/fallback #默认fallback地址
```

##### 自定义GatewayFilter

```java
/**
 * 自定义简易网关全局过滤器
 **/
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("welcome to my spring-cloud-learning project.");
        String nameParam = exchange.getRequest().getQueryParams().getFirst("name");
        if (nameParam == null) {
            ServerHttpResponse response = exchange.getResponse();
            return response.setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
```

### 5、分布式配置中心

#### SpringCloud-Config

> 基于Git的分布式配置管理，需要自己搭建config-server端

##### Config-Server

- pom

```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-config-server</artifactId>
    </dependency>
```

- yml

```yml
spring:
  application:
    name: config-server
  cloud: # Consul注册中心配置
    consul:
      host: localhost
      port: 8500
    config: # Config-Server的Git地址配置
      server:
        git:
          uri: https://github.com/zzhujing/cloud-config-server.git
          search-paths:
            - cloud-config-server
          username: zzhujing
          password: hujing19960825
      label: master
management:
  endpoints:
    web:
      exposure:
        include: ['bus-refresh']
```

- 配置

主程序类上添加`@EnableConfigServer`

##### Config-Client

- pom

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency
```

- yml

在`bootstrap.yml`中指定配置

```yml
spring:
  application:
    name: cloud-provider-payment
    cloud:
      config:
        label: master #所在分支
        profile: prod #对应环境
        name: config #获取的远程配置文件名
        uri: http://localhost:8080 #Config-Server地址
```

- 加载规则

Branch/{spring.application.name}-{profile}-{properites/yml}

Branch/{spring.cloud.config.name}-{profile}-{propertiles/tml}

##### 如何动态刷新配置？

- 局部手动刷新，用POST方法访问/actuator/refresh监控端点
- 集成spring-cloud-bus进行总线消息推送

###### 整合`spring-cloud-bus`

- maven

```xml
    <dependency> <!-- rabbitmq 的实现方式 -->
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-bus-amqp</artifactId>
    </dependency>
```

- yml

```yml
management:
  endpoints:
    web:
      exposure:
        include: ['bus-refresh']
spring:
  rabbitmq:
  	username: guest
    password: guest
    host: localhost
    port: 5672
```

- 对`config-server`进行POST请求/bus-refresh即可(使用的是rabbitmq发送一个topic类型的消息，广播给各个服务的/actuator/refresh）
- 在需要动态刷新的配置类上面进行`@RefreshScope`标注

#### Nacos

- maven

```xml
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
```

- yml

```yaml
spring:
  application:
    name: manuf-service
  cloud:
    nacos:
      config:
        ext-config: # 可以配置多个文件
          - data-id: nacos-discovery.yml # 注册到nacos上面的dataId
            refresh: true                # 是否动态刷新
          - data-id: feign-ext.yml
            refresh: true
        server-addr: 192.168.18.91:8848 # 配置中心地址
        file-extension: yml # 配置中心文件后缀格式
```

- 配置@RefreshScode即可进行动态刷新啦

### 6、分布式消息服务

> Spring-Cloud-Stream是通过在各种消息中间件中间使用Binder进行中间桥梁搭建，让一个系统中可以通过其进行多种消息中间件的对接

#### rabbitMq

- maven

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
        </dependency>
```

- yml

```yaml
spring:
  application:
    name: cloud-stream-provider
  cloud:
    stream:
      binders:
        defaultRabbit: #自定义Binder名称
          type: rabbit
          environment:
            spring: #rabbitmq环境配置
              rabbitmq:
                host: localhost
                port: 5672
                username: guest
                password: guest
      bindings:
        output: #自定义binding名称 提供者output / 消费者 input (也可以自定义)
          destination: customExchange #raibbitmq中Exchange名称
          content-type: application/json
          binder: defaultRabbit #定义自定义Binder名称
          group:  # 组字段用来解决重复性消费以及消息持久化问题(宕机重启之后依旧可以消费消息)
```

- 实践代码

##### 自定义的ChannelBinding

```java
public interface CustomerChannel {

    String OUTPUT = "my-output";   //对应spring.cloud.stream.bindings.<bindingName>
    String INPUT = "my-input";

    @Output(CustomerChannel.OUTPUT)
    MessageChannel output();

    @Input(CustomerChannel.INPUT)
    SubscribableChannel input();

}
```



##### 发送消息

```java
@EnableBinding(Source.class) //绑定提供channel名称
public class MessageProvider {

    @Autowired
    private MessageChannel output; //输出Channel，也可以直接把Source给注入进来

    public String sendMsg() {
        String msg = "Hello,Spring-Cloud-Stream";
        output.send(MessageBuilder.withPayload(msg).build());
        return null;
    }
}
```

##### 接受消息

```java
@EnableBinding(Sink.class)
public class CloudStreamConsumer {

    @Value("${server.port}")
    private String port;

    @StreamListener(Sink.INPUT) //使用监听注解监听消息
    public void received(Message<String> message) {
        System.out.println(port + " 收到消息: " + message.getPayload());
    }
}
```

#### RocketMq

#### kafka

### 7、分布式链路追踪

#### Sleuth + Zipkin

- 下载zipkin的服务端。并启动

- maven

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zipkin</artifactId>
        </dependency>
```

- yml

```yaml
spring:
  zipkin:
    base-url: http://localhost:9411/ # zipkin-server 地址
  sleuth:
    sampler:
      probability: 1 #采集样本所占比例 0~1
```

#### SkyWalking

### 8、分布式事务

#### seata

##### AT模式原理 :  两阶段提交

- 一阶段业务SQL和undo_log回滚日志在同一本地事务中提交，释放本地锁和连接资源
- 二阶段全局异步提交或根据undo_log逆向回滚

- 执行流程

  - Before image , 保存当前数据到undo_log的rollback_info字段
  - 执行业务sql
  - after image , 保存执行业务SQL后的数据到undo_log

  ```json
  {
  	"branchId": 641789253,
  	"undoItems": [{
  		"afterImage": {
  			"rows": [{
  				"fields": [{
  					"name": "id",
  					"type": 4,
  					"value": 1
  				}, {
  					"name": "name",
  					"type": 12,
  					"value": "GTS"
  				}, {
  					"name": "since",
  					"type": 12,
  					"value": "2014"
  				}]
  			}],
  			"tableName": "product"
  		},
  		"beforeImage": {
  			"rows": [{
  				"fields": [{
  					"name": "id",
  					"type": 4,
  					"value": 1
  				}, {
  					"name": "name",
  					"type": 12,
  					"value": "TXC"
  				}, {
  					"name": "since",
  					"type": 12,
  					"value": "2014"
  				}]
  			}],
  			"tableName": "product"
  		},
  		"sqlType": "UPDATE"
  	}],
  	"xid": "xid:xxx"
  }
  ```

  - 提交本地事务前获取全局锁，注册分支。
  - 提交SQL和undo_log然后上报给TC
  - 二阶段异步提交，删除undo_log记录
  - 二阶段回滚，根据全局id和分支id获取undo_log，逆向生成sql进行回滚，若after image和当前数据库不一样，则发生脏写(幻读)。转人工处理！

##### 搭建seata-server

- 下载seata-server

- 修改`conf/registry.conf`，使用`nacos`作为注册和配置中心，seata支持`file,nacos,eureka,redis,zk,consul,sofa...`

  - 使用官网提供的nacos-config.sh将`config.txt`中的seata配置写入到nacos配置中心

  ```properties
  transport.type=TCP
  transport.server=NIO
  transport.heartbeat=true
  transport.enable-client-batch-send-request=false
  transport.thread-factory.boss-thread-prefix=NettyBoss
  transport.thread-factory.worker-thread-prefix=NettyServerNIOWorker
  transport.thread-factory.server-executor-thread-prefix=NettyServerBizHandler
  transport.thread-factory.share-boss-worker=false
  transport.thread-factory.client-selector-thread-prefix=NettyClientSelector
  transport.thread-factory.client-selector-thread-size=1
  transport.thread-factory.client-worker-thread-prefix=NettyClientWorkerThread
  transport.thread-factory.boss-thread-size=1
  transport.thread-factory.worker-thread-size=8
  transport.shutdown.wait=3
  #这里很重要，配置和spring.cloud.alibaba.seata.tx_service_group相同，可配置多个
  service.vgroup_mapping.test_group=default          
  service.default.grouplist=127.0.0.1:8091
  service.enableDegrade=false
  service.disableGlobalTransaction=false
  client.rm.async.commit.buffer.limit=10000
  client.rm.lock.retry.internal=10
  client.rm.lock.retry.times=30
  client.rm.report.retry.count=5
  client.rm.lock.retry.policy.branch-rollback-on-conflict=true
  client.rm.table.meta.check.enable=false
  client.rm.report.success.enable=true
  client.tm.commit.retry.count=5
  client.tm.rollback.retry.count=5
  #事务存储模式
  store.mode=db 																				
  store.file.dir=file_store/data
  store.file.max-branch-session-size=16384
  store.file.max-global-session-size=512
  store.file.file-write-buffer-cache-size=16384
  store.file.flush-disk-mode=async
  store.file.session.reload.read_size=100
  #数据库相关配置
  store.db.datasource=dbcp  													
  store.db.db-type=mysql
  store.db.driver-class-name=com.mysql.cj.jdbc.Driver
  store.db.url=jdbc:mysql://127.0.0.1:3307/seata?useUnicode=true
  store.db.user=hujing
  store.db.password=hujing19960825
  store.db.min-conn=1
  store.db.max-conn=3
  store.db.global.table=global_table
  store.db.branch.table=branch_table
  store.db.query-limit=100
  store.db.lock-table=lock_table
  server.recovery.committing-retry-period=1000
  server.recovery.asyn-committing-retry-period=1000
  server.recovery.rollbacking-retry-period=1000
  server.recovery.timeout-retry-period=1000
  server.max.commit.retry.timeout=-1
  server.max.rollback.retry.timeout=-1
  client.undo.data.validation=true
  client.undo.log.serialization=jackson
  server.undo.log.save.days=7
  server.undo.log.delete.period=86400000
  client.undo.log.table=undo_log
  client.log.exceptionRate=100
  transport.serialization=seata
  transport.compressor=none
  metrics.enabled=false
  metrics.registry-type=compact
  metrics.exporter-list=prometheus
  metrics.exporter-prometheus-port=9898
  #是否支持自动数据库代理
  client.support.spring.datasource.autoproxy=false
  
  ```

  - `sh nacos-config.sh` 将配置同步到配置中心，完成后再nacos上面能看到配置即可

  - 这里使用nacos做为seata的配置中心，所以`file.conf`文件我们可以不用操作，否则需要修改**

    

  ```shell
  service {
  #事务组名，和spring.cloud.alibaba.seata.tx-service-group相同
    vgroup_mapping.my_test_tx_group = "default" 
    #only support when registry.type=file, please don't set multiple addresses
    default.grouplist = "127.0.0.1:8091"
    #disable seata
    disableGlobalTransaction = false
  }
  ## transaction log store, only used in seata-server
  store {
    ## store mode: file、db
    mode = "db" #事务日志存储模式,使用db
  
    ## file store property
    file {
      ## store location dir
      dir = "sessionStore"
    }
    db {
      datasource = "dbcp" 
      db-type = "mysql"
      #可以改成mysql-8，启动server的时候需要添加mysql8的jar包到lib目录
      driver-class-name = "com.mysql.jdbc.Driver" 
      url = "jdbc:mysql://127.0.0.1:3306/seata" #seata-server依赖的数据库的位置
      user = "mysql"
      password = "mysql"
    }
  }
  
  ```

- 启动seata-server即可，如果nacos上面有**serverAddr**的服务实例

##### Client

- maven

**注意版本，版本对不上会有很多的问题发生**

```xml
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
        </dependency>
```

- yml

```yaml
spring:
  application:
    name: seata-storage
  cloud:
    nacos:
      server-addr: localhost:8848
    alibaba:
      seata:
        tx-service-group: test_group
```

- 在`resource`类路径下添加registry.conf文件即可

```shell
registry {
  # file 、nacos 、eureka、redis、zk、consul、etcd3、sofa
  type = "nacos"
  nacos {
    serverAddr = "localhost"
    namespace = ""
    cluster = "default"
  }
}
config {
  # file、nacos 、apollo、zk、consul、etcd3
  type = "nacos"
  nacos {
    serverAddr = "localhost"
    namespace = ""
    group = "SEATA_GROUP"
  }
}

```

- 在TM业务逻辑加上`@GlobalTransactional`

```java
    /**
     * 下单
     *
     * @param order 订单相关信息
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class,name="test_gloabl_tx")
    public void placeAnOrder(Order order) {
        //扣除库存
        storageClient.reduceStorage(order.getProductId(), order.getCount());
        //扣余额
        accountClient.reduceAccount(order.getUserId(), 10);
        //创建订单
        createOrder(order);
    }
```


