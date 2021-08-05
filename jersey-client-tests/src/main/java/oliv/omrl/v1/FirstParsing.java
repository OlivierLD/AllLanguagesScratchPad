package oliv.omrl.v1;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        List<Object> jsonTableMap = mapper.readValue(tablesResource.openStream(), List.class);

        // Dump dev
        System.out.printf("Dev has %d entries\n", jsonDevMap.size());

        // Distinct db_id, and cardinality.
        List<String> dbIds = jsonDevMap.stream().map(one -> (String)((Map) one).get("db_id")).distinct().collect(Collectors.toList());
        System.out.printf("%d distinct DB_IDs\n", dbIds.size());
        dbIds.stream().forEach(db -> {
            long nb = jsonDevMap.stream().filter(one -> {
                return db.equals((String) ((Map) one).get("db_id"));
            }).count();
            System.out.printf("%s - %d entries\n", db, nb);
        });

        // Longest query
        Optional<Object> query = jsonDevMap.stream().max(Comparator.comparingInt(one -> ((String) ((Map) one).get("query")).length()));
        Map<String, Object> theOne = (Map<String, Object>)query.get();
        System.out.printf("Longest query, on %s, %s\n", theOne.get("db_id"), theOne.get("query"));

        jsonDevMap.stream()
//                .limit(10)
                .forEach(oneElement -> {
//                    System.out.println("Got a " + oneElement.getClass().getName());
                    Map<String, Object> oneMap = (Map<String, Object>)oneElement;
                    System.out.printf("DB Id: %s\n", oneMap.get("db_id"));
                    System.out.printf("Query: %s\n", oneMap.get("query"));
                    System.out.printf("Question: %s\n", oneMap.get("question"));
                    System.out.println("-------------------------------");
                });

        System.out.println("Done.");
    }

}
