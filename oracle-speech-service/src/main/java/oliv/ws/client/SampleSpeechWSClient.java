package oliv.ws.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import oliv.rest.client.SampleSpeechRESTClient;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import utils.TextToSpeech;

import javax.ws.rs.core.HttpHeaders;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET ws://websocket.example.com/ HTTP/1.1
 * Origin: http://example.com
 * Connection: Upgrade
 * Host: websocket.example.com
 * Upgrade: websocket
 *
 * Uses TooTallNate, org.java-websocket:Java-WebSocket:1.5.2
 *
 * Modify the WEB_SOCKET_URI to fit yours.
 */

public class SampleSpeechWSClient {

    private final static Logger LOGGER = Logger.getLogger(SampleSpeechRESTClient.class.getName()); //  .getLogger(Logger.GLOBAL_LOGGER_NAME); // BoatBox3D.class;
    static {
        LOGGER.setLevel(Level.ALL);
    }

    private final static String WEB_SOCKET_URI = "ws://100.111.136.104/voice/stream/recognize/en-us/generic";
    private final static String AUDIO_FILE = "foo.wav";

    private WebSocketClient wsClient = null;
    private final SampleSpeechWSClient instance = this;

    private Boolean isWsConnected = false;

    private final static boolean SPEAK_UP = "true".equals(System.getProperty("speak-up"));
    private final static boolean VERBOSE = "true".equals(System.getProperty("verbose"));
    private final static boolean SLOW_DOWN = "true".equals(System.getProperty("slow-down"));

    private final ObjectMapper mapper = new ObjectMapper();

    public SampleSpeechWSClient(String wsUri, Map<String, String> headers) {
        try {
            this.wsClient = new WebSocketClient(new URI(wsUri), headers) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    System.out.println("WS On Open");
                    synchronized (instance.isWsConnected) {
                        instance.isWsConnected = true;
                    }
                }

                @Override
                public void onMessage(String mess) {
                    if (VERBOSE) {
                        System.out.printf("WebSocketReader onMessage: %s\n", mess);
                    }
                    instance.fireDataRead(mess);
                 }

                @Override
                public void onClose(int i, String string, boolean b) {
                    System.out.printf("WS On Close: %d, %s, %b\n", i, string, b);
                    synchronized (instance.isWsConnected) {
                        instance.isWsConnected = false;
                    }
                }

                @Override
                public void onError(Exception exception) {
                    System.out.println("WS On Error");
                    exception.printStackTrace();
                }
            };
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void fireDataRead(Object data) {
        if (data instanceof String) {
            try {
                Map<String, Object> resultMap = (Map<String, Object>)mapper.readValue((String) data, Map.class);
                Object nbest = resultMap.get("nbest");
                if (nbest != null) {
                    if (nbest instanceof List) {
                        List<Object> list = (List<Object>)nbest;
                        list.forEach(obj -> {
                            if (obj instanceof Map) {
                                Map<String, String> oneMap = (Map<String, String>)obj;
                                String toSpeak = String.format("Transcription for file %s is the following one. \"%s\". Score is %s %%.",
                                        AUDIO_FILE,
                                        oneMap.get("utterance"),
                                        oneMap.get("score"));
                                try {
                                    if (SPEAK_UP) {
                                        TextToSpeech.speak(toSpeak);
                                    } else {
                                        System.out.println(toSpeak);
                                    }
                                } catch (Exception ex) {
                                    System.out.printf(">> Transcription for %s: %s (score %s)\n", AUDIO_FILE, oneMap.get("utterance"), oneMap.get("score"));
                                }
                            } else {
                                System.out.printf("Un-managed type: %s\n", obj.getClass().getName());
                            }
                        });
                    } else {
                        System.out.printf(">> read %s\n", data);
                    }
                } else {
                    System.out.printf(">> read %s\n", data);
                }
            } catch (JsonProcessingException jsonEx) {
                jsonEx.printStackTrace();
            }
        } else {
            // TODO See that?
            System.out.printf(">> read %s\n", data);
        }
    }

    public void start() {
        this.wsClient.connect();
    }

    public void stop() {
        if (this.isWsConnected) {
            System.out.println("(stop) ...Closing connection.");
            this.wsClient.close();
            // Wait for the connection to be closed
            while (this.isWsConnected) {
                SampleSpeechWSClient.sleep((10L));
            }
            System.out.println("Closed!");
        }
    }

    public void sendData(byte[] data) {
        int nbTry = 0;
        while (!this.isWsConnected && nbTry < 10) {
            System.out.println("Waiting for the WebSocket connection...");
            nbTry += 1;
            SampleSpeechWSClient.sleep(250L);
        }
        if (this.isWsConnected) {
            this.wsClient.send(data);
        } else {
            throw new RuntimeException("Cannot connect...");
        }
    }

    public static void sleep(long howLong) {
        try {
            Thread.sleep(howLong);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) throws Exception {
        System.out.println("There we are!");
        System.out.println("Ctrl-C to quit...");

        Thread me = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized(me) {
                me.notify();
//                SampleSpeechWSClient.sleep(2_000L);
                try {
                    me.wait(); // Wait for the connection to shut down.
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }, "Hook"));

        Map<String, String> headers = Map.of(HttpHeaders.CONTENT_TYPE,"audio/wav");
        SampleSpeechWSClient speechClient = new SampleSpeechWSClient(WEB_SOCKET_URI, headers);
        speechClient.start();

        if (SLOW_DOWN) {
            SampleSpeechWSClient.sleep(1_000L);
        }

        File file = new File(AUDIO_FILE);
        if (!file.exists()) {
            throw new RuntimeException(String.format("File %s not found in %s", AUDIO_FILE, System.getProperty("user.dir")));
        }
        byte[] audioContent = new byte[(int) file.length()];

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) { // AutoClose
            dis.readFully(audioContent);
        }
        // dis.close();

        // Send data for processing
        System.out.printf("Sending content of %s for processing.\n", AUDIO_FILE);
        speechClient.sendData(audioContent);

        if (SLOW_DOWN) {
            SampleSpeechWSClient.sleep(2_000L);
        }

        System.out.println("Done with the process. You can wait for the result, and/or Ctrl-C any time now.");

        synchronized(me) {
            try {
                me.wait();
                System.out.println("\nExit required...");
                speechClient.stop();
                System.out.println("Bye.");
                synchronized(me) {
                    me.notify();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Over and out.");
    }
}
