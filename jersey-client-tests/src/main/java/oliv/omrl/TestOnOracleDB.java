package oliv.omrl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Connect to an Oracle DB
 * Requires a Gradle dependency:
 *     implementation("com.oracle.database.jdbc:ojdbc8-production:21.1.0.0")
 */
public class TestOnOracleDB {

    private final static String JDBC_HOSTNAME = "100.111.136.104";
    //    private final static String JDBC_HOSTNAME = "100.102.84.101";
    private final static int JDBC_PORT = 1521;
    private final static String JDBC_SERVICE_NAME = "BOTS.localdomain";

    //    private final static String USERNAME = "sys as SYSDBA"; // ""OMCE_BOTS";
//    private final static String PASSWORD = "DBA4bots12345678!";
    private final static String USERNAME = "races";
    private final static String PASSWORD = "racesracesracesraces";

    public static void main(String... args) throws ClassNotFoundException, SQLException {

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

        // Try a select stmt
        String sqlStatement = "select count(*) as num_of_races, r.track_id, (select t.name from track t where t.track_id = r.track_id) t_name " +
                "from race r " +
                "group by r.track_id"; // No final semi-column

//        String sqlStatement = "select count(*), track_id from race group by track_id"; // No final semi-column

//        String sqlStatement = "select * from race"; // No final semi-column

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

        System.out.printf(">> Retrieved %d row(s)\n", nbRows);

        connection.close();
        System.out.println(">> Bye");
    }
}
