package utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 角色 C/D/Common: 工具类
 * 职责：根据文件后缀名返回对应的 Content-Type。
 */
public class MimeTypes {

    private static final Map<String, String> MIME_MAP = new HashMap<>();

    static {
        // 文本与代码
        MIME_MAP.put("html", "text/html");
        MIME_MAP.put("htm", "text/html");
        MIME_MAP.put("txt", "text/plain");
        MIME_MAP.put("css", "text/css");
        MIME_MAP.put("js", "application/javascript");
        MIME_MAP.put("json", "application/json");
        MIME_MAP.put("xml", "application/xml");

        // 图片
        MIME_MAP.put("png", "image/png");
        MIME_MAP.put("jpg", "image/jpeg");
        MIME_MAP.put("jpeg", "image/jpeg");
        MIME_MAP.put("gif", "image/gif");
        MIME_MAP.put("ico", "image/x-icon");
        MIME_MAP.put("svg", "image/svg+xml");

        // 其他常见格式
        MIME_MAP.put("pdf", "application/pdf");
        MIME_MAP.put("zip", "application/zip");
    }

    /**
     * 根据文件名推断 Content-Type
     *
     * @param filename 文件名，例如 "index.html" 或 "image.png"
     * @return 对应的 MIME 类型，如果未知则返回 "application/octet-stream"
     */
    public static String getContentType(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "application/octet-stream"; // 二进制流默认值
        }

        // 提取后缀名并转小写 (e.g. "index.HTML" -> "html")
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        return MIME_MAP.getOrDefault(extension, "application/octet-stream");
    }
}