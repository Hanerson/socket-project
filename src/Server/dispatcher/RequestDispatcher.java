package Server.dispatcher;

import common.HttpRequest;
import common.HttpResponse;

/**
 * 角色 C：请求分发器
 * 职责：根据请求的方法和 URI，将请求分发给对应的处理器 [cite: 164, 174]。
 */
public class RequestDispatcher {

    private final StaticFileHandler fileHandler;
    private final UserAuthHandler authHandler;

    public RequestDispatcher() {
        this.fileHandler = new StaticFileHandler("res");
        this.authHandler = new UserAuthHandler();
    }

    public HttpResponse dispatch(HttpRequest request) {
        String method = request.getMethod().toUpperCase();
        String uri = request.getUri();

        try {
            // 1. 处理 GET 请求
            if ("GET".equals(method)) {
                // 模拟重定向逻辑 (文档 Source 182)
                if ("/old-page".equals(uri)) {
                    HttpResponse response = new HttpResponse();
                    response.setStatusCode(301);
                    response.addHeader("Location", "/index.html");
                    return response;
                }else{
                    if("/temp-page".equals(uri)){
                        HttpResponse response = new HttpResponse();
                        response.setStatusCode(302);
                        response.addHeader("Location", "/index.html");
                        return response;
                    }
                }

                // 默认走静态资源处理 (文档 Source 180)
                // 这里简单判断：如果是注册/登录的 API 路径则不走这里，其余都当静态文件
                if (!"/register".equals(uri) && !"/login".equals(uri)) {
                    return fileHandler.handle(request);
                }else{
                    HttpResponse response = new HttpResponse();
                    response.setStatusCode(405);

                    response.addHeader("Connection", "keep-alive");
                    response.addHeader("Allow", "POST");
                    response.setStringBody("405 Method Not Allowed<br>This API only supports POST requests");
                    response.addHeader("Content-Type", "text/html; charset=UTF-8");

                    return response;
                }
            }
            // 2. 处理 POST 请求 (注册/登录)
            else if ("POST".equals(method)) {
                if ("/register".equals(uri)) {
                    return authHandler.register(request); // [cite: 187]
                } else if ("/login".equals(uri)) {
                    return authHandler.login(request);    // [cite: 189]
                }else{
                    HttpResponse response = new HttpResponse();
                    response.setStatusCode(405);

                    response.addHeader("Connection", "keep-alive");
                    response.addHeader("Allow", "POST");
                    response.setStringBody("405 Method Not Allowed<br>This API only supports GET requests");
                    response.addHeader("Content-Type", "text/html; charset=UTF-8");

                    return response;
                }
            }

            // 3. 兜底：未匹配到任何路由，返回 404 或 405 [cite: 192]
            HttpResponse response = new HttpResponse();
            response.setStatusCode(404);
            response.setStringBody("404 Not Found");
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            HttpResponse response = new HttpResponse();
            response.setStatusCode(500);
            response.setStringBody("500 Internal Server Error");
            return response;
        }
    }
}