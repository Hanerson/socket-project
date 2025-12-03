package Client;

import common.HttpRequest;
import common.HttpResponse;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static common.HttpRequest.readFixedBytes;
import static common.HttpRequest.readLine;

/**
 * HTTP 客户端，实现：
 *  - 发送简单的 HTTP 请求
 *  - 支持 301/302 自动重定向
 *  - 支持基于 ETag 的 304 缓存
 */
public class SimpleHttpClient {

    /** URI -> ETag */
    private final Map<String, String> eTagCache = new ConcurrentHashMap<>();
    /** URI -> 响应 body 缓存（配合 ETag 使用） */
    private final Map<String, byte[]> bodyCache = new ConcurrentHashMap<>();

    /** 最多允许的重定向次数 */
    private final int maxRedirects = 5;

    /**
     * 发送 HTTP 请求，并自动处理重定向和缓存。
     *
     * @param request 要发送的 HttpRequest（至少要包含 Host 头）
     * @return 最终的 HTTP 响应
     */
    public HttpResponse send(HttpRequest request) throws Exception {
        HttpRequest currentRequest = request;

        for (int i = 0; i < maxRedirects; i++) {
            // ===== 1. 缓存检查（只对 GET 生效） =====
            String method = currentRequest.getMethod();
            String uri = currentRequest.getUri();
            String version = currentRequest.getHttpVersion();

            Map<String, String> sendHeaders = new HashMap<>(currentRequest.getHeaders());
            byte[] body = currentRequest.getBody();

            if ("GET".equalsIgnoreCase(method)) {
                String cachedEtag = eTagCache.get(uri);
                if (cachedEtag != null) {
                    sendHeaders.put("If-None-Match", cachedEtag);
                }
            }

            if (body != null && body.length > 0 && !sendHeaders.containsKey("Content-Length")) {
                sendHeaders.put("Content-Length", String.valueOf(body.length));
            }
            // 默认短连接
            sendHeaders.putIfAbsent("Connection", "close");

            // ===== 2. 建立 Socket 连接 =====
            String hostHeader = sendHeaders.get("Host");
            if (hostHeader == null || hostHeader.isEmpty()) {
                throw new IllegalArgumentException("Host header is required in HttpRequest");
            }
            String host = hostHeader;
            int port = 80;
            int idx = hostHeader.indexOf(':');
            if (idx >= 0) {
                host = hostHeader.substring(0, idx);
                port = Integer.parseInt(hostHeader.substring(idx + 1));
            }

            try (Socket socket = new Socket(host, port)) {
                // ===== 3. 发送请求报文 =====
                OutputStream out = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(out, false, StandardCharsets.ISO_8859_1);

                // 请求行
                writer.print(method + " " + uri + " " + version + "\r\n");
                // 头部
                for (Map.Entry<String, String> e : sendHeaders.entrySet()) {
                    writer.print(e.getKey() + ": " + e.getValue() + "\r\n");
                }
                // 空行
                writer.print("\r\n");
                writer.flush();
                // body
                if (body != null && body.length > 0) {
                    out.write(body);
                    out.flush();
                }

                // ===== 4. 读取响应并解析为 HttpResponse =====
                HttpResponse response = readResponse(socket.getInputStream());

                int status = response.getStatusCode();

                // ===== 5. 处理响应（缓存 & 重定向） =====
                if (status == 304) { // Not Modified
                    byte[] cachedBody = bodyCache.get(uri);
                    if (cachedBody != null) {
                        HttpResponse cached = new HttpResponse(response);
                        cached.setStatusCode(200);
                        cached.setBody(cachedBody);
                        return cached;
                    }
                    // 没有本地缓存就直接返回 304 响应
                    return response;
                } else if (status == 301 || status == 302) {
                    String location = response.getHeaders().get("Location");
                    if (location == null || location.isEmpty()) {
                        return response;
                    }
                    URL newUrl = new URL(location);
                    String newPath = newUrl.getPath();
                    if (newPath == null || newPath.isEmpty()) {
                        newPath = "/";
                    }
                    if (newUrl.getQuery() != null && !newUrl.getQuery().isEmpty()) {
                        newPath = newPath + "?" + newUrl.getQuery();
                    }
                    Map<String, String> newHeaders = getStringStringMap(newUrl, currentRequest);

                    currentRequest = new HttpRequest(method, newPath, version, newHeaders, body);
                    // 关闭当前 socket，继续下一轮循环
                    continue;
                } else if (status == 200) {
                    if ("GET".equalsIgnoreCase(method)) {
                        String etag = response.getHeaders().get("ETag");
                        if (etag != null) {
                            eTagCache.put(uri, etag);
                            bodyCache.put(uri, response.getBody());
                        }
                    }
                    return response;
                } else {
                    // 其他状态码，直接返回
                    return response;
                }
            }
        }
        throw new Exception("Too many redirects.");
    }

    private static Map<String, String> getStringStringMap(URL newUrl, HttpRequest currentRequest) {
        String newHostHeader;
        int newPort = newUrl.getPort();
        if (newPort == -1 || newPort == newUrl.getDefaultPort()) {
            newHostHeader = newUrl.getHost();
        } else {
            newHostHeader = newUrl.getHost() + ":" + newPort;
        }

        Map<String, String> newHeaders = new HashMap<>(currentRequest.getHeaders());
        newHeaders.put("Host", newHostHeader);
        // 重定向后通常不再携带条件请求头
        newHeaders.remove("If-None-Match");
        return newHeaders;
    }

    /**
     * 便捷方法：发送 GET 请求。
     *
     * @param urlString 目标 URL
     */
    public HttpResponse get(String urlString) throws Exception {
        URL url = new URL(urlString);

        String path = url.getPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        if (url.getQuery() != null && !url.getQuery().isEmpty()) {
            path = path + "?" + url.getQuery();
        }

        Map<String, String> headers = new HashMap<>();
        int port = url.getPort();
        String hostHeader;
        if (port == -1 || port == url.getDefaultPort()) {
            hostHeader = url.getHost();
        } else {
            hostHeader = url.getHost() + ":" + port;
        }
        headers.put("Host", hostHeader);
        headers.put("User-Agent", "SimpleHttpClient/1.0");
        headers.put("Accept", "*/*");

        HttpRequest request = new HttpRequest("GET", path, "HTTP/1.1", headers, null);
        return send(request);
    }

    /**
     * 从输入流解析 HTTP 响应报文。
     */
    private HttpResponse readResponse(InputStream in) throws Exception {
        BufferedInputStream bin = new BufferedInputStream(in);

        // 状态行
        String statusLine = readLine(bin);
        if (statusLine == null || statusLine.trim().isEmpty()) {
            throw new Exception("Empty status line from server");
        }
        String[] parts = statusLine.split("\\s+", 3);
        if (parts.length < 2) {
            throw new Exception("Invalid status line: " + statusLine);
        }
        String version = parts[0];
        int statusCode = Integer.parseInt(parts[1]);
        String message = parts.length >= 3 ? parts[2] : "";

        // 响应头
        Map<String, String> headers = new LinkedHashMap<>();
        String line;
        while ((line = readLine(bin)) != null) {
            if (line.isEmpty()) break;
            int idx = line.indexOf(':');
            if (idx <= 0) continue;
            String name = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();
            headers.put(name, value);
        }

        // body（仅处理 Content-Length，忽略 chunked）
        byte[] body = new byte[0];
        String contentLengthValue = headers.get("Content-Length");
        String transferEnc = headers.get("Transfer-Encoding");
        if (transferEnc != null && transferEnc.toLowerCase().contains("chunked")) {
            throw new UnsupportedOperationException("chunked Transfer-Encoding not supported by SimpleHttpClient");
        } else if (contentLengthValue != null) {
            int contentLength = Integer.parseInt(contentLengthValue.trim());
            if (contentLength > 0) {
                body = readFixedBytes(bin, contentLength);
            }
        }

        return new HttpResponse(version, statusCode, message, headers, body);
    }
}
