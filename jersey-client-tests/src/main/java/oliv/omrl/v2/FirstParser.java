package oliv.omrl.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import oliv.omrl.v2.utils.OMRL2SQL;

import java.io.File;
import java.net.URL;
import java.util.Map;

/**
 * Hard coded file names.
 * Invokes OMRL2SQL.omrlToSQLQuery
 */
public class FirstParser {

    private final static String OMRL_SCHEMA_PATH = "omrl.mapping.schema.01.json";
//    private final static String OMRL_QUERY_PATH  = "omrl.race_track.query.01.json";
//    private final static String OMRL_QUERY_PATH  = "omrl.race_track.query.02.json";
//    private final static String OMRL_QUERY_PATH  = "omrl.race_track.query.03.json";
    private final static String OMRL_QUERY_PATH  = "omrl.race_track.query.04.json";
    private final static String SCHEMA_NAME = "race_track";

    public static void main(String... args) {
        URL schemaResource;
        URL queryResource;

        try {
            schemaResource = new File(OMRL_SCHEMA_PATH).toURI().toURL();
            queryResource = new File(OMRL_QUERY_PATH).toURI().toURL();

            ObjectMapper mapper = new ObjectMapper();
            // Schema Object
            Map<String, Map<String, Object>> schemas = mapper.readValue(schemaResource.openStream(), Map.class);
            // Query Object
            Map<String, Object> query = mapper.readValue(queryResource.openStream(), Map.class);
            System.out.println("Done generating resources.");

            String sql = "";
            Map<String, Object> schema = schemas.get(SCHEMA_NAME);
            if (schema != null) {
                sql = OMRL2SQL.omrlToSQLQuery(schema, query);
            } else {
                System.out.printf("Schema [%s] not found.\n", SCHEMA_NAME);
            }

            System.out.println("SQL Query:");
            System.out.println(sql);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
