package Client;

import common.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

/**
 * 客户端主程序入口：
 *  1. 创建 SimpleHttpClient 实例
 *  2. 提供命令行交互界面，读取用户输入的 URL
 *  3. 调用 httpClient.get(url)
 *  4. 打印响应状态行、所有响应头和响应体
 */
public class Main {

    public static void main(String[] args) throws Exception {
        SimpleHttpClient httpClient = new SimpleHttpClient();
        try (Scanner scanner = new Scanner(System.in)) {

            System.out.print("URL:");
            String url = scanner.nextLine().trim();
            if (url.isEmpty()) {
                System.out.println("URL 不能为空。程序结束。");
                return;
            }

            HttpResponse response = httpClient.get(url);

            // 打印状态行
            System.out.println("=== 响应状态 ===");
            System.out.println(response.getHttpVersion() + " " + response.getStatusCode() + " " + response.getStatusMessage());

            // 打印响应头
            System.out.println("=== 响应头 ===");
            for (Map.Entry<String, String> e : response.getHeaders().entrySet()) {
                System.out.println(e.getKey() + ": " + e.getValue());
            }

            // 打印响应体
            System.out.println("=== 响应体 ===");
            byte[] body = response.getBody();
            if (body != null && body.length > 0) {
                String bodyText = new String(body, StandardCharsets.UTF_8);
                System.out.println(bodyText);
            } else {
                System.out.println("(无内容)");
            }
        }
    }
}
