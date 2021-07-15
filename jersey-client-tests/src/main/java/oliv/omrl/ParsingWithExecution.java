package oliv.omrl;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Execute a given query.
 */
public class ParsingWithExecution {

    private final static String DEV_JSON = "dev.json"; // In the resource folder
    private final static String TABLES_JSON = "tables.json"; // In the resource folder

    private final static String DEV_DOCUMENT_PREFIX = "--dev:";
    private final static String TABLES_STATEMENT_PREFIX = "--tables:";

    // TODO A parameter for that one...
    private final static String DEFAULT_SQLITE_DB_PATH = "/Users/olivierlediouris/repos/oracle/OMRL_v0_PoC/duorat/data/database/%s/%s.sqlite";

    public static void main(String... args) throws IOException, SQLException {
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
            ClassLoader classLoader = ParsingWithExecution.class.getClassLoader();
            devResource = classLoader.getResource(DEV_JSON); // At the root of the resources folder.
        } else {
            devResource = new File(dev).toURI().toURL();
        }
        if (tables == null) {
            ClassLoader classLoader = ParsingWithExecution.class.getClassLoader();
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
        List<String> dbIds = jsonDevMap.stream().map(one -> (String) ((Map) one).get("db_id")).distinct().collect(Collectors.toList());
        System.out.printf("%d distinct DB_IDs\n", dbIds.size());
        dbIds.stream().forEach(db -> {
            long nb = jsonDevMap.stream().filter(one -> db.equals((String) ((Map) one).get("db_id"))).count();
            System.out.printf("%s - %d entries\n", db, nb);
        });

        System.out.println("+-----------------------------------------------+");

        // Given query

        String toFind = "Show name, country, age for all singers ordered by age from the oldest to the youngest.";

        Optional<Object> query = jsonDevMap.stream().filter(one -> toFind.equals((String) ((Map) one).get("question"))).findFirst();
        Map<String, Object> theOne = (Map<String, Object>) query.get();
        String dbId = (String) theOne.get("db_id");
        String dbQuery = (String) theOne.get("query");
        String question = (String) theOne.get("question");
        System.out.printf("Longest query, on %s, %s\n", dbId, dbQuery);
        System.out.printf("Question is [%s]\n", question);

        String dbPath = String.format(DEFAULT_SQLITE_DB_PATH, dbId, dbId);

        Connection dbConnection = null;
        String dbURL = String.format("jdbc:sqlite:%s", dbPath);

        try {
            Class.forName("org.sqlite.JDBC");
            dbConnection = DriverManager.getConnection(dbURL);

            if (true) {
                DatabaseMetaData dm = dbConnection.getMetaData();
                System.out.println("-------------------------------------");
                System.out.println("Driver name: " + dm.getDriverName());
                System.out.println("Driver version: " + dm.getDriverVersion());
                System.out.println("Product name: " + dm.getDatabaseProductName());
                System.out.println("Product version: " + dm.getDatabaseProductVersion());
                System.out.println("-------------------------------------");
            }

            String sqlStatement = dbQuery;
            Statement statement = dbConnection.createStatement();
            ResultSet rs = statement.executeQuery(sqlStatement);

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();


            while (rs.next()) {
                List<String> oneLine = new ArrayList<>();
                for (int i=0; i<columnCount; i++) {
                    String colName = metaData.getColumnName(i + 1);
                    int columnType = metaData.getColumnType(i + 1);
                    String colValue = "";
                    if (columnType == JDBCType.INTEGER.getVendorTypeNumber()) {
                        colValue = String.format("%d", rs.getInt(colName));
                    } else if (columnType == JDBCType.VARCHAR.getVendorTypeNumber()) {
                        colValue = String.format("%s", rs.getString(colName));
                    }
                    oneLine.add(String.format("%s: %s", colName, colValue));
                }
                System.out.println(oneLine.stream().collect(Collectors.joining(", ")));
            }
            rs.close();
            statement.close();

            dbConnection.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("+-----------------------------------------------+");

        System.out.println("Done.");
    }

}
