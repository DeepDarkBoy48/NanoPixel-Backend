# 代理设置
```java
        //这行代码设置了一个 HTTP 代理：
        Proxy proxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress("127.0.0.1",7892));
        //这里使用了 OkHttpClient 的构建器来设置 HTTP 请求代理：
        // 所有通过这个客户端发出的 HTTP 请求都会通过 127.0.0.1:7892 代理。
        OkHttpClient.Builder builder2 = new OkHttpClient.Builder().proxy(proxy);
        //WebSocketFactory 实例，它用于构建 WebSocket 连接,也要设置代理
        WebSocketFactory factory = new WebSocketFactory();
        factory.getProxySettings().setHost("127.0.0.1").setPort(7892);
```

@PostConstruct 方法中执行代码：把 bot 的初始化代码放到 @PostConstruct 注解的方法中，以确保在 Spring Boot 启动后执行

# modellist
gemini flash thinking exp 01-21
```aiexclude
gemini-2.0-flash-thinking-exp-01-21
```
# gemini-2.0-flash-lite
```aiexclude
gemini-2.0-flash-lite
```

# gemini-2.0-pro-exp-02-05
```aiexclude
gemini-2.0-pro-exp-02-05
```

#  gemini-2.0-flash
```
gemini-2.0-flash
```
