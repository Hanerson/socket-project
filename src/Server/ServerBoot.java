package Server;

import Server.SimpleHttpServer;
import Server.dispatcher.RequestDispatcher;

public class ServerBoot {
    public static void main(String[] args) {
        try {
            // 1. 创建你的分发器 (Role C)
            RequestDispatcher dispatcher = new RequestDispatcher();

            // 2. 将分发器注入到服务器 (Role B)
            // 注意：端口号设为 8080
            SimpleHttpServer server = new SimpleHttpServer(8080, dispatcher);

            server.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}