package utils;

import java.util.HashMap;
import java.util.Map;

public class TextToSpeech {
    private final static Map<String, String> speechTools = new HashMap<>();

    static {
        speechTools.put("Mac OS X", "say");
        speechTools.put("Linux", "espeak");
    }

    public static void speak(String text) throws Exception {
        String speechTool = speechTools.get(System.getProperty("os.name"));
        if (speechTool == null) {
            throw new RuntimeException("No speech tool found in this os [" + System.getProperty("os.name") + "]");
        }
        try {
            Runtime.getRuntime().exec(new String[]{speechTool, "\"" + text + "\""});
        } catch (Exception ex) {
            // ex.printStackTrace();
            throw ex;
        }
    }
}
