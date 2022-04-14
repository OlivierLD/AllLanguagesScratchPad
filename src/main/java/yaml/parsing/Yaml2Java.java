package yaml.parsing;

import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An example.
 * Generate the Java code (a Map) equivalent to a yaml document.
 * Uses snakeyaml 'org.yaml:snakeyaml:1.21'
 */
public class Yaml2Java {

    private final static String YAML_FILE = "acousticModelsMap.yaml"; // in the misc-tests folder
    private final static NumberFormat PLATFORM_FORMAT = new DecimalFormat("00.00");

    private static void findInMap(Map<String, Object> map, String cultureStr, String platform) {
        Optional<Object> foundCulture = ((List<Object>) map.get("cultures")).stream()
                .filter(culture -> ((Map<String, Object>) culture).get("culture").equals(cultureStr))
                .findFirst();
        if (foundCulture.isPresent()) {
            Map<String, Object> foundMap = (Map<String, Object>)foundCulture.get();
            List<Object> platFormList = (List<Object>)foundMap.get("botsPlatforms");
            Optional<Object> botsPlatform = platFormList.stream()
                    // Original platform: this is a number in the yaml.
                    .filter(pf -> PLATFORM_FORMAT.format(Double.parseDouble(String.valueOf(((Map<String, Object>) pf).get("botsPlatform")))).equals(platform))
//                    .filter(pf -> ((Map<String, Object>) pf).get("botsPlatform").equals(platform))
                    .findFirst();
            if (botsPlatform.isPresent()) {
                Map<String, Object> foundPlatform = (Map<String, Object>)botsPlatform.get();
                System.out.println(String.format("For %s/%s, SLM version is %s", cultureStr, platform, foundPlatform.get("acousticModel")));
            } else {
                System.out.printf("No %s in %s !\n", platform, cultureStr);
            }
        } else {
            System.out.printf("No %s in original map...\n", cultureStr);
        }
    }

    public void generateCode(Map<String, Object> map, PrintStream out) {
        out.println("final Map<String, Object> acousticModelsMap = Map.of(  // Immutable map!");
        out.println(String.format(" \"version\", %s,", map.get("version") ));
        out.println(String.format(" \"name\", \"%s\",", map.get("name") ));

        List<Object> cultures = (List<Object>)map.get("cultures");
        out.println(" \"cultures\", List.of(  // Cultures list");

        cultures.forEach(culture -> {
            out.println("Map.of(");
            out.println(String.format("\"culture\", \"%s\",", ((Map<String, Object>)culture).get("culture")));
            out.println("\"botsPlatforms\", List.of(");
            List<Object> botsPlatforms = (List<Object>)((Map<String, Object>)culture).get("botsPlatforms");
            botsPlatforms.forEach(botsPlatform -> {
                String platform = PLATFORM_FORMAT.format(((Map<String, Object>)botsPlatform).get("botsPlatform"));
                String model = (String)((Map<String, Object>)botsPlatform).get("acousticModel");
                out.println(String.format("Map.of(\"botsPlatform\", \"%s\",", platform));
                out.println(String.format("       \"acousticModel\", \"%s\")%s", model, botsPlatforms.indexOf(botsPlatform) == botsPlatforms.size() - 1 ? "" : ","));
            });
            out.println(")  // End of botsPlatforms list");

            out.println(String.format(")%s  // end of one culture map", cultures.indexOf(culture) == cultures.size() - 1 ? "" : ",")); // End of Map of above

        });
        out.println(")  // End of culture list"); // End of cultures list
        out.println(");"); // End of top map.
    }
    
    public void go() throws Exception {
        Yaml yaml = new Yaml();
        InputStream inputStream = new FileInputStream(YAML_FILE);
        Map<String, Object> map = yaml.load(inputStream);
        System.out.println("Map:" + map);
//		Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
//		while (iterator.hasNext()) {
//			Map.Entry<String, Object> next = iterator.next();
//			System.out.println(String.format("%s -> %s", next.getKey(), next.getValue().getClass().getName()));
//		}
        map.keySet().forEach(k -> {
            System.out.println(String.format("%s -> %s", k, map.get(k).getClass().getName()));
        });

        // Code generation
        System.out.println("------- Code Generation -- ----");

        PrintStream ps;
        ByteArrayOutputStream baos;
        final int OPTION = 2;

        if (OPTION == 1) {
            ps = System.out;
        }
        if (OPTION == 2) {
            baos = new ByteArrayOutputStream();
            ps = new PrintStream(baos);
        }

        generateCode(map, ps);

        if (OPTION == 2) {
            System.out.println(baos.toString());
        }

        System.out.println("---- End of Generated Code ----");

        if (true) {
            // Result of the code generated below.
            final Map<String, Object> acousticModelsMap = Map.of(  // Immutable map!
                    "version", 1.0,
                    "name", "Acoustic models",
                    "cultures", List.of(  // Cultures list
                            Map.of(
                                    "culture", "en-US",
                                    "botsPlatforms", List.of(
                                            Map.of("botsPlatform", "22.04",
                                                    "acousticModel", "215.20210406.17"),
                                            Map.of("botsPlatform", "22.02",
                                                    "acousticModel", "215.20200610.0"),
                                            Map.of("botsPlatform", "21.12",
                                                    "acousticModel", "215.20200610.0"),
                                            Map.of("botsPlatform", "21.10",
                                                    "acousticModel", "215.20200610.0"),
                                            Map.of("botsPlatform", "21.08",
                                                    "acousticModel", "215.20200610.0"),
                                            Map.of("botsPlatform", "21.06",
                                                    "acousticModel", "215.20200610.0"),
                                            Map.of("botsPlatform", "21.04",
                                                    "acousticModel", "215.20200610.0"),
                                            Map.of("botsPlatform", "21.02",
                                                    "acousticModel", "215.20200610.0"),
                                            Map.of("botsPlatform", "20.12",
                                                    "acousticModel", "215.20200610.0"),
                                            Map.of("botsPlatform", "20.09",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "20.08",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "20.06",
                                                    "acousticModel", "none")
                                    )  // End of botsPlatforms list
                            ),  // end of one culture map
                            Map.of(
                                    "culture", "en-AU",
                                    "botsPlatforms", List.of(
                                            Map.of("botsPlatform", "22.04",
                                                    "acousticModel", "215.20201218.1"),
                                            Map.of("botsPlatform", "22.02",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.12",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.10",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.08",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.06",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.04",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.02",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "20.12",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "20.09",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "20.08",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "20.06",
                                                    "acousticModel", "none")
                                    )  // End of botsPlatforms list
                            ),  // end of one culture map
                            Map.of(
                                    "culture", "fr-FR",
                                    "botsPlatforms", List.of(
                                            Map.of("botsPlatform", "22.04",
                                                    "acousticModel", "213.20200508.4"),
                                            Map.of("botsPlatform", "22.02",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.12",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.10",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.08",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.06",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.04",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.02",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "20.12",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "20.09",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "20.08",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "20.06",
                                                    "acousticModel", "none")
                                    )  // End of botsPlatforms list
                            ),  // end of one culture map
                            Map.of(
                                    "culture", "es-ES",
                                    "botsPlatforms", List.of(
                                            Map.of("botsPlatform", "22.04",
                                                    "acousticModel", "213.20200722.0"),
                                            Map.of("botsPlatform", "22.02",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.12",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.10",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.08",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.06",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.04",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "21.02",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "20.12",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "20.09",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "20.08",
                                                    "acousticModel", "none"),
                                            Map.of("botsPlatform", "20.06",
                                                    "acousticModel", "none")
                                    )  // End of botsPlatforms list
                            )  // end of one culture map
                    )  // End of culture list
            );

            // Compare the two maps
            int origCulturesSize = ((List<Object>)map.get("cultures")).size();
            int generatedCultureSize = ((List<Object>)acousticModelsMap.get("cultures")).size();
            System.out.printf("%d vs %d\n", origCulturesSize, generatedCultureSize);

            // Get the 22.04 SLM version for en-US:
            final String CULTURE_TO_FIND = "es-ES"; // ""en-US";
            final String PLATFORM_TO_FIND = "22.04";
            System.out.println("Original Map:");
            findInMap(map, CULTURE_TO_FIND, PLATFORM_TO_FIND);
            System.out.println("Generated Map:");
            findInMap(acousticModelsMap, CULTURE_TO_FIND, PLATFORM_TO_FIND);

            // Make sure it is immutable
            try {
                acousticModelsMap.put("whatever", "duh");
                System.err.println("Should have failed!");
            } catch (UnsupportedOperationException uoe) {
                System.out.println(">> Expected Exception.");
            }
        }
    }

    public static void main(String... args) {
        Yaml2Java yaml2Java = new Yaml2Java();
        System.out.printf("Running from %s\n", System.getProperty("user.dir", null));
        try {
            yaml2Java.go();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
