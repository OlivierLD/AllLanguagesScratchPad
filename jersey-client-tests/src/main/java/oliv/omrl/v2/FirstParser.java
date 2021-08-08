package oliv.omrl.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import oliv.omrl.v2.utils.OMRL2SQL;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hard coded file names.
 * Invokes OMRL2SQL.omrlToSQLQuery {@link oliv.omrl.v2.utils.OMRL2SQL#omrlToSQLQuery(Map, Map)}
 */
public class FirstParser {

    private final static boolean EXECUTE_QUERY = false;

    private final static String OMRL_SCHEMA_PATH = "omrl.mapping.schema.01.json";

    private final static String[] OMRL_QUERY_PATH = {
            "omrl.race_track.query.01.json",  // 0
            "omrl.race_track.query.02.json",  // 1
            "omrl.race_track.query.03.json",  // 2
            "omrl.race_track.query.04.json",  // 3
            "omrl.race_track.query.05.json",  // 4
            "omrl.race_track.query.06.json",  // 5
            "omrl.race_track.query.07.json"   // 6
    };
    private final static int PATH_INDEX = 6;

    private final static String SCHEMA_NAME = "race_track";

    private final static String JDBC_HOSTNAME = "100.111.136.104";  // "100.102.84.101";
    private final static int JDBC_PORT = 1521;
    private final static String JDBC_SERVICE_NAME = "BOTS.localdomain";

//    private final static String USERNAME = "sys as SYSDBA"; // ""OMCE_BOTS";
//    private final static String PASSWORD = "DBA4bots12345678!";
    private final static String USERNAME = "races";
    private final static String PASSWORD = "racesracesracesraces";

    public static void main(String... args) {
        URL schemaResource;
        URL queryResource;

        try {
            schemaResource = new File(OMRL_SCHEMA_PATH).toURI().toURL();
            queryResource = new File(OMRL_QUERY_PATH[PATH_INDEX]).toURI().toURL();

            ObjectMapper mapper = new ObjectMapper();
            // Schema Object
            Map<String, Map<String, Object>> schemas = mapper.readValue(schemaResource.openStream(), Map.class);
            // Query Object
            Map<String, Object> query = mapper.readValue(queryResource.openStream(), Map.class);
            System.out.println("Done generating resources, processing the OMRL query.");

            String sql = "";
            Map<String, Object> schema = schemas.get(SCHEMA_NAME);
            if (schema != null) {
                sql = OMRL2SQL.omrlToSQLQuery(schema, query);
            } else {
                System.out.printf("Schema [%s] not found.\n", SCHEMA_NAME);
            }

            System.out.println("SQL Query:");
            System.out.println(sql);

            // Execution?
            if (EXECUTE_QUERY) {
                System.out.println("-------------------------------------");
                String jdbcUrl = String.format("jdbc:oracle:thin:@//%s:%d/%s",
//                USERNAME,
//                PASSWORD,
                        JDBC_HOSTNAME,
                        JDBC_PORT,
                        JDBC_SERVICE_NAME);

                Class.forName("oracle.jdbc.driver.OracleDriver");
                System.out.println(">> Driver loaded");
                System.out.printf("Connecting with [%s]\n", jdbcUrl);
                Connection connection = DriverManager.getConnection(jdbcUrl, USERNAME, PASSWORD);
                System.out.println(">> Connected");

                // Try the select stmt generated above
                String sqlStatement = sql; // No final semi-column

                System.out.println(">> Executing query");
                System.out.println(sqlStatement);
                System.out.println("-------------------------------------");

                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(sqlStatement);

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                int nbRows = 0;
                while (rs.next()) {
                    nbRows++;
                    List<String> oneLine = new ArrayList<>(); // One record.
                    for (int i = 0; i < columnCount; i++) {
                        String colName = metaData.getColumnName(i + 1);
                        int columnType = metaData.getColumnType(i + 1);
                        String colValue = "";
                        if (columnType == JDBCType.INTEGER.getVendorTypeNumber()) {
                            colValue = String.format("%d", rs.getInt(colName));
                        } else if (columnType == JDBCType.VARCHAR.getVendorTypeNumber()) {
                            colValue = String.format("%s", rs.getString(colName));
                        } else {
                            colValue = String.format("%s", rs.getObject(colName)); // Big fallback
                        }
                        oneLine.add(String.format("%s: %s", colName, colValue));
                    }
                    System.out.println(oneLine.stream().collect(Collectors.joining(", ")));
                }
                rs.close();
                statement.close();

                System.out.println("-------------------------------------");
                System.out.printf(">> Retrieved %d row(s)\n", nbRows);
                System.out.println("-------------------------------------");

                connection.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
