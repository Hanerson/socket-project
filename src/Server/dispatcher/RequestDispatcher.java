package Server.dispatcher;

import common.HttpRequest;
import common.HttpResponse;

/**
 * [临时替身] Role C 的分发器
 * 目前仅用于测试 Role B 是否能正常接收请求并调用分发器。
 */
public class RequestDispatcher {

    public HttpResponse dispatch(HttpRequest request) {
        // 这是一个假的实现，无论请求什么，都返回 200 OK
        HttpResponse response = new HttpResponse();
        response.setStatusCode(200);
        response.setStatusMessage("OK");
        String content = "<h1>Server is working!</h1><p>Request received: " + request.getUri() + "</p>";
        response.setBody(content.getBytes());
        return response;
    }
}