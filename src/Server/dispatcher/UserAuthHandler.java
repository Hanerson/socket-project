package Server.dispatcher;

import common.HttpRequest;
import common.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 角色 C：用户认证处理器
 * 职责：处理 /register 和 /login 的 POST 请求，管理内存中的用户数据 [cite: 208, 210]。
 */
public class UserAuthHandler {

    // 内存数据库
    private final Map<String, String> userDatabase = new ConcurrentHashMap<>();

    public HttpResponse register(HttpRequest request) {
        HttpResponse response = new HttpResponse();

        // 1. 解析参数 [cite: 211]
        String[] credentials = parseBody(request);
        if (credentials == null) {
            response.setStatusCode(400); // Bad Request
            response.setStringBody("Invalid format. Use username=x&password=y");
            return response;
        }
        String username = credentials[0];
        String password = credentials[1];

        // 2. 检查用户是否存在 [cite: 213]
        if (userDatabase.containsKey(username)) {
            response.setStatusCode(409); // Conflict [cite: 215]
            response.setStringBody("User already exists");
        } else {
            userDatabase.put(username, password);
            response.setStatusCode(200);
            response.setStringBody("Register Success");
        }
        return response;
    }

    public HttpResponse login(HttpRequest request) {
        HttpResponse response = new HttpResponse();

        // 1. 解析参数
        String[] credentials = parseBody(request);
        if (credentials == null) {
            response.setStatusCode(400);
            return response;
        }
        String username = credentials[0];
        String password = credentials[1];

        // 2. 校验逻辑 [cite: 214]
        String storedPwd = userDatabase.get(username);
        if (storedPwd != null && storedPwd.equals(password)) {
            response.setStatusCode(200);
            response.setStringBody("Login Success");
        } else {
            response.setStatusCode(401); // Unauthorized
            response.setStringBody("Login Failed: Wrong username or password");
        }
        return response;
    }

    /**
     * 简单的 Body 解析器
     * 假设 Content-Type 为 application/x-www-form-urlencoded
     * 格式：username=abc&password=123
     * 返回: [username, password] 或 null
     */
    private String[] parseBody(HttpRequest request) {
        byte[] bodyBytes = request.getBody();
        if (bodyBytes == null || bodyBytes.length == 0) return null;

        String bodyStr = new String(bodyBytes, StandardCharsets.UTF_8);
        String username = null;
        String password = null;

        // 简单手动解析，避免依赖复杂库
        String[] pairs = bodyStr.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                if ("username".equals(kv[0])) username = kv[1];
                if ("password".equals(kv[0])) password = kv[1];
            }
        }

        if (username != null && password != null) {
            return new String[]{username, password};
        }
        return null;
    }
}