# `JVM` 基础



## `JVM` 内存模型`JMM` （`Java memory model`）

### 内存模型概念













## 参数控制JVM

### 1. 堆

`-Xms` 最小堆内存

`-Xmx` 最大堆内存



### 2. 元数据

`jdk1.8`后移除了永久代概念，增加元数据空间



`-XX:MetaSpaceSize`：元数据初始化空间

`-XX:MaxMetaSpaceSize`：最大元数据空间



## 3. 栈空间



## 4. 垃圾收集器的参数



#### G1(Garbage-First Garbage Collector)



#### Parallel Collector （并行收集器）



#### 启动不同垃圾收集器

![image-20200622122457224](img/JVM基础/image-20200622122457224.png)

## 垃圾收集器

