

# Netty

## 可以做什么？

1. `http`容器，不是标准的`servlet`规范
2. socket开发
3. 长链接开发



## Netty 快速上手

### 1. 使用`netty`做`http`服务器

 这里是看视频中的简单`demo`，跟着写了一下。



创建`gradle`项目，引入`netty`：

```yaml
compile group: 'io.netty', name: 'netty-all', version: '4.1.42.Final'	
```

![image-20200701185438099](img/Netty/image-20200701185438099.png)



写一个启动类：

```java
public class MyHttpServer {


    public static void main(String[] args) throws InterruptedException {
        //使用两个线程池来接收请求、处理请求
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();


        try {
            //创建启动辅助类
            ServerBootstrap bootStrap = new ServerBootstrap();

            //定制内容
            bootStrap.group(bossGroup,workerGroup)
                    //设置通道类型
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MyHttpInitializer());

            //绑定端口
            ChannelFuture future = bootStrap.bind(8899).sync();
            future.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
```

其中`MyHttpInitializer`是自定义的`handler`

```java
public class MyHttpInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //获取管道
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("httpServerCodec",new HttpServerCodec());

        //添加自己定义的处理器
        pipeline.addLast("myHttpServerHandler",new MyHttpServerHandler());

    }
}
```

`MyHttpInitializer`中使用到了`netty`提供的`http`的处理器，这些处理器就类似过滤器。这里自定义一个自己的`handler`：

```java
//针对http来说，泛型为 HttpObject
public class MyHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if(msg instanceof HttpRequest){
            //定义一个响应内容
            ByteBuf content = Unpooled.copiedBuffer("Hello world", Charset.forName("UTF-8"));

            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,content);
            //设置http响应头
            response.headers().set(HttpHeaderNames.CONTENT_TYPE,HttpHeaderValues.TEXT_PLAIN);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH,content.readableBytes());
            //响应
            ctx.writeAndFlush(response);
        }
    }
}
```

 

将程序运行起来，并访问`http://localhost:8899`就可以看到结果：

![image-20200701192633160](img/Netty/image-20200701192633160.png)

### 2. 使用`netty`做`socket`编程

`netty`的编程流程基本是一样的，只是在不同的模型中使用到的的对象不同而已。编写`socket`通信程序，将上面`http`中的几个类拿出来改造下即可。

```java
public class MySocketServer {


    public static void main(String[] args) throws InterruptedException {
        //使用两个线程池来接收请求、处理请求
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();


        try {
            //创建启动辅助类
            ServerBootstrap bootStrap = new ServerBootstrap();

            //定制内容
            bootStrap.group(bossGroup,workerGroup)
                    //设置通道类型
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MySocketInitializer());

            //绑定端口
            ChannelFuture future = bootStrap.bind(8899).sync();
            future.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
```

```java
public class MySocketInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //获取管道
        ChannelPipeline pipeline = ch.pipeline();
        //添加netty提供的编解码handler
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
        pipeline.addLast(new LengthFieldPrepender(4));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());
        //这里是自定义的handler
        pipeline.addLast(new MyScoketServerHandler());
    }
}
```

```java
//这里使用socket和客户端交互使用字符串
public class MyScoketServerHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("Server received msg: "+msg);
        String content = "hello , your id is "+ UUID.randomUUID().toString();
        ctx.channel().writeAndFlush(content);
    }
}
```



`socket`和`http`不同的是，无法使用浏览器直接调用，所以需要编写客户端。客户端和服务端也大体相似，只是启动类变为`BootStrap`，且不再是绑定端口，而是链接端口。

```java
public class MyClient {



    public static void main(String[] args) throws InterruptedException {
        //客户端只需要一个线程池来链接服务端即可
        NioEventLoopGroup clientGroup = new NioEventLoopGroup();


        try {
            //客户端的启动类为bootStrap
            Bootstrap bootStrap = new Bootstrap();

            MyClientInitializer initializer = new MyClientInitializer();
            //定制内容
            bootStrap.group(clientGroup)
                    //设置通道类型，这里不是NioServerSocketChannel
                    .channel(NioSocketChannel.class)
                    //客户端只有一个线程池，使用handler去对应
                    .handler(initializer);


            //这里是连接端口8899，而不是绑定了
            ChannelFuture future = bootStrap.connect("localhost",8899).sync();
            //这里需要想服务端发送一个消息来打破平静，否则客户端和服务端都等待对方给与消息。
            future.channel().writeAndFlush("1111");
            future.channel().closeFuture().sync();

        }finally {
            clientGroup.shutdownGracefully();
        }

    }
}
```

```java
public class MyClientInitializer extends ChannelInitializer<SocketChannel> {


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //获取管道
        ChannelPipeline pipeline = ch.pipeline();
        //添加netty提供的编解码handler
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
        pipeline.addLast(new LengthFieldPrepender(4));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());
        pipeline.addLast(new MyClientHandler());
    }
}
```



```java
//这里和服务端交互使用字符串
public class MyClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected synchronized void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("get Server msg: "+msg);
        System.out.println("client : "+ LocalDateTime.now());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
```

依次启动服务端和客户端，可以看到结果：

![image-20200701235842242](img/Netty/image-20200701235842242.png)



![image-20200701235938137](img/Netty/image-20200701235938137.png)





### 3. 使用`netty`实现多个客户端的即时通讯

该案例实现了多个客户端之间的聊天

#### 服务端

```java
public class MySocketChatServer {


    public static void main(String[] args) throws InterruptedException {
        //使用两个线程池来接收请求、处理请求
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //创建启动辅助类
            ServerBootstrap bootStrap = new ServerBootstrap();

            //定制内容
            bootStrap.group(bossGroup,workerGroup)
                    //设置通道类型
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MySocketChatInitializer());

            //绑定端口
            ChannelFuture future = bootStrap.bind(8899).sync();
            future.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
```

初始化器中使用的是分隔符解码器

```java
public class MySocketChatInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //获取管道
        ChannelPipeline pipeline = ch.pipeline();
        //添加netty提供的分隔符解码器
        pipeline.addLast(new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()));
        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
        pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
        pipeline.addLast(new MyScoketChatServerHandler());
    }
}
```



自定义处理器中使用`ChannelGroup`来记录加入连接的通道，用于在一个用户说话时将消息发给其他用户。

```java
//这里使用socket和客户端交互使用字符串
public class MyScoketChatServerHandler extends SimpleChannelInboundHandler<String> {

    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.forEach(ch -> {
            if(ch != channel){
                //writeAndFlush输出的内容必须加换行符，否则无法被DelimiterBasedFrameDecoder解析，客户端就收不到
                ch.writeAndFlush(ctx.channel().remoteAddress()+"说："+msg+"\n");
            }else {
                ch.writeAndFlush("我说："+msg+"\n");
            }
        });
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String msg= ctx.channel().remoteAddress()+"上线了";
        System.out.println(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String msg= ctx.channel().remoteAddress()+"下线了";
        System.out.println(msg);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String msg= channel.remoteAddress()+"加入链接\n";
        channelGroup.writeAndFlush(msg);
        channelGroup.add(channel);
        System.out.println(msg);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String msg= channel.remoteAddress()+"离开链接";
        channelGroup.writeAndFlush(msg);
        System.out.println(msg);
        //会自动移除
//        channelGroup.remove(ctx.channel());
    }
}
```



#### 客户端

客户端中要录入键盘信息来聊天，因此用了一个死循环。需要注意在消息后加`/r/n`来做分隔。

```java
public class MyChatClient {

    public static void main(String[] args) throws InterruptedException, IOException {
        //客户端只需要一个线程池来链接服务端即可
        NioEventLoopGroup clientGroup = new NioEventLoopGroup();
        try {
            //客户端的启动类为bootStrap
            Bootstrap bootStrap = new Bootstrap();

            MyClientChatInitializer initializer = new MyClientChatInitializer();
            //定制内容
            bootStrap.group(clientGroup)
                    //设置通道类型，这里不是NioServerSocketChannel
                    .channel(NioSocketChannel.class)
                    //客户端只有一个线程池，使用handler去对应
                    .handler(initializer);
            //链接端口
            ChannelFuture future = bootStrap.connect("localhost",8899).sync();
//            future.channel().closeFuture().sync();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true){
                String s = reader.readLine();
                if("stop".equals(s)){break;}
                //最后必须加换行符，否则服务端无法解析读取
                future.channel().writeAndFlush(s+"\r\n");
            }
        }finally {
            clientGroup.shutdownGracefully();
        }

    }
}
```



客户端也要使用分隔符解码器来对应

```java
public class MyClientChatInitializer extends ChannelInitializer<SocketChannel> {


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //获取管道
        ChannelPipeline pipeline = ch.pipeline();
        //添加netty提供的分隔符解码器
        pipeline.addLast(new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()));
        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
        pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
        pipeline.addLast(new MyClientChatHandler());
    }
}
```



自定义处理器直接打印发过来的消息即可。

```java
public class MyClientChatHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg);
    }
}
```



### 4. 使用`netty`做心跳检测

服务端基本一致

```java
public class MySocketServer {

    //客户端使用chat的就行
    public static void main(String[] args) throws InterruptedException {
        //使用两个线程池来接收请求、处理请求
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();


        try {
            //创建启动辅助类
            ServerBootstrap bootStrap = new ServerBootstrap();

            //定制内容
            bootStrap.group(bossGroup,workerGroup)
                    //设置通道类型
                    .channel(NioServerSocketChannel.class)
                    //打印boosGroup的日志
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new MyHeartbeatInitializer());

            //绑定端口
            ChannelFuture future = bootStrap.bind(8899).sync();
            future.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
```



初始化器引入间隔处理器

```java
public class MyHeartbeatInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //获取管道
        ChannelPipeline pipeline = ch.pipeline();
        //添加间隔时间拦截
        // 读空闲5s，写空闲7s，读写空闲10s
       pipeline.addLast(new IdleStateHandler(5,7,10, TimeUnit.SECONDS));
       pipeline.addLast(new MyHeartbeatHandler());
    }
}
```



```java
//继承的类不一样了
public class MyHeartbeatHandler extends ChannelInboundHandlerAdapter {

    //重写的用户事件触发方法
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            String result = null;
            switch (event.state()){
                case READER_IDLE://读空闲
                    result = "读空闲";
                    break;
                case WRITER_IDLE:
                    result = "写空闲";
                    break;
                case ALL_IDLE:
                    result = "读写空闲";
                    break;
            }
            System.out.println("地址："+ctx.channel().remoteAddress()+result+"超时");
            ctx.channel().closeFuture();
        }
    }
}
```

客户端使用聊天的来测试：

![image-20200708214621408](img/Netty/image-20200708214621408.png)





### 5. 使用`netty`实现`webSocket`

`websocket`是基于`http`的长链接协议，使用`websocket`可以轻松实现客户端和服务端的双工通信。`websocket`连接一旦建立，客户端和服务端就是对等的。

`netty`对`websocket`也提供了很好的支持。

#### 服务端 

服务器仍然还是基本没有变化。

```java
public class MySocketChatServer {
    public static void main(String[] args) throws InterruptedException {
        //使用两个线程池来接收请求、处理请求
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //创建启动辅助类
            ServerBootstrap bootStrap = new ServerBootstrap();

            //定制内容
            bootStrap.group(bossGroup,workerGroup)
                    //设置通道类型
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new MySocketChatInitializer());
            //绑定端口
            ChannelFuture future = bootStrap.bind(8899).sync();
            future.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
```





```java
public class MySocketChatInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //获取管道
        ChannelPipeline pipeline = ch.pipeline();
        //webscoket是基于http的，所以引入http的编解码器
        pipeline.addLast(new HttpServerCodec());
        // 以块的方式去写
        pipeline.addLast(new ChunkedWriteHandler());
        //添加消息聚合器
        pipeline.addLast(new HttpObjectAggregator(406));
        //针对websocket的处理器，/ws 就是项目名称
        // 比如： ws://localhost:port/项目名
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        pipeline.addLast(new TextWebsocketFrameHandler());
    }
}
```



这里的泛型是`TextWebSocketFrame`，眼熟一下即可。

```java

public class TextWebsocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        System.out.println("服务器收到消息："+msg.text());
        //这里返回的内容需要包装到TextWebSocketFrame中
        ctx.channel().writeAndFlush(new TextWebSocketFrame("服务器收到时间："+ LocalDateTime.now()));
    }
    
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //channel.asLongText 对应的是长id，能够保证是不重复的
        System.out.println("handlerAdded:"+ctx.channel().id().asLongText());
    }
    
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerRemoved:"+ctx.channel().id().asLongText());
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("发生异常");
        cause.printStackTrace();
        ctx.channel().closeFuture();
    }
}
```



### 6. `netty`和`protobuf`联合

`protobuf`是用于结构化数据的，与平台，语言无关、可扩展性强的序列化工具。

要使用`protobuf`，需要先下载`protoc`，它可以读取自定义的`.proto`文件，然后帮我们生成相应语言的对象。



1. [下载`protoc`](https://github.com/protocolbuffers/protobuf/releases)，找到`protoc -版本- win64.zip`，解压到自定义目录
2. 在环境变量中将`bin`目录加入
3. 打开`powerShell`，输入`protoc --version`，查看到版本信息及配置好了。
4. 在项目中建一个`protobuf`目录用于存放`*.proto`文件，该文件是遵循`protobuf`的语法规则来定义需要的对象的。这里先试用起来，简单了解下即可。
5. 在目录中建立一个`Student.proto`文件：

```protobuf
# protobuf版本
syntax = "proto2";

# 定义包名，不是java中的包名，而是所有语言都需要定义这个包名。防止多语言情况下冲突
package distribute_practice;

# 如果是java语言定义这个，那么会以这个为准
option java_package = "com.lomo.distribute_practice";
# 生成的外层类的名称。这里说的外层类是因为，protobuf把一个proto文件中生成的类都当做内部类，所以最外层有# # 个外层类的概念
option java_outer_classname = "UserInfo";

# message是Proto定义类的关键字
# optional表示可选的，另外还有required、repeated
# 这里的 =1 ，=2 不是赋值的意思，可以理解为对这个个变量进行编号
message Student {
  optional string name = 1;
  optional int32 age = 2;
  optional string grade = 3;
}

```

6. 在项目中引入`protobuf-java`

![image-20200708231824427](img/Netty/image-20200708231824427.png)











### 7. `netty`和`Thrift`联合

#### `Thrift`实现rpc调用



#### `Thrift`简介

##### （应用层）传输方式：

* `TSocket` - 阻塞式`socket`
* `TFramedTransport`- 以`frame`单位进行传输，非阻塞式服务中使用
* `TFileTransport`- 以文件形式进行传输
* `TMemoryTransport`- 将内存用于`I/O`，`java`实现时内部实际使用了简单的`ByteArrayOutputStream`
* `TZlibTransport`- 使用`zlib`进行压缩，与其他传输方式联合使用。当前无`java`实现



##### 支持的服务模型：

* `TSimpleServer` - 简单的单线程服务模型，常用于测试
* `TThreadPoolServer` - 多线程服务模型，使用标准的阻塞式IO
* `TNonblockingServer` - 多线程服务模型，使用非阻塞式IO（需使用`TFramedTransport`数据传输方式）
* `THsHaServer` - `THsHa`引入了线程池去处理，其模型把读写任务放到线程池去处理；`Half-sync/Half-async`的处理模式，`Half-aysnc`是在处理IO时间上（`accept/read/write io`），`Half-sync`用于handler对rpc的同步处理



##### 传输格式：

* `TBinaryProtocol` - 二进制格式
* `TCompactProtocol` - 压缩模式
* `TJSONProtocol` - JSON格式
* `TSimpleJSONProtocol` - 提供JSON只写协议，生成的文件很容易通过脚本语言解析 
* `TDebugProtocol` - 使用易懂的刻度的文本格式，以便于debug











