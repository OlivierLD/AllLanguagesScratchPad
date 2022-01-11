package oliv.tyrus.client;

import oliv.tyrus.util.JsonUtil;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.Session;
import java.net.URI;
import java.util.Scanner;

/**
 * GET ws://websocket.example.com/ HTTP/1.1
 * Origin: http://example.com
 * Connection: Upgrade
 * Host: websocket.example.com
 * Upgrade: websocket
 *
 * Uses Tyrus, org.glassfish.tyrus:tyrus-client:1.1    // 2.0.1
 */

public class SampleSpeechTyrusClient {

    private final static String WEB_SOCKET_URI = "ws://100.111.136.104/voice/stream/recognize/en-us/generic";
    private final static String AUDIO_FILE = "foo.wav";

    public static final String SERVER = "ws://localhost:8025/ws/chat";

    public static void main(String... args) throws Exception {

        ClientManager client = ClientManager.createClient();
        String message;

        // connect to server
        Session session = client.connectToServer(ClientEndpoint.class, new URI(WEB_SOCKET_URI));

        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Tiny Chat!");
        System.out.println("What's your name?");
        String user = scanner.nextLine();
//        Session session = client.connectToServer(ClientEndpoint.class, new URI(WEB_SOCKET_URI));
        System.out.println("You are logged in as: " + user);

        // repeatedly read a message and send it to the server (until quit)
        do {
            message = scanner.nextLine();
            session.getBasicRemote().sendText(JsonUtil.formatMessage(message, user));
        } while (!message.equalsIgnoreCase("quit"));
    }
}
