# dubbo学习记录



记录一些看源码、文档时的理解点





[TOC]



## dubbo的配置关系：



参见官方文档配置部分，下面图出自[XML 配置](http://dubbo.apache.org/zh-cn/docs/user/configuration/xml.html)

dubbo的配置项很多，一开始往往会昏头，哪些配置是属于哪些？`dubbo:service`、`dubbo:reference`、`dubbo:registry`、`dubbo:monitor`……等，这么多项，他们的关系是什么，从下面的图就可以看出来了，可以给自己一个大体的认知和区分。

### 配置模块的关系怎么看？

![image-20200415160737274](img/dubbo学习记录/image-20200415160737274.png)

配置分为4个大模块，比如`provider-side`表示提供者的配置块，那么它可以配置的就是与它相关联的那些配置块。这里的关系表示是UML类图中的表示方法，只是将配置抽象出来，而不是一具体的类。

> UML类图的内容参考：[看懂UML类图和时序图](https://design-patterns.readthedocs.io/zh_CN/latest/read_uml.html)

比如：

`ServiceConfig`（服务配置）继承自`ProviderConfig`，即表示可以配置提供者是哪些。

`providerConfig`又依赖`ProtocolConfig`，即又还可以配置暴露的协议。

同样，它还依赖`RegistryConfig`注册中心模块、`MonitorConfig`监测中心模块、`ApplicationConfig`模块，这些都可以配置。

最后，`sub-config`是它的组成部分，强依赖关系。即可以配置到方法级别、参数级别去。



> PS：
>
> application-shared、sub-config两个模块分别提供给服务提供方、消费方去依赖使用。



> sub-config之所以可以给两者都依赖，因为api是单独抽象出来的，即提供方和消费方都用的同一套api。只是提供方负责实现，消费方负责引用。



### Dubbo Schema标签

<img src="img/dubbo学习记录/image-20200423113431317.png" alt="image-20200423113431317" style="zoom:50%;" />

dubbo 的配置项很多，这里记录一下理解点。

`dubbo:service`、`dubbo:reference`配置具体的服务提供者和消费者。

`dubbo:provider`、`dubbo:consumer`为多个服务提供者、消费者的默认配置，针对所有提供者和消费者的。



### 配置优先级

1. 精确的优先
2. 级别一样的，消费方比服务方优先

![image-20200423135940557](img/dubbo学习记录/image-20200423135940557.png)



	### 配置

该部分内容参考官方文档的配置，这里记录一些额外的东西。



#### 重试 （retries）

配置消费者时，幂等性的服务适合配置重试次数。

幂等性：同样的方法执行多次和执行一次的效果是一样的。（查询、删除、修改）

非幂等性：同样的方法执行多次会有不一样的效果。（新增）



#### 灰度发布

通过版本控制，让一些消费者使用新的版本，其他还是使用老版本，等稳定了在逐步全都替换为新版本。这样一旦新版本有什么问题不会大面积影响服务。



#### 本地存根

一般服务消费方只是拥有某个接口api，然后通过dubbo远程调用提供者。但是如果消费方想自己在调用远程操作前做一些缓存、验证等工作时，可以自己实现这个接口api，并创建一个能传入提供者proxy的构造方法，那么就可以先执行自己实现的逻辑，再调用远程接口。

具体配置见官网文档`示例——本地存根`

>  实际开发中，本地存根通常是将实现放在抽奖出来的api包里。



#### 服务容错

`dubbo`提供了一些容错策略，见文档：`示例 —— 集群容错`

另外，可以继承 `hystrix` 来进行容错





## 实战



###  一个简单的demo

​	启动一个本地的`zookeeper`

​	写一个公用的api，打包

​	写一个服务提供者，实现api包中的接口

​	写一个消费者，引用远程的提供者

​	启动一个`dubbo-admin`

​	

### dubbo和springboot

[Dubbo Spring Boot 工程](https://github.com/apache/dubbo-spring-boot-project/blob/master/README_CN.md)

[视频：Dubbo 视频教程全集--12p](https://www.bilibili.com/video/BV1zt411M7pF?p=13)

##### springboot和dubbo整合的三种方式：

1. 使用`@EnableDubbo`注解开启`dubbo`的注解扫描，使用注解：`@service`、`@Reference`暴露和应用服务。并在`applicaion.properties`中配置`dubbo`的内容。

> 缺点是，无法精确控制到method级别

2. 使用`@ImportResource`注解引入基于`xml`的配置文件
3. 基于`java Bean`的`API`方式，手动创建对应的配置`Bean`注入容器中。参见文档：`配置 —— API配置`



