package common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 简单的验证/演示程序（非单元测试框架，便于快速手工验证）。
 */
public class CommonProtocolTest {

    public static void main(String[] args) throws Exception {
        testParsePost();
        testResponseWrite();
    }

    private static void testParsePost() throws Exception {
        String raw =
                "POST /api/login HTTP/1.1\r\n" +
                        "Host: localhost:8080\r\n" +
                        "Content-Type: application/x-www-form-urlencoded\r\n" +
                        "Content-Length: 30\r\n" +
                        "Connection: keep-alive\r\n" +
                        "\r\n" +
                        "username=alice&password=secret";

        ByteArrayInputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.ISO_8859_1));
        HttpRequest req = new HttpRequest(in);
        System.out.println("Parsed request: " + req);
        System.out.println("Method: " + req.getMethod());
        System.out.println("Uri: " + req.getUri());
        System.out.println("Host header: " + req.getHeader("Host"));
        System.out.println("Body text: " + new String(req.getBody(), StandardCharsets.ISO_8859_1));
    }

        private static void testResponseWrite() throws Exception {
        HttpResponse resp = new HttpResponse();
        resp.setStatusCode(200);
        resp.addHeader("Content-Type", "text/plain; charset=utf-8");
        resp.addHeader("Connection", "close");
        resp.setBody("Hello, world!".getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        resp.write(out);
        String outText = out.toString(StandardCharsets.ISO_8859_1.name());
        System.out.println("Serialized response:");
        System.out.println("----- START -----");
        System.out.println(outText.replace("\r\n", "\\r\\n\n")); // 可视化 CRLF
        System.out.println("----- END -----");
    }
}
