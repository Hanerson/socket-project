# HTTP Server - Role C 功能测试文档 (v1.0)

**测试目标**：验证 `RequestDispatcher`、`StaticFileHandler`、`UserAuthHandler` 及 `MimeTypes` 组件的功能正确性。
**测试环境**：Windows CMD (命令提示符)
**前置条件**：

1.  服务器已启动 (运行 `IntegrationTest` 或 `SimpleHttpServer`)，监听端口 `8080`。
2.  项目根目录下存在 `webroot` 文件夹，且其中包含 `index.html` 文件（内容任意，如 "Hello Role C"）。

-----

## 1\. 静态资源测试 (StaticFileHandler)

### 1.1 获取存在的静态文件 (GET 200)

[cite_start]验证服务器能否正确读取文件、设置 Content-Type 并返回内容 [cite: 198, 204]。

* **命令**：
  ```cmd
  curl -v http://localhost:8080/index.html
  ```
* **预期结果**：
    * 状态行：`HTTP/1.1 200 OK`
    * 响应头：包含 `Content-Type: text/html` (验证 MimeTypes 生效)
    * 响应头：包含 `ETag` 和 `Last-Modified`
    * 响应体：显示 `index.html` 的文件内容 (如 "Hello Role C")

### 1.2 获取不存在的文件 (GET 404)

[cite_start]验证文件不存在时的错误处理 [cite: 200]。

* **命令**：
  ```cmd
  curl -v http://localhost:8080/not_exist_file.html
  ```
* **预期结果**：
    * 状态行：`HTTP/1.1 404 Not Found`

### 1.3 缓存协商 (GET 304)

[cite_start]验证当客户端携带缓存标识时，服务器能否正确返回 304 状态码 [cite: 201, 202]。

* **步骤 1 (获取 ETag)**：
  先执行 `curl -v http://localhost:8080/index.html`，找到响应头中的 `ETag` 值（例如 `W/"173000-50"`）。
* **步骤 2 (带 ETag 请求)**：
  将获取的 ETag 填入下方命令（**注意：CMD 中内部的双引号需要用 `\` 转义**）：
  ```cmd
  curl -v -H "If-None-Match: W/\"替换为你的ETag数字\"" http://localhost:8080/index.html
  ```
  *(示例：如果 ETag 是 `W/"12345"`, 则写为 `W/\"12345\"`)*
* **预期结果**：
    * 状态行：`HTTP/1.1 304 Not Modified`
    * **无响应体** (不显示 HTML 内容)

-----

## 2\. 用户认证 API 测试 (UserAuthHandler)

### 2.1 用户注册 - 成功 (POST 200)

[cite_start]验证注册逻辑及参数解析 [cite: 211, 213]。

* **命令**：
  ```cmd
  curl -v -d "username=testuser&password=123" http://localhost:8080/register
  ```
* **预期结果**：
    * 状态行：`HTTP/1.1 200 OK`
    * 响应体：`Register Success`

### 2.2 用户注册 - 重复冲突 (POST 409)

[cite_start]验证内存数据库是否检测到用户已存在 [cite: 213, 215]。

* **命令**：
  再次执行上述相同的注册命令。
* **预期结果**：
    * 状态行：`HTTP/1.1 409 Conflict` (或代码中设置的状态码)
    * 响应体：`User already exists`

### 2.3 用户登录 - 成功 (POST 200)

[cite_start]验证密码校验逻辑 [cite: 214]。

* **命令**：
  ```cmd
  curl -v -d "username=testuser&password=123" http://localhost:8080/login
  ```
* **预期结果**：
    * 状态行：`HTTP/1.1 200 OK`
    * 响应体：`Login Success`

### 2.4 用户登录 - 失败/密码错误 (POST 401)

[cite_start]验证错误凭证的处理 [cite: 214]。

* **命令**：
  ```cmd
  curl -v -d "username=testuser&password=wrongpass" http://localhost:8080/login
  ```
* **预期结果**：
    * 状态行：`HTTP/1.1 401 Unauthorized`

-----

## 3\. 路由重定向测试 (RequestDispatcher)

### 3.1 页面重定向 (GET 301)

[cite_start]验证特定路径的跳转逻辑 [cite: 182, 183]。

* **命令**：
  ```cmd
  curl -v http://localhost:8080/old-page
  ```
* **预期结果**：
    * 状态行：`HTTP/1.1 301 Moved Permanently` (或其他重定向状态码)
    * 响应头：`Location: /index.html`

-----

## 4\. 常见问题排查 (Troubleshooting)

1.  **浏览器能访问，但 curl 报 404**：

    * **原因**：使用了 `curl -I`。该命令发送 `HEAD` 请求，而当前的 `RequestDispatcher` 仅处理了 `GET` 和 `POST`，导致 `HEAD` 请求落入 404 兜底逻辑。
    * **解决**：使用 `curl -v` (GET) 进行测试。

2.  **POST 请求报 404**：

    * **原因**：`dispatch` 方法中对 Method 的判断大小写不匹配（例如代码用 `POST` 而请求是 `post`），或 URL 拼写错误。
    * **解决**：确保代码中使用 `.toUpperCase()` 处理方法名，且 curl 命令 URL 正确。

3.  **CMD 中 304 测试失败 (返回 200)**：

    * **原因**：ETag 格式错误，Windows CMD 处理双引号时未转义。
    * **解决**：确保 `If-None-Match` 的值中，引号前加了反斜杠，例如 `\"value\"`。