# `JVM `调优及问题追踪



## 内存问题

**为什么有内存问题？**

java的垃圾自动回收机制（GC）



### 内存问题的两种形式：

#### 内存溢出（OOM）

堆内溢出

堆外溢出（排查困难）







#### 内存泄漏(ML  - Memory Leak)





## 可达性分析

### 引用级别

**强引用**

**软引用**

**弱引用**

**虚引用**





## JVM 控制参数

<img src="img/JVM调优及问题追踪/image-20200615121851201.png" alt="image-20200615121851201" style="zoom:50%;" />







## 命令工具

### jmap





### pmap





### wrk

并发压测工具

```
wrk -t20 -c20 -d300s http://127.0.0.1:8084/api/test
-t 使用的线程数
-c 开启的连接数量
-d 持续压测的时间 
```





### jhsdb 

`java9`后废弃了``jmap`，使用该命令





## 工具

### MAT

eclipse的插件