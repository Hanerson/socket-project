package common;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * 稳健的 HttpRequest，实现：
 *  - 使用单一 BufferedInputStream 读取头部与 body（避免 BufferedReader 的预读问题）
 *  - 支持 Content-Length 的 body 读取
 *  - Headers 使用大小写不敏感的 Map
 */
public class HttpRequest {

    private final String method;
    private final String uri;
    private final String httpVersion;
    private final Map<String, String> headers;
    private final byte[] body;

    public HttpRequest(InputStream in) throws Exception {
        if (in == null) throw new IllegalArgumentException("InputStream cannot be null");

        // 使用单一的 BufferedInputStream 做所有读取（避免预读冲突）
        BufferedInputStream bin = new BufferedInputStream(in);

        // 1) 读取请求行（按字节查找 CRLF）
        String requestLine = readLine(bin);
        if (requestLine == null || requestLine.trim().isEmpty()) {
            throw new Exception("Empty request line (client closed or invalid request)");
        }
        String[] parts = requestLine.split("\\s+");
        if (parts.length < 3) throw new Exception("Invalid request line: " + requestLine);
        this.method = parts[0].toUpperCase();
        this.uri = parts[1];
        this.httpVersion = parts[2];

        // 2) 读取 headers，直到空行
        TreeMap<String, String> hdrs = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        String line;
        while ((line = readLine(bin)) != null) {
            if (line.isEmpty()) break; // headers done
            int idx = line.indexOf(':');
            if (idx <= 0) continue;
            String name = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();
            if (hdrs.containsKey(name)) {
                hdrs.put(name, hdrs.get(name) + "," + value);
            } else {
                hdrs.put(name, value);
            }
        }
        this.headers = Collections.unmodifiableMap(hdrs);

        // 3) 读取 body（固定长度）
        byte[] bodyBytes = new byte[0];
        String contentLengthValue = getHeader("Content-Length");
        String transferEnc = getHeader("Transfer-Encoding");
        if (transferEnc != null && transferEnc.toLowerCase().contains("chunked")) {
            throw new UnsupportedOperationException("chunked Transfer-Encoding not supported by HttpRequest");
        } else if (contentLengthValue != null) {
            int contentLength;
            try {
                contentLength = Integer.parseInt(contentLengthValue.trim());
            } catch (NumberFormatException nfe) {
                throw new Exception("Invalid Content-Length: " + contentLengthValue);
            }
            if (contentLength > 0) {
                bodyBytes = readFixedBytes(bin, contentLength);
            }
        }
        this.body = bodyBytes;
    }

    // 从 BufferedInputStream 按字节读取到 CRLF（不包含 CRLF），返回用 ISO_8859_1 解码的行字符串
    private static String readLine(BufferedInputStream bin) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int prev = -1;
        while (true) {
            int cur = bin.read();
            if (cur == -1) {
                if (baos.size() == 0) return null;
                break;
            }
            // 检测 CRLF 序列
            if (prev == '\r' && cur == '\n') {
                // 删除之前写入的 CR (最后一个字节)
                byte[] arr = baos.toByteArray();
                int len = arr.length;
                if (len > 0 && arr[len - 1] == '\r') {
                    return new String(arr, 0, len - 1, StandardCharsets.ISO_8859_1);
                } else {
                    return new String(arr, StandardCharsets.ISO_8859_1);
                }
            }
            baos.write(cur);
            prev = cur;
        }
        return new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
    }

    // 从同一个 BufferedInputStream 读取固定字节数（阻塞直到读到足够或 EOF）
    private static byte[] readFixedBytes(BufferedInputStream in, int len) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
        int remaining = len;
        byte[] buffer = new byte[4096];
        while (remaining > 0) {
            int toRead = Math.min(buffer.length, remaining);
            int r = in.read(buffer, 0, toRead);
            if (r == -1) {
                throw new Exception("Unexpected end of stream while reading body");
            }
            baos.write(buffer, 0, r);
            remaining -= r;
        }
        return baos.toByteArray();
    }

    // getters
    public String getMethod() { return method; }
    public String getUri() { return uri; }
    public String getHttpVersion() { return httpVersion; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getBody() { return body == null ? new byte[0] : body.clone(); }
    public String getHeader(String name) { if (name == null) return null; return headers.get(name); }

    public boolean isConnectionCloseRequested() {
        String conn = getHeader("Connection");
        return conn != null && "close".equalsIgnoreCase(conn.trim());
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                ", httpVersion='" + httpVersion + '\'' +
                ", headers=" + headers +
                ", bodyLength=" + (body == null ? 0 : body.length) +
                '}';
    }
}
