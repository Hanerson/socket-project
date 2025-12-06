package Server.dispatcher;

import common.HttpRequest;
import common.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.Set;

/**
 * 角色 C：用户认证处理器
 * 新增：黑名单机制，黑名单用户登录返回 403 Forbidden
 */
public class UserAuthHandler {

    // 内存数据库（线程安全）
    private final Map<String, String> userDatabase = new ConcurrentHashMap<>();

    // 黑名单用户列表（不可变集合，支持动态添加）
    private final Set<String> blacklist = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // 初始化黑名单（可手动添加测试用户）
    public UserAuthHandler() {
        // 预添加测试黑名单用户
        blacklist.add("blackuser");
        blacklist.add("test_black");
    }

    /**
     * 统一处理注册/登录请求
     */
    public HttpResponse handle(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        response.addHeader("Connection", "keep-alive");
        response.addHeader("Content-Type", "text/html; charset=UTF-8");

        String uri = request.getUri();
        String method = request.getMethod();

        // 注册接口处理
        if ("/register".equals(uri)) {
            if (!"POST".equalsIgnoreCase(method)) {
                response.setStatusCode(405);
                response.addHeader("Allow", "POST");
                response.setStringBody("405 Method Not Allowed<br>Register API only supports POST requests");
                return response;
            }
            try {
                return register(request);
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatusCode(500);
                response.setStringBody("500 Internal Server Error<br>Register failed: " + e.getMessage());
                return response;
            }
        }

        // 登录接口处理（核心：新增黑名单校验）
        if ("/login".equals(uri)) {
            if (!"POST".equalsIgnoreCase(method)) {
                response.setStatusCode(405);
                response.addHeader("Allow", "POST");
                response.setStringBody("405 Method Not Allowed<br>Login API only supports POST requests");
                return response;
            }
            try {
                return login(request);
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatusCode(500);
                response.setStringBody("500 Internal Server Error<br>Login failed: " + e.getMessage());
                return response;
            }
        }

        // 未知接口
        response.setStatusCode(404);
        response.setStringBody("404 Not Found<br>API " + uri + " not found");
        return response;
    }

    /**
     * 注册逻辑（无修改）
     */
    public HttpResponse register(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        response.addHeader("Connection", "keep-alive");
        response.addHeader("Content-Type", "text/html; charset=UTF-8");

        String[] credentials = parseBody(request);
        if (credentials == null) {
            response.setStatusCode(400);
            response.setStringBody("400 Bad Request<br>Invalid format. Use username=x&password=y");
            return response;
        }
        String username = credentials[0];
        String password = credentials[1];

        if (username.isBlank() || password.isBlank()) {
            response.setStatusCode(400);
            response.setStringBody("400 Bad Request<br>Username or password cannot be empty");
            return response;
        }

        if (userDatabase.containsKey(username)) {
            response.setStatusCode(409);
            response.setStringBody("409 Conflict<br>User already exists");
        } else {
            userDatabase.put(username, password);
            response.setStatusCode(200);
            response.setStringBody("200 OK<br>Register Success");
        }
        return response;
    }

    /**
     * 登录逻辑（新增：黑名单校验）
     */
    public HttpResponse login(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        response.addHeader("Connection", "keep-alive");
        response.addHeader("Content-Type", "text/html; charset=UTF-8");

        String[] credentials = parseBody(request);
        if (credentials == null) {
            response.setStatusCode(400);
            response.setStringBody("400 Bad Request<br>Invalid format. Use username=x&password=y");
            return response;
        }
        String username = credentials[0];
        String password = credentials[1];

        if (username.isBlank() || password.isBlank()) {
            response.setStatusCode(400);
            response.setStringBody("400 Bad Request<br>Username or password cannot be empty");
            return response;
        }

        // ========== 核心新增：黑名单校验 ==========
        if (blacklist.contains(username)) {
            response.setStatusCode(403); // 403 禁止访问（区别于 401 密码错误）
            response.setStringBody("403 Forbidden<br>User " + username + " is in blacklist, login denied");
            return response;
        }

        // 原有密码校验逻辑
        String storedPwd = userDatabase.get(username);
        if (storedPwd != null && storedPwd.equals(password)) {
            response.setStatusCode(200);
            response.setStringBody("200 OK<br>Login Success");
        } else {
            response.setStatusCode(401);
            response.setStringBody("401 Unauthorized<br>Login Failed: Wrong username or password");
        }
        return response;
    }

    // ========== 可选：黑名单管理方法（扩展用） ==========
    /**
     * 添加用户到黑名单
     */
    public void addToBlacklist(String username) {
        if (username != null && !username.isBlank()) {
            blacklist.add(username);
        }
    }

    /**
     * 从黑名单移除用户
     */
    public void removeFromBlacklist(String username) {
        blacklist.remove(username);
    }

    /**
     * 解析请求体（无修改）
     */
    private String[] parseBody(HttpRequest request) {
        byte[] bodyBytes = request.getBody();
        if (bodyBytes == null || bodyBytes.length == 0) return null;

        String bodyStr = new String(bodyBytes, StandardCharsets.UTF_8);
        String username = null;
        String password = null;

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