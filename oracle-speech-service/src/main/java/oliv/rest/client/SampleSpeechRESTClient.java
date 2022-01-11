package oliv.rest.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;

/*
 * The goal here is to do from Java the equivalent of
 * curl -X POST \
    --data-binary @${AUDIO_FILE} \
    -H "Bots-TenantId: ${TENANT_ID}" \
    -H "channelId: recognize" \
    -H "Content-Type: audio/wav" \
    "http://${MACHINE_IP}/voice/recognize/en-us/${OPTION}"
 */
public class SampleSpeechRESTClient {

    private final static String AUDIO_FILE = "foo.wav";
    private final static String TENANT_ID = "odaserviceinstance00";
    private final static String MACHINE = "100.111.136.104";
    private final static String OPTION = "generic";

    private final static String URL_TEMPLATE = "http://%s/voice/recognize/en-us/%s";

    private final static Logger LOGGER = Logger.getLogger(SampleSpeechRESTClient.class.getName()); //  .getLogger(Logger.GLOBAL_LOGGER_NAME); // BoatBox3D.class;
    static {
        LOGGER.setLevel(Level.ALL);
    }

    public static void main(String... args) throws Exception {

        LOGGER.log(Level.INFO,
                String.format("Parsing %s from %s\n", AUDIO_FILE, System.getProperty("user.dir")));

        String restURL = String.format(URL_TEMPLATE, MACHINE, OPTION);

        URL url = new URL(restURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestMethod(HttpMethod.POST);
        conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, "audio/wav");
//        conn.setRequestProperty("Bots-TenantId", TENANT_ID);
//        conn.setRequestProperty("channelId", "recognize");

        File file = new File(AUDIO_FILE);
        if (!file.exists()) {
            throw new RuntimeException(String.format("File %s not found in %s", AUDIO_FILE, System.getProperty("user.dir")));
        }
        byte[] audioContent = new byte[(int) file.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        dis.readFully(audioContent);
        dis.close();

        OutputStream os = conn.getOutputStream();
        os.write(audioContent);
        os.flush();

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_CREATED && responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

        String output;
        System.out.println("Output from Server .... \n");
        StringBuffer sb = new StringBuffer();
        while ((output = br.readLine()) != null) {
             sb.append(output);
        }
        // Parse JSONObject
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> resultMap = mapper.readValue(sb.toString(), Map.class);
            Object nbest = resultMap.get("nbest");
            if (nbest != null) {
                if (nbest instanceof List) {
                    List<Object> list = (List)nbest;
                    list.forEach(obj -> {
                        if (obj instanceof Map) {
                            Map<String, String> oneMap = (Map)obj;
                            System.out.printf(">> Transcription for %s: %s (score %s)\n", AUDIO_FILE, oneMap.get("utterance"), oneMap.get("score"));
                        } else {
                            System.out.printf("Un-managed type: %s\n", obj.getClass().getName());
                        }
                    });
                } else {
                    System.out.printf(">> read %s\n", sb.toString());
                }
            } else {
                System.out.printf(">> read %s\n", sb.toString());
            }
        } catch (JsonProcessingException jsonEx) {
            jsonEx.printStackTrace();
        }
        conn.disconnect();
    }
}
