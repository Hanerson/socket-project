package Server;

import Server.dispatcher.RequestDispatcher;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HTTP 服务器主类 (Role B)
 * 职责：构建服务器并发模型，监听端口，使用线程池分发连接。
 */

public class SimpleHttpServer {

    private final int port;
    private final ExecutorService threadPool;
    private final RequestDispatcher dispatcher;
    private volatile boolean isRunning = true;

    /**
     * 构造函数
     *
     * @param port 服务器监听的端口号
     */
    public SimpleHttpServer(int port) {
        this.port = port;
        // 初始化 RequestDispatcher (由 Role C 实现)
        this.dispatcher = new RequestDispatcher();
        // 初始化固定大小线程池，处理并发连接
        this.threadPool = Executors.newFixedThreadPool(50);
    }

    /**
     * 启动服务器
     * 在循环中接收客户端连接，并交给线程池处理。
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port: " + port);

            while (isRunning) {
                try {
                    // 1. 阻塞等待客户端连接
                    Socket socket = serverSocket.accept();

                    // 2. 创建连接处理器 (将 Socket 和 分发器 传入)
                    ConnectionHandler handler = new ConnectionHandler(socket, dispatcher);

                    // 3. 将任务提交给线程池执行
                    threadPool.execute(handler);

                } catch (IOException e) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            e.printStackTrace();
        }
    }
}