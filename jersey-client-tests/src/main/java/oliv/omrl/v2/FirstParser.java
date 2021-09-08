package oliv.omrl.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import oliv.omrl.v2.utils.OMRL2SQL;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Hard coded file names.
 * Invokes OMRL2SQL.omrlToSQLQuery {@link oliv.omrl.v2.utils.OMRL2SQL#omrlToSQLQuery(Map, Map)}
 */
public class FirstParser {

    private final static boolean EXECUTE_QUERY = true;
    private final static boolean USE_PREPARED_STMT = true;

    private final static String OMRL_SCHEMA_PATH = // "omrl.mapping.schema.01.json";
//            "omrl.mapping.schema.02.json";
              "omrl.mapping.schema.generated.03.json";

    private final static String[] OMRL_QUERY_PATH = {
            "omrl.race_track.query.01.json",  // index  0
            "omrl.race_track.query.02.json",  // index  1
            "omrl.race_track.query.03.json",  // index  2
            "omrl.race_track.query.04.json",  // index  3
            "omrl.race_track.query.05.json",  // index  4
            "omrl.race_track.query.06.json",  // index  5
            "omrl.race_track.query.07.json",  // index  6
            "omrl.race_track.query.08.json",  // index  7
            "omrl.race_track.query.09.json",  // index  8
            "omrl.dm.query.01.json",          // index  9
            "omrl.dm.query.02.json",          // index 10
            "omrl.dm.query.03.json",          // index 11
            "omrl.dm.query.04.json"           // index 12
    };
    private final static int PATH_INDEX = 8;

    private final static String SCHEMA_NAME = // "department_management";
                                              "race_track";
                                              // "journal_committee";

    private final static String JDBC_HOSTNAME = "100.111.136.104";  // "100.102.84.101";
    private final static int JDBC_PORT = 1521;
    private final static String JDBC_SERVICE_NAME = "BOTS.localdomain";

//    private final static String USERNAME = "sys as SYSDBA"; // ""OMCE_BOTS";
//    private final static String PASSWORD = "DBA4bots12345678!";
    private final static String USERNAME = "races";
    private final static String PASSWORD = "racesracesracesraces";

    private final static String FUNC_REGEX = "^(\\w+)\\(([^\\)]+)\\)";
    private final static Pattern FUNC_PATTERN = Pattern.compile(FUNC_REGEX, Pattern.MULTILINE);

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

            Map<String, Object> omrlSql = null;
            Map<String, Object> schema = schemas.get(SCHEMA_NAME);

            if (schema != null) {
                OMRL2SQL.usePreparedStmt = USE_PREPARED_STMT;
                omrlSql = OMRL2SQL.omrlToSQLQuery(schema, query);
            } else {
                System.out.printf("Schema [%s] not found.\n", SCHEMA_NAME);
            }

            System.out.println("SQL Query:");
            System.out.println(omrlSql.get("query"));

            List<Object> prmValues = (List)omrlSql.get("prm-values");
            if (prmValues != null && prmValues.size() > 0) {
                System.out.println("Prm Values: " + prmValues.stream()
                        .map(prm -> prm.toString())
                        .collect(Collectors.joining(", ")));
            }

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
                String sqlStatement = (String)omrlSql.get("query"); // No final semi-column
                List<String> rsMap = (List<String>)omrlSql.get("rs-map");

                System.out.println(">> Executing query");
                System.out.println(sqlStatement);
                System.out.println("-------------------------------------");

                ResultSet rs;
                PreparedStatement preparedStatement = null;
                Statement statement = null;

                if (USE_PREPARED_STMT) {
                    preparedStatement = connection.prepareStatement(sqlStatement);
                    List<Object> prms = (List)omrlSql.get("prm-values");
                    for (Object prm : prms) {
//                        System.out.println(prm);
                        if (prm instanceof String) {
                            preparedStatement.setString(prms.indexOf(prm) + 1, (String) prm);
                        } else if (prm instanceof Number) {
                            preparedStatement.setDouble(prms.indexOf(prm) + 1, Double.parseDouble(prm.toString()));
                        } else { // TODO Ohlala!
                            System.out.println("Et merde!");
                        }
                    }
                    rs = preparedStatement.executeQuery();
                } else {
                    statement = connection.createStatement();
                    rs = statement.executeQuery(sqlStatement);
                }
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                int nbRows = 0;
                List<Map<String, Object>> finalResultSet = new ArrayList<>();
                while (rs.next()) {
                    nbRows++;
                    List<String> oneLine = new ArrayList<>(); // One record.
                    Map<String, Object> oneRow = new HashMap<>();
                    for (int i = 0; i < columnCount; i++) {
                        String colName = metaData.getColumnName(i + 1);
                        int columnType = metaData.getColumnType(i + 1);
                        String colValue = "";
                        if (columnType == JDBCType.INTEGER.getVendorTypeNumber()) {
                            colValue = String.format("%d", rs.getInt(i+1)); //  colName));
                        } else if (columnType == JDBCType.VARCHAR.getVendorTypeNumber()) {
                            colValue = String.format("%s", rs.getString(i+1)); // colName));
                        } else {
                            colValue = String.format("%s", rs.getObject(i+1)); // colName)); // Big fallback
                        }
                        oneLine.add(String.format("%s: %s", colName, colValue));
                        // the RS Map
                        String rsMapValue = rsMap.get(i);
                        System.out.println(rsMapValue + ":" + colValue);
                        // See if there are parenthesis -> that would be a function
                        String func = null;
                        String funcArg = null;
                        Matcher matcher = FUNC_PATTERN.matcher(rsMapValue);
                        if (matcher.find()) {
                            func = matcher.group(1);
                            funcArg = matcher.group(2);
                            if (func != null) {
                                func = func.trim();
                            }
                            if (funcArg != null) {
                                funcArg = funcArg.trim();
                            }
                            System.out.println("Full match: " + matcher.group(0));
                            for (int x = 1; x <= matcher.groupCount(); x++) {
                                System.out.println("Group " + x + ": " + matcher.group(x));
                            }
                        }
                        String entity = null;
                        String column = null;

                        if (rsMapValue.contains(".")) { // nested
                            if (func == null) {
                                entity = rsMapValue.substring(0, rsMapValue.indexOf(".")).trim();
                                column = rsMapValue.substring(rsMapValue.indexOf(".") + 1).trim();
                            } else {
                                entity = funcArg.substring(0, funcArg.indexOf(".")).trim();
                                column = func + "_" + funcArg.substring(funcArg.indexOf(".") + 1).trim();
                            }
                            // Get the entity subMap, if it exists
                            Map entityMap = (Map)oneRow.get(entity);
                            if (entityMap == null) {
                                entityMap = new HashMap<>();
                                oneRow.put(entity, entityMap);
                            }
                            entityMap.put(column, colValue);
                        } else { // in the from, top level, not nested.
                            oneRow.put(rsMapValue, colValue);
                        }
                    }
                    finalResultSet.add(oneRow);
                    System.out.println(oneLine.stream().collect(Collectors.joining(", ")));
                }
                rs.close();
                if (statement != null) {
                    statement.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }

                System.out.println("-------------------------------------");
                System.out.printf(">> Retrieved %d row(s)\n", nbRows);
                System.out.println("-------------------------------------");

                connection.close();
                // ResultSet
                String finalRS = mapper.writeValueAsString(finalResultSet);
                System.out.println(finalRS);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
