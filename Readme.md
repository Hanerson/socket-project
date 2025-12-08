# Basic-Java-Socket

---
# A部分

---

## API 说明

### 1. `HttpRequest(InputStream in): common.HttpRequest`

#### 功能
- 解析原始 HTTP 请求（支持 POST/GET 等所有标准格式）
- 使用单一 `BufferedInputStream` 按字节流方式读取请求头和请求体，规避 `BufferedReader` 的预读问题
- 支持 `Content-Length` 方式 body 读取
- 不支持 `Transfer-Encoding: chunked`（如遇将抛出异常）
- 头部采用大小写不敏感的 `TreeMap`
#### 用法示例
```java
HttpRequest req = new HttpRequest(in);
// 获取方法、URI、协议、头、体
String method = req.getMethod();
String uri = req.getUri();
String version = req.getHttpVersion();
Map<String, String> headers = req.getHeaders();
byte[] body = req.getBody();
String host = req.getHeader("Host");
```
#### 设计说明
- 能优雅处理空行、无效头部等格式问题
- 构造异常时明确区分 IO 错误和解析错误
- 推荐在解析阶段发现异常直接返回 400

---
### 2. `HttpResponse: common.HttpResponse`

#### 功能
- 表示和构建一个可变 HTTP 响应（包括状态行、头、body）
- 支持 `setStatusCode`, `setStatusMessage`, `addHeader`, `setBody`
- 自动为 body 设置/覆盖 `Content-Length`
- `write(OutputStream)` 可把完整 HTTP 响应输出到流，包括状态行、所有头、空行和 body
#### 用法示例
```java
HttpResponse resp = new HttpResponse();
resp.setStatusCode(200);
resp.setBody("Hello".getBytes(StandardCharsets.UTF_8));
        resp.addHeader("Connection", "close");
// 输出到 socket
resp.write(socket.getOutputStream());
```
#### 设计说明
- 状态码自动带常用 Reason Phrase
- 支持自定义 HTTP 版本，所有 header 均手动添加
- 无 Content-Length 时自动补齐（默认为 0）

---
### 3. `HttpParseException: common.HttpParseException`

#### 功能
- 标识 HTTP 请求解析中的逻辑/格式错误，而非 IO 层异常
- 用于请求行、头格式、content-length、chunked 编码等协议级错误
#### 用法示例
```java
try {
HttpRequest req = new HttpRequest(in);
} catch (HttpParseException ex) {
        // 返回 400 Bad Request 响应
        }
```
#### 设计说明
- 继承自 `Exception`，要求业务层显式 catch
- 支持多种构造方式、附带上下文信息和原始异常

---
### 4. `CommonProtocolTest: common.CommonProtocolTest`
#### 文件开头

#### 功能
- 轻量测试程序（并非 JUnit），用于手工验证 `HttpRequest`/`HttpResponse` 主流程
- `testParsePost()` 用于解析 HTTP POST
- `testResponseWrite()` 演示一个简单响应如何序列化输出
#### 用法示例
执行 main 方法看到解析和序列化的中间结果

---
# B部分

---

## API 说明

### 1. `SimpleHttpServer: Server.SimpleHttpServer`

#### 功能
- 服务器主入口，负责初始化并启动 ServerSocket 监听指定端口
- 内部维护一个固定大小的线程池 (ExecutorService) 以管理并发连接
- 实现主循环：阻塞接收客户端连接 (accept) 并将其封装为 ConnectionHandler 任务提交给线程池执行
- 负责 Role C (RequestDispatcher) 的生命周期管理
- 异常边界处理：确保 accept 过程中的 IO 异常（如连接重置）只记录日志而不导致服务器崩溃

#### 用法示例
```java
// 内部会自动初始化 RequestDispatcher 和 线程池
SimpleHttpServer server = new SimpleHttpServer(80035);
server.start(); // 进入阻塞循环，服务器开始运行
```

#### 设计说明
- 采用 BIO (阻塞IO) + 线程池 模型，有效避免单线程阻塞，支持多客户端同时访问

---

### 2. `ConnectionHandler: Server.ConnectionHandler`

#### 功能
- 实现 Runnable 接口，作为独立单元在线程池中运行，处理单个 Socket 连接
- 核心实现 HTTP/1.1 Keep-Alive (长连接) 机制，在循环中处理多次 "请求-响应" 交互，直到断开
- 设置 Socket 超时 (setSoTimeout)，防止空闲连接长期占用服务器资源
- 协调工作流：调用 Role A 解析请求 -> 调用 Role C 分发业务 -> 调用 Role A 写回响应
- 异常隔离：单个连接内的解析错误或网络中断只影响当前线程，不影响主服务器运行

#### 用法示例
```java
// 此类通常由 SimpleHttpServer 内部实例化及调用
Socket clientSocket = serverSocket.accept();
ConnectionHandler handler = new ConnectionHandler(clientSocket, dispatcher);
// 提交给线程池
threadPool.execute(handler);
```

#### 设计说明
- 完整生命周期管理：使用 try-finally 块确保无论发生异常还是正常退出，Socket 最终都会被关闭
- 智能断开策略：根据 SocketTimeoutException（超时）或请求头中的 Connection: close 决定是否跳出 Keep-Alive 循环
---
# C部分

---
## API 说明

### 1. Executor

#### 功能 
- 实现抽象类AbstractExecutor
- 不同类的Executor实现对不同情况的URL method处理
- handle方法实现根据给出的request返回对应的respond

#### 用法示例

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
    public abstract HttpResponse handle (HttpRequest request) throws Exception;

### 2.Template

#### 功能
- 给出不同状态码的模板化处理
-具体包括 200 301 302 304 400 404 405 500
### 3.RequestDispatcher

#### 功能
- 根据request 分配executor
- 进行404 405 状态码处理
