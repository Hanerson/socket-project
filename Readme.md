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