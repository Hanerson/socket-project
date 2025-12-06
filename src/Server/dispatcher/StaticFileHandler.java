package Server.dispatcher;

import common.HttpRequest;
import common.HttpResponse;
import utils.MimeTypes; // 确保 MimeTypes 工具类路径正确

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 角色 C：静态资源处理器
 * 职责：处理文件读取、MIME 类型设置、304 缓存协商、405 方法校验、长连接支持
 */
public class StaticFileHandler {

    private final String webRoot;

    public StaticFileHandler(String webRoot) {
        this.webRoot = webRoot;
    }

    public HttpResponse handle(HttpRequest request) {
        HttpResponse response = new HttpResponse();

        // ========== 新增：1. 请求方法校验（核心405逻辑） ==========
        // 静态资源仅支持 GET 方法，其他方法直接返回 405
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            response.setStatusCode(405);
            response.addHeader("Allow", "GET"); // HTTP规范：标识支持的方法
            response.addHeader("Connection", "keep-alive"); // 长连接支持
            response.setStringBody("<h1>405 Method Not Allowed</h1><p>Static resources only support GET method</p>");
            return response;
        }

        // ========== 原有逻辑：安全检查和路径拼接 ==========
        // 防止路径遍历攻击 (e.g. ../../etc/passwd)
        if (request.getUri().contains("..")) {
            response.setStatusCode(403);
            response.addHeader("Connection", "keep-alive");
            response.setStringBody("<h1>403 Forbidden</h1><p>Path traversal is not allowed</p>");
            return response;
        }

        // 默认访问 index.html
        String relPath = request.getUri().equals("/") ? "/index.html" : request.getUri();
        File file = new File(webRoot, relPath);

        // ========== 原有逻辑：文件不存在处理 ==========
        if (!file.exists() || !file.isFile()) {
            response.setStatusCode(404);
            response.addHeader("Connection", "keep-alive");
            response.setStringBody("<h1>404 File Not Found</h1><p>Resource " + relPath + " not found</p>");
            return response;
        }

        // ========== 原有逻辑：准备缓存元数据 ==========
        long lastModified = file.lastModified();
        String etag = "W/\"" + lastModified + "-" + file.length() + "\""; // 简单生成 ETag

        // 日期格式化为 HTTP 标准格式 (GMT)
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String lastModifiedStr = dateFormat.format(new Date(lastModified));

        // ========== 原有逻辑：304 缓存校验逻辑 ==========
        String ifNoneMatch = request.getHeader("If-None-Match");
        String ifModifiedSince = request.getHeader("If-Modified-Since");

        boolean notModified = false;

        // 优先检查 ETag
        if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
            notModified = true;
        }
        // 其次检查 Last-Modified
        else if (ifModifiedSince != null && ifModifiedSince.equals(lastModifiedStr)) {
            notModified = true;
        }

        if (notModified) {
            response.setStatusCode(304);
            response.addHeader("Connection", "keep-alive"); // 长连接支持
            response.addHeader("ETag", etag); // 304响应仍需返回ETag
            response.addHeader("Last-Modified", lastModifiedStr);
            // 304 响应不需要 Body
            return response;
        }

        // ========== 原有逻辑：返回 200 和文件内容（完善版） ==========
        try {
            byte[] content = Files.readAllBytes(file.toPath());
            response.setStatusCode(200);
            response.setBody(content);

            // 设置 MIME 类型（至少支持 text/html、text/css、image/png 三种）
            String contentType = MimeTypes.getContentType(file.getName());
            response.addHeader("Content-Type", contentType);
            // 设置内容长度
            response.addHeader("Content-Length", String.valueOf(content.length));
            // 长连接支持
            response.addHeader("Connection", "keep-alive");
            // 缓存头
            response.addHeader("ETag", etag);
            response.addHeader("Last-Modified", lastModifiedStr);

        } catch (IOException e) {
            e.printStackTrace();
            response.setStatusCode(500);
            response.addHeader("Connection", "keep-alive");
            response.setStringBody("<h1>500 Internal Server Error</h1><p>Failed to read file: " + relPath + "</p>");
        }

        return response;
    }
}