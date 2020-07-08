# tomcat补充

课程内容参考《Apache Tomcat Web应用服务器课程笔记.pdf》



查看`jvm`内存占用：

> `jhsdb jmap --heap -pid xxx `



`Nginx`压缩、缓存调优：

![image-20200623225201475](img/tomcat补充/image-20200623225201475.png)





##  作业

### 实现步骤：

1. 将公用的`servlet`接口、`HttpServlet、request、response`这些类抽出来单独打成`jar`包，提供给`mini_tomcat`和`demoA、demoB`去使用。

<img src="img/tomcat补充/image-20200623110936843.png" alt="image-20200623110936843" style="zoom:50%;" />



2. 因为要读取放在`C:/users/webapps`中的多个项目中的资源（类、静态文件），所以重写`ContexClassLoader`，继承自`URLClassLoader`。

   

   因为新建`ContextClassLoader`时，给它指定的`classPath`是具体项目的，所以依然可以严格按照双亲委派模型，将类加载任务先交给父加载器。父加载器（依次为：`AppClassLoader`、`ExtrClassLoader`、`BootStrapCloassLoader`）无法在它的类路径`classpath`下找到`demoA`的类信息，最终还是交给自定义的`ContextClassLoader`来加载。

   ​	

   另外，因为每个项目都新建`ContextClassLoader`，所以即使相同的类在多个项目中被部署多次，因为多个`ContextClassLoader`之间并不是父子关系，所以其内部加载的类的缓存不会覆盖。即同样的类被不同的`contextClassLoader`加载后是不同的类对象。

   

   这样可以避免同样的类，不同的版本，因为在一个项目中被加载了，导致另一个项目中的类直接使用之前加载的类信息，造成错误。

![image-20200623111918125](img/tomcat补充/image-20200623111918125.png)



![image-20200623111958203](img/tomcat补充/image-20200623111958203.png)

![image-20200623112348083](img/tomcat补充/image-20200623112348083.png)

同样的`HelloServlet`不是同一个`Class`：

`demoA`中的`ContextClassLoader`加载出来的类实例

![image-20200623112614106](img/tomcat补充/image-20200623112614106.png)



`demoB`中的`ContextClassLoader`加载出来的类实例

<img src="img/tomcat补充/image-20200623112827000.png" alt="image-20200623112827000" style="zoom:50%;" />



### 遇到的问题：

**1. 使用`UrlClassLoader`读取资源时读取不到****

因为创建`UrlClassLoader`时，传入的`URL`为`C:/users/webapps/`，而`web.xml`在`C:/users/webapps/demoA/web.xml`。所以读不到



**2. 将`Url`设置到`C:/users/webapps/demoA/web.xml`后，读取到的`web.xml`内容不是`demoA`下的，而是手写的`mini_tomcat`下的`web.xml`**

​	**原因：**因为`classLoader.getResourcesAsStream()`也是双亲委派机制，会先将资源交给父`Classloader`（即`AppClassloader`）加载，而`AppClassLoader`的`ClassPath`就在`mini_tomcat`根目录下，所以找到`web.xml`后，就直接返回了结果。

​	**解决方式：**重写`getResourcesAsStream()`，直接调用当前类的`getResourcesAsStream()`实现。因为继承的是`UrlClassLoader`，所以会在传入的`URL`目录下查找`web.xml`。



**3. 读取静态资源的时候报空指针？**

修改了一下寻找映射的逻辑，通过对应的应用的`ContextClassLoader`去它的`ClassPath`下找就可以了。

![image-20200623113006033](img/tomcat补充/image-20200623113006033.png)









## 源码跟踪

#### 源码构建





### 主要流程

#### tomcat容器init流程



#### tomcat容器start流程





### 感兴趣的点

`nio`接收数据流，selector这一系列流程的实现？



tomcat的类加载机制，双亲委派机制，`AppClassLoader`只能加载`classpath`下的类吗？



