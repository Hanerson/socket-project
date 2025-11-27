package Server;

public class ServerBoot {
    public static void main(String[] args) {
        int port = 8080;
        SimpleHttpServer server = new SimpleHttpServer(port);
        server.start();
    }
}