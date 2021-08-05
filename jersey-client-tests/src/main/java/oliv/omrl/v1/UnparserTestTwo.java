package oliv.omrl.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static oliv.omrl.v1.utils.OMRLUtils.executeQuery;
import static oliv.omrl.v1.utils.OMRLUtils.unparseToSQL;

/**
 * See the spec:
 * Spider ASDL at https://github.com/ElementAI/duorat/blob/master/duorat/asdl/lang/spider/Spider.asdl
 *
 * Get the query # as System Property -Dquery.index (defaulted to 0)
 */
public class UnparserTestTwo {

    private final static boolean verbose = false;
    private final static boolean dumpQueries = true; // To visualize the available queries

    private final static String TABLE_JSON = "/Users/olivierlediouris/repos/oracle/OMRL_v0_PoC/spider_sample_data/tables.json";
    private final static String TRAINED_JSON = "/Users/olivierlediouris/repos/oracle/OMRL_v0_PoC/spider_sample_data/train_spider.json";
    private final static String OMRL_JSON = "";
    private final static String DEFAULT_SQLITE_DB_PATH = "/Users/olivierlediouris/repos/oracle/OMRL_v0_PoC/duorat/data/database/%s/%s.sqlite";

    public static void main(String... args) throws IOException {

        URL trainedResource = new File(TRAINED_JSON).toURI().toURL();
        URL tablesResource = new File(TABLE_JSON).toURI().toURL();
        URL ormlResource = null;
        if (!OMRL_JSON.isEmpty()) {
            ormlResource = new File(OMRL_JSON).toURI().toURL();
        }

        ObjectMapper mapper = new ObjectMapper();

        List<Object> jsonTrainedMap = mapper.readValue(trainedResource.openStream(), List.class);

        if (dumpQueries) {
            AtomicInteger rank = new AtomicInteger(0);
            jsonTrainedMap.forEach(query -> {
                System.out.printf("Query #%d: %s\n", rank.getAndIncrement(), ((Map<String, Object>)query).get("query"));
            });
        }

        List<Object> jsonTableMap = mapper.readValue(tablesResource.openStream(), List.class);
        Map<String, Object> jsonOMRLMap;
        Map<String, Object> firstOne = null;
        if (ormlResource != null) { // Should always be null
            jsonOMRLMap = mapper.readValue(ormlResource.openStream(), Map.class);
        } else {
            String indexPrm = System.getProperty("query.index", null);
            Integer queryIndex = 0; // null; // Set it to the number of the query you want.
            if (indexPrm != null) {
                try {
                    queryIndex = Integer.parseInt(indexPrm);
                } catch (NumberFormatException nfe) {
                    System.err.println(nfe.toString());
                }
            }
            // queryIndex = 137; // multiple where clause
            // queryIndex = 209; // Auto-join . Problem.
            // queryIndex = 6945; // where on text column
            // queryIndex = 6942; // multiple where and join, group by, having
            queryIndex = 6986; // limit

            if (queryIndex == null) {
                firstOne = (Map<String, Object>) jsonTrainedMap.stream()
                        .findFirst()
                        .get();
            } else {
                firstOne = (Map<String, Object>) jsonTrainedMap.get(queryIndex);
            }
            String query = (String)firstOne.get("query");
            jsonOMRLMap = (Map<String, Object>)firstOne.get("sql"); // We assume SQL
            System.out.println("-------------------------------------------------------------");
            System.out.printf("QUERY to Generate: %s\n", query);
            System.out.println("-------------------------------------------------------------");
        }
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonOMRLMap));

        // Let's go
        String dbId = (String)firstOne.get("db_id"); // "race_track";
        System.out.println("DB id: " + dbId);

        try {
            String unparsedQuery = unparseToSQL(dbId, jsonTableMap, jsonOMRLMap);

            System.out.println("-------------------------------------------------------------");
            System.out.println("Final Query:\n" + unparsedQuery);
            System.out.println("-------------------------------------------------------------");

            // Execution
            String dbPath = String.format(DEFAULT_SQLITE_DB_PATH, dbId, dbId);
            // For oracle, would be like "jdbc:oracle:thin:@localhost:1521:xe","system","oracle"
            String dbURL = String.format("jdbc:sqlite:%s", dbPath);
            try {
                // Class.forName("oracle.jdbc.driver.OracleDriver"); // Oracle
                Class.forName("org.sqlite.JDBC"); // SQLite
                Connection dbConnection = DriverManager.getConnection(dbURL);
                if (verbose) {
                    DatabaseMetaData dm = dbConnection.getMetaData();
                    System.out.println("-------------------------------------");
                    System.out.println("Driver name: " + dm.getDriverName());
                    System.out.println("Driver version: " + dm.getDriverVersion());
                    System.out.println("Product name: " + dm.getDatabaseProductName());
                    System.out.println("Product version: " + dm.getDatabaseProductVersion());
                    System.out.println("-------------------------------------");
                }

                List<Map<String, Object>> queryResult = executeQuery(dbConnection, unparsedQuery);
                if (queryResult != null) {
                    System.out.printf("Returned %d row%s\n", queryResult.size(), (queryResult.size() > 1 ? "s" : ""));
                    System.out.println("----- R E S U L T -----");
                    queryResult.forEach(row -> {
//                        System.out.println(row.stream().collect(Collectors.joining(", ")));
                        try {
                            System.out.println(mapper.writeValueAsString(row));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    });
                    System.out.println("-----------------------");
                } else {
                    System.out.println("No row returned.");
                }
                dbConnection.close();
            } catch (Exception sqlEx) {
                sqlEx.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("Done");
    }
}
