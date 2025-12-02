package common;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HTTP 响应对象（可变）。
 *
 * 特性：
 *  - 提供 setStatusCode/setStatusMessage/addHeader/setBody 等方法。
 *  - write(OutputStream) 会写出完整的 HTTP 响应报文（状态行 + 头 + 空行 + body）。
 *  - 当调用 setBody(byte[]) 时，会自动设置 Content-Length 头（覆盖现有的 Content-Length）。
 */
public class HttpResponse {

    private String httpVersion = "HTTP/1.1";
    private int statusCode = 200;
    private String statusMessage = "OK";
    private final Map<String, String> headers = new LinkedHashMap<>();
    private byte[] body = new byte[0];
public HttpResponse() {
}
public HttpResponse(HttpResponse httpResponse) {
    this.httpVersion = httpResponse.httpVersion;
    this.statusCode = httpResponse.statusCode;
    this.statusMessage = httpResponse.statusMessage;
    this.headers.putAll(httpResponse.headers);
    this.body = httpResponse.body;
}
    public HttpResponse(String httpVersion, int statusCode, String statusMessage, Map<String, String> headers, byte[] body) {
        this.httpVersion = httpVersion;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.headers.putAll(headers);
        this.body = body;
    }



    /**
     * 设置状态码，同时采用常用状态码的默认 reason phrase（如果有）。
     *
     * @param code HTTP 状态码
     */
    public void setStatusCode(int code) {
        this.statusCode = code;
        this.statusMessage = defaultReasonPhrase(code);
    }

    /**
     * 手动设置状态行中的 message（reason phrase）。
     */
    public void setStatusMessage(String message) {
        if (message != null) this.statusMessage = message;
    }

    public void setHttpVersion(String version) {
        if (version != null) this.httpVersion = version;
    }

    public void addHeader(String key, String value) {
        if (key == null || value == null) return;
        headers.put(key, value);
    }

    /**
     * 设置响应体（同时设置 Content-Length）。
     *
     * @param body body 字节（如果传 null，将设置为空 body）
     */
    public void setBody(byte[] body) {
        this.body = body == null ? new byte[0] : body.clone();
        addHeader("Content-Length", String.valueOf(this.body.length));
    }
public void setStringBody(String stringBody) {
        this.body=stringBody.getBytes(StandardCharsets.UTF_8);
}
    public byte[] getBody() {
        return body == null ? new byte[0] : body.clone();
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    /**
     * 将当前响应写入到输出流。
     *
     * @param out 输出流（通常是 socket.getOutputStream()）
     * @throws Exception 写入异常
     */
    public void write(OutputStream out) throws Exception {
        if (out == null) throw new IllegalArgumentException("OutputStream cannot be null");

        PrintWriter writer = new PrintWriter(out, false, StandardCharsets.ISO_8859_1);

        // 1) 状态行
        writer.print(httpVersion + " " + statusCode + " " + statusMessage + "\r\n");

        // 2) 确保 Content-Length 存在（如果未设置 body 且 header 也未设置，默认 0）
        if (!headers.containsKey("Content-Length")) {
            addHeader("Content-Length", String.valueOf(body == null ? 0 : body.length));
        }

        // 3) 写入所有 header
        for (Map.Entry<String, String> e : headers.entrySet()) {
            writer.print(e.getKey() + ": " + e.getValue() + "\r\n");
        }

        // 4) 空行
        writer.print("\r\n");
        writer.flush(); // 确保头部已经发送到流中

        // 5) 写入 body（如果有）
        if (body != null && body.length > 0) {
            out.write(body);
            out.flush();
        }
    }

    private static String defaultReasonPhrase(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 304 -> "Not Modified";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 409 -> "Conflict";
            case 500 -> "Internal Server Error";
            default -> "Status";
        };
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "httpVersion='" + httpVersion + '\'' +
                ", statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", headers=" + headers +
                ", bodyLength=" + (body == null ? 0 : body.length) +
                '}';
    }
}
