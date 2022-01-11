package oliv.tyrus.client;

import jakarta.websocket.*;
import org.glassfish.tyrus.client.ClientManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * GET ws://websocket.example.com/ HTTP/1.1
 * Origin: http://example.com
 * Connection: Upgrade
 * Host: websocket.example.com
 * Upgrade: websocket
 *
 * Uses Tyrus, org.glassfish.tyrus:tyrus-client:2.0.1
 */

@ClientEndpoint
public class SampleSpeechWSClient {

    @OnOpen
    public void onOpen(Session session) {
        System.out.println ("--- Connected " + session.getId());
        try {
            session.getBasicRemote().sendText("start");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println ("--- Received " + message);
            String userInput = bufferRead.readLine();
            return userInput;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("--- Session: " + session.getId());
        System.out.println("--- Closing because: " + closeReason);
    }

    public static void main(String... args) {
        ClientManager client = ClientManager.createClient();
        try {
            URI uri = new URI("ws://localhost:8025/folder/app");
            client.connectToServer(SampleSpeechWSClient.class, uri);
            while(true) {

            }
        } catch (DeploymentException | URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
