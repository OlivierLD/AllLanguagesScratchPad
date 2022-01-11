package websocket.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class OneOOne {
    public static void main(String... args) throws IOException, NoSuchAlgorithmException {
        ServerSocket server = new ServerSocket(80);
        try {
            System.out.println("Server has started on 127.0.0.1:80.\r\nWaiting for a connection...");
            Socket client = server.accept();
            System.out.println("A client connected.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
