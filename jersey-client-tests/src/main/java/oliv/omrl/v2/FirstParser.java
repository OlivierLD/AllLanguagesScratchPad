package oliv.omrl.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import oliv.omrl.v2.utils.OMRL2SQL;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Hard coded file names.
 * Invokes OMRL2SQL.omrlToSQLQuery {@link oliv.omrl.v2.utils.OMRL2SQL#omrlToSQLQuery(Map, Map, org.json.JSONObject)}
 */
public class FirstParser {

    private final static boolean EXECUTE_QUERY = true; // when OMRLQuery.execute is true too.
    private final static boolean USE_PREPARED_STMT = false;

    private final static String OMRL_SCHEMA_PATH = "oct29.schemas/OMRL_base_schema.json";
    private final static String OMRL_SQL_SCHEMA_PATH = "oct29.schemas/OMRL_sql_schema.json";

    private final static String OMRL_SCHEMA_NS = "https://oda.oracle.com/OMRL-Schema";
    private final static String OMRL_SQL_SCHEMA_NS = "https://oda.oracle.com/OMRL-Sql-Schema";

    public enum OMRLQuery {
        _01("omrl.race_track.query.01.json", "race_track", true),
        _02("omrl.race_track.query.02.json", "race_track", true),
        _03("omrl.race_track.query.03.json", "race_track", true),
        _04("omrl.race_track.query.04.json", "race_track", true),
        _05("omrl.race_track.query.05.json", "race_track", true),
        _06("omrl.race_track.query.06.json", "race_track", true),
        _07("omrl.race_track.query.07.json", "race_track", false),
        _08("omrl.race_track.query.08.json", "race_track", true),
        _09("omrl.race_track.query.09.json", "race_track", true),
        _10("omrl.race_track.query.10.json", "race_track", true),
        _11("omrl.race_track.query.11.json", "race_track", true),
        _12("omrl.race_track.query.12.json", "race_track", true),
        _13("omrl.race_track.query.13.json", "race_track", true),
        _14("omrl.race_track.query.14.json", "race_track", true),
        _14_2("omrl.race_track.query.14.2.json", "race_track", true),
        _15("omrl.race_track.query.15.json", "race_track", true),
        _16("omrl.race_track.query.16.json", "race_track", true),
        _17("omrl.race_track.query.17.json", "race_track", true),
        _18("omrl.race_track.query.18.json", "race_track", true),
        _19("omrl.race_track.query.19.json", "race_track", true),
        _20("omrl.race_track.query.20.json", "race_track", false),
        _21("omrl.race_track.query.21.json", "race_track", false),
        _22("omrl.race_track.query.22.json", "race_track", false),
        _23("omrl.race_track.query.23.json", "race_track", false),
        _24("omrl.race_track.query.24.json", "race_track", false),
        _25("omrl.race_track.query.25.json", "race_track", true), // With race_stuff. Can execute.
        _26("omrl.race_track.query.26.json", "race_track", false),
        _27("omrl.race_track.query.27.json", "race_track", false),
        _28("omrl.race_track.query.28.json", "race_track", true),
        _29("omrl.race_track.query.29.json", "race_track", false),
        _30("omrl.race_track.query.30.json", "race_track", false),
        _31("omrl.race_track.query.31.json", "race_track", false),
        _32("omrl.race_track.query.32.json", "race_track", false),
        _33("omrl.race_track.query.33.json", "race_track", true),
        _34("omrl.race_track.query.34.json", "race_track", true),
        _35("omrl.race_track.query.35.json", "race_track", true),
        _36("omrl.race_track.query.36.json", "race_track", true),
        _37("omrl.race_track.query.37.json", "race_track", true),
        _38("omrl.race_track.query.38.json", "race_track", true),
        _39("omrl.race_track.query.39.json", "race_track", true),
        _40("omrl.race_track.query.40.json", "race_track", false),
        _41("omrl.race_track.query.41.json", "race_track", false),
        _42("omrl.race_track.query.42.json", "race_track", false),
        _43("omrl.race_track.query.43.json", "race_track", false),
        _44("omrl.race_track.query.44.json", "race_track", true),
        _45("omrl.race_track.query.45.json", "race_track", true),
        _46("omrl.race_track.query.46.json", "race_track", false),
        _47("omrl.race_track.query.47.json", "race_track", false),
        _48("omrl.race_track.query.48.json", "race_track", false),
        _49("omrl.race_track.query.49.json", "race_track", true),
        _50("omrl.race_track.query.50.json", "race_track", true),
        _51("omrl.race_track.query.51.json", "race_track", true),
        _52("omrl.race_track.query.52.json", "race_track", true),
        _DM_01("omrl.dm.query.01.json", "department_management", false),
        _DM_02("omrl.dm.query.02.json", "department_management", false),
        _JC_01("omrl.jc.query.03.json", "journal_committee", false),
        _DM_04("omrl.dm.query.04.json", "department_management", false),
        _BK_01("omrl.bike_1.query.01.json", "bike_1", false);

        private final String fileName;
        private final String connection;
        private final boolean execute;

        OMRLQuery(String fileName, String connection, boolean execute) {
            this.fileName = fileName;
            this.connection = connection;
            this.execute = execute;
        }

        public String fileName() {
            return this.fileName;
        }

        public String connection() {
            return this.connection;
        }

        public boolean execute() {
            return this.execute;
        }
    }

    private final static OMRLQuery DEFAULT_QUERY = OMRLQuery._26;

//    private final static String SCHEMA_NAME = ""; // "department_management", "race_track", "journal_committee";
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
        boolean justOne = true;
        if (justOne) {
//            executeQuery(OMRLQuery._BK_01); // DEFAULT_QUERY); // Default one
            OMRLQuery q = OMRLQuery._48;
            System.out.println(">> Processing query " + q);
            executeQuery(q); // DEFAULT_QUERY); // Default one
        } else {
            for (OMRLQuery query : OMRLQuery.values()) {
                System.out.println("Executing " + query.toString());
                try {
                    executeQuery(query);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void executeQuery(OMRLQuery omrlQuery) {
        URL schemaResource;
        URL sqlSchemaResource;
        URL queryResource;

        try {
            schemaResource = new File(OMRL_SCHEMA_PATH).toURI().toURL();
            sqlSchemaResource = new File(OMRL_SQL_SCHEMA_PATH).toURI().toURL();
            queryResource = new File(omrlQuery.fileName).toURI().toURL();

            ObjectMapper mapper = new ObjectMapper();
            // Schema Object
            List<Object> schemas = mapper.readValue(schemaResource.openStream(), List.class);
            List<Object> sqlSchemas = mapper.readValue(sqlSchemaResource.openStream(), List.class);
            // Query Object
            Map<String, Object> query = mapper.readValue(queryResource.openStream(), Map.class);

            JSONObject jsonQuery = null;
            try {
                jsonQuery = new JSONObject(mapper.writeValueAsString(query));
            } catch (Exception ex) {
                throw new RuntimeException("JSON Conversion failed.");
            }

            // System.out.println("Done generating resources, processing the OMRL query.");

            Map<String, Object> schema = null; // schemas.get(SCHEMA_NAME);
            for (Object obj : schemas) {
                if (obj instanceof Map) {
                    Map<String, Object> _schema = (Map)obj;
                    if (omrlQuery.connection().equals(_schema.get("schema"))) {
                        schema = _schema;
                        break;
                    }
                }
            }
            Map<String, Object> sqlSchema = null; // schemas.get(SCHEMA_NAME);
            for (Object obj : sqlSchemas) {
                if (obj instanceof Map) {
                    Map<String, Object> _sqlSchema = (Map)obj;
                    if (omrlQuery.connection().equals(_sqlSchema.get("schema"))) {
                        sqlSchema = _sqlSchema;
                        break;
                    }
                }
            }

            Map<String, Object> omrlSql = null;
            if (schema != null || sqlSchema != null) {
                // Check namespaces
                if (!OMRL_SCHEMA_NS.equals(schema.get("$ref"))) {
                    System.err.println("Invalid NameSpace for base schema.");
                    System.exit(1);
                }
                if (!OMRL_SQL_SCHEMA_NS.equals(sqlSchema.get("$ref"))) {
                    System.err.println("Invalid NameSpace for SQL schema.");
                    System.exit(1);
                }
                /*
                 *  HERE IS THE SKILL. Query Generation.
                 */
                OMRL2SQL.usePreparedStmt = USE_PREPARED_STMT;
                omrlSql = OMRL2SQL.omrlToSQLQuery(schema, sqlSchema, jsonQuery);
            } else {
                System.out.printf("Schema [%s] not found.\n", omrlQuery.connection());
                System.exit(1);
            }

            System.out.println("SQL Query:");
            System.out.println(omrlSql.get("query"));

            System.out.println("ResultSet Map:");
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(omrlSql.get("rs-map")));

            List<Object> prmValues = (List)omrlSql.get("prm-values");
            if (prmValues != null && prmValues.size() > 0) {
                System.out.println("Prm Values: " + prmValues.stream()
                        .map(prm -> prm.toString())
                        .collect(Collectors.joining(", ")));
            }

            // Execution?
            if (EXECUTE_QUERY && omrlQuery.execute()) {
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
                Enumeration<Driver> drivers = DriverManager.getDrivers();
                while (drivers.hasMoreElements()) {
                    Driver driver = drivers.nextElement();
                    System.out.println(String.format("Version %d.%d", driver.getMajorVersion(), driver.getMinorVersion()));
                }
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
//                    for (Object prm : prms) { // Not indexOf, actual index!
                    for (int i=0; i<prms.size(); i++) {
                        Object prm = prms.get(i);
//                        System.out.println(prm);
                        if (prm instanceof String) {
                            preparedStatement.setString(i + 1, (String) prm);
                        } else if (prm instanceof Number) {
                            preparedStatement.setDouble(i + 1, Double.parseDouble(prm.toString()));
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
                        Object colValue;
                        /**
                         * Identifies the generic SQL type {@code CHAR}.
                         */
                        if (columnType == JDBCType.INTEGER.getVendorTypeNumber() ||
                                columnType == JDBCType.TINYINT.getVendorTypeNumber() ||
                                columnType == JDBCType.SMALLINT.getVendorTypeNumber() ||
                                columnType == JDBCType.NUMERIC.getVendorTypeNumber() ||
                                columnType == JDBCType.BIGINT.getVendorTypeNumber()) {
//                            colValue = String.format("%d", rs.getInt(i+1)); //  colName));
                            colValue = rs.getInt(i+1); //  colName));
                        } else if (columnType == JDBCType.FLOAT.getVendorTypeNumber() ||
                                columnType == JDBCType.REAL.getVendorTypeNumber() ||
                                columnType == JDBCType.DOUBLE.getVendorTypeNumber() ||
                                columnType == JDBCType.DECIMAL.getVendorTypeNumber()) {
//                            colValue = String.format("%f", rs.getDouble(i+1)); // colName));
                            colValue = rs.getDouble(i+1); // colName));
                        } else if (columnType == JDBCType.VARCHAR.getVendorTypeNumber() ||
                                columnType == JDBCType.CHAR.getVendorTypeNumber()) {
//                            colValue = String.format("%s", rs.getString(i+1)); // colName));
                            colValue = rs.getString(i+1); // colName));
                        } else {
//                            colValue = String.format("%s", rs.getObject(i+1)); // colName)); // Big fallback
                            colValue = rs.getObject(i+1); // colName)); // Big fallback
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
