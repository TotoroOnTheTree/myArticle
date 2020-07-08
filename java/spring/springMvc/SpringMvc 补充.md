# SpringMvc 补充







手写MVC时读取方法形参名，需要配置maven插件：

<img src="img/SpringMvc 补充/image-20200610142044596.png" alt="image-20200610142044596" style="zoom: 67%;" />





## 1. 概念

其他概念参看springMvc讲义

是什么？



能做什么？





### 1.1  MVC 模式和经典三层的关系

MVC为表现层的代码组织方式，不包含业务层和DAO层。

<img src="img/SpringMvc 补充/经典三层&amp;mvc模式.png" style="zoom:33%;" />



### 1.2 九大组件：





#### `HandlerMapping` 处理器映射（三大件）



#### `HandlerAdapter` 处理器适配器（三大件）



#### `ViewResolver` 视图解析器（三大件）



#### HandlerExceptionResolver	异常解析器



#### MultipartResolver	文件上传解析器



#### RequestToViewNameTranslator		请求视图转换器

将`request`请求转化为逻辑视图`view`

#### FlashMapManager	闪存管理器

#### ThemeResolver	主题解析器

#### LocaleResolver	国际化解析器

#### 







## 2. 使用



### 2.1  与spring集成

参见讲义



####  2.2 静态资源映射

原因： `DispatcherServlet`配置`web.xml`时，将`<url-pattern>`配置为`/`时，会拦截除了`*.jsp*`之外的所有路径，包括静态资源。当然如果配置为`*.do, *.action ...`这种指定了具体后缀的方式就不会拦截到了。

为什么不拦截`*.jsp*`呢？因为`tomcat`容器中有一个父`web.xml`，里面配置了一个`JspServlet`拦截`jsp`后缀的请求，相当于容器给处理了。



三种方式：

1. 将静态资源访问交给`web`容器去处理，在`web.xml`中配置多个静态资源类型，这样会将这类匹配的路径交给`web`容器（tomcat）的`DefaultServlet`去处理：

```xml
<servlet-mapping>
    <servlet-name>default</servlet-name>
    <url-pattern>*.jpg</url-pattern>
</servlet-mapping>
<servlet-mapping>
    <servlet-name>default</servlet-name>
    <url-pattern>*.js</url-pattern>
</servlet-mapping>
<servlet-mapping>
    <servlet-name>default</servlet-name>
    <url-pattern>*.css</url-pattern>
</servlet-mapping>
```



2. springMvc.xml中去配置（有mvc的域名空间的配置文件）：

```xml
<mvc:default-servlet-handler/>
```

该标签会在`springMVC`上下文中定义一个`DefaultHttpServletHandler`对象，它会检查进入的请求路径，对静态资源请求进行过滤，如果是静态资源 请求，会将请求转给web应用服务器（tomcat)默认的`DefaultServlet`处理。

这种方式的弊端：静态资源只能放到`webapp`根目录下，不能放到其他目录中，比如`resources、WEB_INFO、项目目录`等。

3. 将静态资源交给`springMvc`处理

```xml
<mvc:resources location="classpath:/" mapping="/resources/**">
```

`location`：匹配文件路径

`mapping`：匹配Url路径



参考文章：[SpringMVC访问静态资源的三种方式](https://blog.csdn.net/u012730299/article/details/51872704#)





#### 2.3  字符集编码转换

// TODO



## 3. 源码追踪

追踪什么？

1. springMVC的组件什么时候初始化的？

   

步骤：

找到`org.springframework.web.servlet.DispatcherServlet`，作为`mvc`的核心，其内部定义了9大组件。

观察到其有一个初始化方法`org.springframework.web.servlet.DispatcherServlet#initStrategies`，打断点跟踪

<img src="img/SpringMvc 补充/image-20200612175023365.png" alt="image-20200612175023365" style="zoom:50%;" />



2. spring如何和springMvc通信，唤起springMVC的初始化的？









## 4. Classpath理解

