

# SpringBoot 



思想：约定优于配置（convertion over configration）

目的：简化spring配置

原因：

	1. 配置繁琐
 	2. 依赖太多，依赖管理困难



两大核心：

​	起步依赖

​	自动配置





## 使用

### 测试



### 热部署

1. 引入`spring-boot-devtools`包

2. 开启IDEA的自动编译
3. `Ctrl + Shift + Alt + /` 打开`maintenance`选项卡，在选项卡中找到`complier.automake.allow.when.app.running`，并勾选。



### 全局配置文件

默认名称：`application.properties/yaml`

`yaml`后缀也可以写作`yml`。

#### yaml配置写法

```yaml
server: 
	port: 8080 # 冒号后有空格
	
person:
	map: {key1:v1,key2:v2}
	array: [1,2,3,4]
	obj: {name:'lili',sex:'男'}
```







### 注解

* `@ConfigrationProperties(prefix="person")`

将配置文件中以`person`开头的值注入到具体对象中。如果想要在配置时可以看到提示，需要引入如下依赖并重新编译：

```
spring-boot-configration-processor
```

* `@PropertySource`

该注解用于读取自定义的配置文件

* `@SpringbootApplication`

启动类标识，用于启动自动扫描`spring`注解



* `@conditionOnXXX`
  * `@ConditionOnClass`
  * `@ConditionOnMissingClass`
  * `@ConditionOnBean`
  * `@ConditionOnMissingBean`
  * `@ConditionOnSingleCandidate`
  * `@ConditionOnExpression`
  * `@ConditionOnProperty`
  * `@ConditionOnResource`
  * `@ConditionOnJndi`
  * `@ConditionOnJava`
  * `@ConditionOnWebApplication`
  * `@ConditionOnNotWebApplication`



## 自定义stater

1. 创建`maven`工程引入`spring-boot-autoconfigration`包
2. 创建配置类，标注`@configration`，其中创建要注入到容器中的`bean`对象，并标注`@ConditionXXX`条件
3. 在`resources/META_INF`下创建`spring.factories`文件

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\ com.lagou.config.MyAutoConfiguration

```






