

# Spring 补充

`Bean`实例化的生命周期

![image-20200618164425813](img/spring补充/image-20200618164425813.png)

spring中实例化`ioc`容器时最重要的方法在`AbstractApplicationContext`中：

>  org.springframework.context.support.AbstractApplicationContext#refresh()