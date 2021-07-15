package oliv.omrl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class FirstParsing {

    private final static String DEV_JSON = "dev.json"; // In the resource folder
    private final static String TABLES_JSON = "tables.json"; // In the resource folder

    private final static String DEV_DOCUMENT_PREFIX = "--dev:";
    private final static String TABLES_STATEMENT_PREFIX = "--tables:";

    public static void main(String... args) throws IOException {
        String dev = null;
        String tables = null;

        for (String arg : args) {
            if (arg.startsWith(DEV_DOCUMENT_PREFIX)) {
                dev = arg.substring(DEV_DOCUMENT_PREFIX.length());
            } else if (arg.startsWith(TABLES_STATEMENT_PREFIX)) {
                tables = arg.substring(TABLES_STATEMENT_PREFIX.length());
            }
        }

        URL devResource;
        URL tablesResource;
        if (dev == null) {
            ClassLoader classLoader = FirstParsing.class.getClassLoader();
            devResource = classLoader.getResource(DEV_JSON); // At the root of the resources folder.
        } else {
            devResource = new File(dev).toURI().toURL();
        }
        if (tables == null) {
            ClassLoader classLoader = FirstParsing.class.getClassLoader();
            tablesResource = classLoader.getResource(TABLES_JSON); // At the root of the resources folder.
        } else {
            tablesResource = new File(tables).toURI().toURL();
        }
        System.out.println("Dev Resource   : " + devResource);
        System.out.println("Tables Resource: " + tablesResource);

        ObjectMapper mapper = new ObjectMapper();
        // Dev Object
        List<Object> jsonDevMap = mapper.readValue(devResource.openStream(), List.class);
        // Tables Object
        List<String> jsonTableMap = mapper.readValue(tablesResource.openStream(), List.class);

        // Dump dev
        jsonDevMap.stream()
                .limit(10)
                .forEach(oneElement -> {
                    System.out.println("Got a " + oneElement.getClass().getName());
                    Map<String, Object> oneMap = (Map<String, Object>)oneElement;
                    System.out.printf("DB Id: %s\n", oneMap.get("db_id"));
                    System.out.printf("Query: %s\n", oneMap.get("query"));
                    System.out.printf("Question: %s\n", oneMap.get("question"));
                    System.out.println("-------------------------------");
                });

        System.out.println("Done.");
    }

}
