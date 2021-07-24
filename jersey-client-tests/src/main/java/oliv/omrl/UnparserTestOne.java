package oliv.omrl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static oliv.omrl.utils.OMRLUtils.executeQuery;
import static oliv.omrl.utils.OMRLUtils.unparseToSQL;

/**
 * See the spec:
 * Spider ASDL at https://github.com/ElementAI/duorat/blob/master/duorat/asdl/lang/spider/Spider.asdl
 *
 * Get the query # as System Property -Dquery.index (defaulted to 0)
 */
public class UnparserTestOne {

    private final static boolean verbose = false;
    private final static boolean dumpQueries = true; // To visualize the available queries

//    private final static String TABLE_JSON = "race_track_tables.json";
    private final static String TABLE_JSON = "tables.viraj.json";
    private final static String TRAINED_JSON = "trained_race_track.json";
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
        if (ormlResource != null) {
            jsonOMRLMap = mapper.readValue(ormlResource.openStream(), Map.class);
        } else {
            String indexPrm = System.getProperty("query.index", null);
            // 16: group by, 0 where and order by, 4 min, max, 26 between, 9 distinct, 28 join, 30, 32, another JOIN
            Integer queryIndex = 0; // null; // Set it to the number of the query you want.
            if (indexPrm != null) {
                try {
                    queryIndex = Integer.parseInt(indexPrm);
                } catch (NumberFormatException nfe) {
                    System.err.println(nfe.toString());
                }
            }

            Map<String, Object> firstOne;
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
        String dbId = "race_track"; // Hard-coded for now

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
