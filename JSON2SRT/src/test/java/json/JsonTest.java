package json;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JsonTest {

    @Test
    public void jsonFileReader() {
        final String FILE_NAME = "./input.json";
        Path filePath = Paths.get(FILE_NAME);
        try {
            String jsonTxt = Files.readString(filePath);
            JSONObject jo = new JSONObject(jsonTxt);
            JSONArray jTranscriptions = jo.getJSONArray("transcriptions");
            if (jTranscriptions.length() == 0) {
                System.out.println("[E] no transcriptions in json file");
            } else {
                // just take the first transcription
                JSONArray jTokens = jTranscriptions.getJSONObject(0).getJSONArray("tokens");
                System.out.printf("Found %d token(s).\n", jTokens.length());

                assertEquals("Bad number of tokens", 10_878, jTokens.length());

                jTokens.toList().forEach(element -> {
                    Map<String, String> tokenMap = (Map<String, String>)element;
                    String wordText = tokenMap.get("token");
                    String type = tokenMap.get("type");
                    System.out.printf("Token (%s): %s\n", type, wordText);
                });

                for (int i = 0; i < jTokens.length(); i++) {
                    JSONObject jToken = jTokens.getJSONObject(i);
                    String wordText = jToken.getString("token");
                    String type = jToken.getString("type");
                    System.out.printf("Token (%s): %s\n", type, wordText);
                }

                System.out.println("Done");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
