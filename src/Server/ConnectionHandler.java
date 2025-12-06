package Server;

import Server.dispatcher.RequestDispatcher;
import common.HttpRequest;
import common.HttpResponse;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * 单个连接的处理器 (Role B)
 * 职责：处理单个 Socket 连接，实现 HTTP 长连接 (Keep-Alive) 逻辑。
 */
public class ConnectionHandler implements Runnable{
    private final Socket socket;
    private final RequestDispatcher dispatcher;

    /**
     * 构造函数
     * @param socket 客户端连接 Socket
     * @param dispatcher 请求分发器
     */
    public ConnectionHandler(Socket socket, RequestDispatcher dispatcher) {
        this.socket = socket;
        this.dispatcher = dispatcher;
    }

    /**
     * 核心处理逻辑
     * 包含：设置超时、解析请求、分发业务、处理长连接关闭。
     */
    @Override
    public void run() {
        try {
            // 1. 设置 Socket 超时 (防止恶意连接占用资源)
            socket.setSoTimeout(60000);

            // 核心长连接逻辑
            while(true){
                // 2. 从 socket.getInputStream() 构建 HttpRequest (Role A)
                // 如果构建失败或超时(Read timeout)，说明连接已断开或空闲过久，跳出循环。
                HttpRequest request;
                try {
                    request = new HttpRequest(socket.getInputStream());
                }catch (Exception e){
                    // 解析失败（如客户端关闭连接）或超时，结束当前连接处理
                    break;
                }
                System.out.println("Received request: " + request.getUri());

                // 3. 调用 dispatcher.dispatch(request) 获取 HttpResponse (Role C)
                HttpResponse response = dispatcher.dispatch(request);

                // 4. 检查请求头中是否包含 "Connection: close"
                boolean keepAlive = true;
                if (request.isConnectionCloseRequested()) {
                    keepAlive = false;
                    // 设置响应头也为 "Connection: close"
                    response.addHeader("Connection", "close");
                } else {
                    response.addHeader("Connection", "keep-alive");
                }

                // 5. 将 HttpResponse 写入 socket.getOutputStream() (Role A)
                response.write(socket.getOutputStream());
                // 6. 如果需要关闭连接，跳出循环
                if(!keepAlive) break;
            }
        }catch (SocketTimeoutException e){
            System.out.println("Connection timed out (Idle for too long).");
        }catch (IOException e){
            System.out.println("IO Error: " + e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();;
        }finally {
            try {
                if(socket != null && !socket.isClosed()){
                    socket.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
