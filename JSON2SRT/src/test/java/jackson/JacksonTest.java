package jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Jackson tests
 */
public class JacksonTest {

    @Test
    public void jacksonBasics() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonResult = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(map);
            System.out.println(jsonResult);
        } catch (JsonProcessingException jpe) {
            jpe.printStackTrace();
        }
    }

    @Test
    public void jacksonFileReader() {

        ObjectMapper mapper = new ObjectMapper();

        final String FILE_NAME = "./input.json";
        Path filePath = Paths.get(FILE_NAME);
        try {
            String jsonTxt = Files.readString(filePath);
            Object obj = mapper.readValue(jsonTxt, Object.class);
            System.out.printf("Object was read, it is a %s\n", obj.getClass().getName());
            if (obj instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) obj;
                String status = (String)map.get("status");
                System.out.printf("Status: %s\n", status);
                Object transcriptions = map.get("transcriptions");
                System.out.printf("Transcriptions is a %s\n", transcriptions.getClass().getName());
                if (transcriptions instanceof List) {
                    Map<String, Object> transcriptionMap = (Map<String, Object>)((List<Object>)transcriptions).get(0);
                    System.out.printf("Transcription: %s\n", transcriptionMap.get("transcription"));
                    // Now the tokens
                    List<Map<String, Object>> tokens = (List<Map<String, Object>>)transcriptionMap.get("tokens");
                    assertEquals("Bad number of tokens", 10_878, tokens.size());
                    System.out.printf("We have %s tokens.\n", tokens.size());
                    tokens.forEach(token -> {
                        System.out.printf("Token (%s): %s\n", token.get("type"), token.get("token"));
                    });
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
