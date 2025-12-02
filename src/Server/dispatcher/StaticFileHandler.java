package Server.dispatcher;

import common.HttpRequest;
import common.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 角色 C：静态资源处理器
 * 职责：处理文件读取、MIME 类型设置、以及最重要的 304 缓存协商 [cite: 197]。
 */
public class StaticFileHandler {

    private final String webRoot;

    public StaticFileHandler(String webRoot) {
        this.webRoot = webRoot;
    }

    public HttpResponse handle(HttpRequest request) {
        HttpResponse response = new HttpResponse();

        // 1. 安全检查和路径拼接
        // 防止路径遍历攻击 (e.g. ../../etc/passwd)
        if (request.getUri().contains("..")) {
            response.setStatusCode(403);
            return response;
        }

        // 默认访问 index.html
        String relPath = request.getUri().equals("/") ? "/index.html" : request.getUri();
        File file = new File(webRoot, relPath);

        // 2. 文件不存在处理 [cite: 200]
        if (!file.exists() || !file.isFile()) {
            response.setStatusCode(404);
            response.setStringBody("<h1>404 File Not Found</h1>");
            return response;
        }

        // 3. 准备缓存元数据 (Last-Modified / ETag)
        long lastModified = file.lastModified();
        String etag = "W/\"" + lastModified + "-" + file.length() + "\""; // 简单生成 ETag

        // 日期格式化为 HTTP 标准格式 (GMT)
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String lastModifiedStr = dateFormat.format(new Date(lastModified));

        // 4. **核心：304 缓存校验逻辑** [cite: 201, 202]
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
            response.setStatusCode(304); // [cite: 202]
            // 304 响应不需要 Body
            return response;
        }

        // 5. 如果未命中缓存，返回 200 和文件内容 [cite: 203]
        try {
            byte[] content = Files.readAllBytes(file.toPath());
            response.setStatusCode(200);
            response.setBody(content);

            String contentType = utils.MimeTypes.getContentType(file.getName());

            // 设置缓存头 [cite: 205]
            response.addHeader("ETag", etag);
            response.addHeader("Last-Modified", lastModifiedStr);

        } catch (IOException e) {
            e.printStackTrace();
            response.setStatusCode(500);
        }

        return response;
    }
}