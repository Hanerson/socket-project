package common;

/**
 * 自定义异常类：用于标识 HTTP 请求解析阶段出现的逻辑或格式错误。
 *
 * 使用场景：
 * - 当服务器在解析 HTTP 请求行、请求头或请求体时，发现格式不符合规范，
 *   例如：
 *     - 缺少空格 / CRLF
 *     - Header 不含冒号
 *     - Content-Length 无法解析为整数
 *     - Chunked 编码格式错误
 * - 注意：它不表示 IO 层异常（例如连接断开、Socket 超时），
 *   这些情况由 {@link java.io.IOException} 捕获与处理。
 *
 * 设计原则：
 * - 继承自 Exception（受检异常），要求调用方显式处理。
 * - 可附带原始 cause（例如 NumberFormatException、EOFException）。
 * - 提供多种构造方法方便捕获上下文信息。
 *
 * 建议上层处理：
 * - 在 ConnectionHandler 或主服务循环中捕获 HttpParseException，
 *   并立即返回 400 Bad Request 响应（然后关闭连接）。
 */
public class HttpParseException extends Exception {

    /**
     * 创建一个无具体信息的 HttpParseException。
     * 通常仅在未知错误情况下使用。
     */
    public HttpParseException() {
        super();
    }

    /**
     * 创建一个带错误消息的 HttpParseException。
     *
     * @param message 错误描述（建议包含请求上下文，如 "Malformed header line: ..."）
     */
    public HttpParseException(String message) {
        super(message);
    }

    /**
     * 创建一个带错误消息和根本原因（cause）的 HttpParseException。
     *
     * @param message 错误描述
     * @param cause   原始异常，例如 NumberFormatException、IOException
     */
    public HttpParseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个仅带根本原因（cause）的 HttpParseException。
     *
     * @param cause 原始异常
     */
    public HttpParseException(Throwable cause) {
        super(cause);
    }
}
