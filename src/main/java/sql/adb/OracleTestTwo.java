package sql.adb;

/*
 * Copied from https://github.com/oracle/oracle-db-examples/blob/master/java/jdbc/ConnectionSamples/DataSourceSample.java
 *
 * See https://www.oracle.com/database/technologies/java-connectivity-to-atp.html
 * https://oracle.github.io/learning-library/data-management-library/autonomous-database/shared/adb-quickstart-workshop/freetier/?lab=pre-register-free-tier-account
 *
=== Create DB User ===
create user races identified by AkeuCoucou_1;
grant create table to races;
grant create session to races;
grant resource to races;
alter user races default tablespace users quota unlimited on users;

=== Create and populate tables ==
DROP TABLE RACE;
DROP TABLE TRACK;

CREATE TABLE TRACK (
    TRACK_ID NUMBER,
    NAME VARCHAR2(48),
    LOCATION VARCHAR2(128),
    SEATING NUMBER,
    YEAR_OPENED NUMBER
);
ALTER TABLE TRACK ADD CONSTRAINT TRACK_PK PRIMARY KEY(TRACK_ID);

CREATE TABLE RACE (
    RACE_ID NUMBER,
    NAME VARCHAR2(48),
    RACE_CLASS VARCHAR2(12),
    RACE_DATE VARCHAR2(64),
    TRACK_ID NUMBER
);
ALTER TABLE RACE ADD CONSTRAINT RACE_PK PRIMARY KEY(RACE_ID);
ALTER TABLE RACE ADD CONSTRAINT RACE_FK_TRACK FOREIGN KEY (TRACK_ID) REFERENCES TRACK(TRACK_ID) ON DELETE CASCADE;

INSERT INTO TRACK VALUES (1, 'Auto Club Speedway', 'Fontana, CA', 92000, 1997);
INSERT INTO TRACK VALUES (2, 'Chicagoland Speedway','Joliet, IL',75000,2001);
INSERT INTO TRACK VALUES (3, 'Darlington Raceway','Darlington, SC',63000,1950);
INSERT INTO TRACK VALUES (4, 'Daytona International Speedway','Daytona Beach, FL',168000,1959);
INSERT INTO TRACK VALUES (5, 'Homestead-Miami Speedway','Homestead, FL',65000,1995);
INSERT INTO TRACK VALUES (6, 'Kansas Speedway','Kansas City, KS',81687,2001);
INSERT INTO TRACK VALUES (7, 'Martinsville Speedway','Ridgeway, VA',65000,1947);
INSERT INTO TRACK VALUES (8, 'Michigan International Speedway','Brooklyn, MI',137243,1968);
INSERT INTO TRACK VALUES (9, 'Phoenix International Raceway','Avondale, AZ',76812,1964);

INSERT INTO  RACE VALUES (1, 'Rolex 24 At Daytona','DP/GT','January 26 January 27',1);
INSERT INTO  RACE VALUES (2, 'Gainsco Grand Prix of Miami','DP/GT','March 29',2);
INSERT INTO  RACE VALUES (3, 'Mexico City 250','DP/GT','April 19',2);
INSERT INTO  RACE VALUES (4, 'Bosch Engineering 250 at VIR','GT','April 27',4);
INSERT INTO  RACE VALUES (5, 'RumBum.com 250','DP/GT','May 17',5);
INSERT INTO  RACE VALUES (6, 'Lime Rock GT Classic 250','GT','May 26',6);
INSERT INTO  RACE VALUES (7, 'Sahlen''s Six Hours of the Glen','DP/GT','June 7',7);

commit;

 */

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class OracleTestTwo {

    // The recommended format of a connection URL is the long format with the connection descriptor.

    // For ATP and ADW - use the TNS Alias name along with the TNS_ADMIN when using 18.3 JDBC driver
    final static String WALLET_PATH = "/Users/olivierlediouris/Wallet_OlivDBOne"; // This is on the machine this code is running on.
    final static String TNS_NAME = "olivdbone_medium"; // From tnsname.ora, in the wallet folder.

    // This user must have been created before running this code.
    final static String DB_USER = "RACES";
    final static String DB_PASSWORD = "AkeuCoucou_1";

    // Insert statements
    static String[] insertStmt = new String[]{
            "INSERT INTO TRACK VALUES (1, 'Auto Club Speedway', 'Fontana, CA', 92000, 1997)",
            "INSERT INTO TRACK VALUES (2, 'Chicagoland Speedway','Joliet, IL',75000,2001)",
            "INSERT INTO TRACK VALUES (3, 'Darlington Raceway','Darlington, SC',63000,1950)",
            "INSERT INTO TRACK VALUES (4, 'Daytona International Speedway','Daytona Beach, FL',168000,1959)",
            "INSERT INTO TRACK VALUES (5, 'Homestead-Miami Speedway','Homestead, FL',65000,1995)",
            "INSERT INTO TRACK VALUES (6, 'Kansas Speedway','Kansas City, KS',81687,2001)",
            "INSERT INTO TRACK VALUES (7, 'Martinsville Speedway','Ridgeway, VA',65000,1947)",
            "INSERT INTO TRACK VALUES (8, 'Michigan International Speedway','Brooklyn, MI',137243,1968)",
            "INSERT INTO TRACK VALUES (9, 'Phoenix International Raceway','Avondale, AZ',76812,1964)",

            "INSERT INTO  RACE VALUES (1, 'Rolex 24 At Daytona','DP/GT','January 26 January 27',1)",
            "INSERT INTO  RACE VALUES (2, 'Gainsco Grand Prix of Miami','DP/GT','March 29',2)",
            "INSERT INTO  RACE VALUES (3, 'Mexico City 250','DP/GT','April 19',2)",
            "INSERT INTO  RACE VALUES (4, 'Bosch Engineering 250 at VIR','GT','April 27',4)",
            "INSERT INTO  RACE VALUES (5, 'RumBum.com 250','DP/GT','May 17',5)",
            "INSERT INTO  RACE VALUES (6, 'Lime Rock GT Classic 250','GT','May 26',6)",
            "INSERT INTO  RACE VALUES (7, 'Sahlen''s Six Hours of the Glen','DP/GT','June 7',7)"
    };
    
    /*
     * The code gets a database connection using
     * oracle.jdbc.pool.OracleDataSource. It also sets some connection
     * level properties, such as,
     * OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH,
     * OracleConnection.CONNECTION_PROPERTY_THIN_NET_CHECKSUM_TYPES, etc.,
     * There are many other connection related properties. Refer to
     * the OracleConnection interface to find more.
     */
    public static void main(String... args) throws SQLException {

        Properties info = new Properties();
        info.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME, DB_USER);
        info.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, DB_PASSWORD);
        info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");

        String dbURL = String.format("jdbc:oracle:thin:@%s?TNS_ADMIN=%s", TNS_NAME, WALLET_PATH);
        System.out.println(String.format("Connecting using %s...", dbURL));

        OracleDataSource ods = new OracleDataSource();
        ods.setURL(dbURL);
        ods.setConnectionProperties(info);

        // With AutoCloseable, the connection is closed automatically.
        try (OracleConnection connection = (OracleConnection) ods.getConnection()) {
            // Get the JDBC driver name and version
            DatabaseMetaData dbmd = connection.getMetaData();
            System.out.println("Driver Name: " + dbmd.getDriverName());
            System.out.println("Driver Version: " + dbmd.getDriverVersion());
            // Print some connection properties
            System.out.println("Default Row Prefetch Value is: " +
                    connection.getDefaultRowPrefetch());
            System.out.println("Database Username is: " + connection.getUserName());
            System.out.println();

            // Part one - drop and re-create tables and constraints
            try {
                executeStatement("DROP TABLE RACE", connection);
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
            try {
                executeStatement("DROP TABLE TRACK", connection);
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
            try {
                executeStatement("CREATE TABLE TRACK (\n" +
                        "    TRACK_ID NUMBER,\n" +
                        "    NAME VARCHAR2(48),\n" +
                        "    LOCATION VARCHAR2(128),\n" +
                        "    SEATING NUMBER,\n" +
                        "    YEAR_OPENED NUMBER\n" +
                        ")", connection);
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
            try {
                executeStatement("ALTER TABLE TRACK ADD CONSTRAINT TRACK_PK PRIMARY KEY(TRACK_ID)", connection);
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
            try {
                executeStatement("CREATE TABLE RACE (\n" +
                        "    RACE_ID NUMBER,\n" +
                        "    NAME VARCHAR2(48),\n" +
                        "    RACE_CLASS VARCHAR2(12),\n" +
                        "    RACE_DATE VARCHAR2(64),\n" +
                        "    TRACK_ID NUMBER\n" +
                        ")", connection);
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
            try {
                executeStatement("ALTER TABLE RACE ADD CONSTRAINT RACE_PK PRIMARY KEY(RACE_ID)", connection);
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
            try {
                executeStatement("ALTER TABLE RACE ADD CONSTRAINT RACE_FK_TRACK FOREIGN KEY (TRACK_ID) REFERENCES TRACK(TRACK_ID) ON DELETE CASCADE", connection);
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
//            try {
//                executeStatement("", connection);
//            } catch (SQLException sqlEx) {
//                sqlEx.printStackTrace();
//            }

            connection.setAutoCommit(false);

            // Part two - Populate the tables
            Arrays.stream(insertStmt).forEach(stmt -> {
                try {
                    executeStatement(stmt, connection);
                } catch (SQLException sqlEx) {
                    sqlEx.printStackTrace();
                }
            });

            // Commit ? If auto commit is off (on by default).
            connection.commit();

            // Part three - Perform database operation(s)
            String sqlSelect;
//            sqlSelect = "select /* low */ c_city, c_region, count(*)\n" +
//                    "from ssb.customer c_low\n" +
//                    "group by c_region, c_city\n" +
//                    "order by count(*)";

            sqlSelect = "SELECT * FROM TAB";
            System.out.println("Tables:");
            executeQuery(sqlSelect, connection);
            System.out.println("----------------");

            sqlSelect = "SELECT track.name, " +
                    "COUNT(race.race_id), " +
                    "COUNT(*) FROM race " +
                    "JOIN track ON track.Track_ID = race.Track_ID " +
                    "GROUP BY track.name " +
                    "HAVING COUNT(race.race_id) > 1";

            System.out.println(String.format("A Query: %s", sqlSelect));
            System.out.println("----------------");
            executeQuery(sqlSelect, connection);
            System.out.println("----------------");

            System.out.println("Done!");
        }
    }

    private static void executeStatement(String sql, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private static void executeQuery(String sql, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(sql)) {

                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (resultSet.next()) {
                    List<Object> oneRow = new ArrayList<>();
                    for (int i = 0; i < columnCount; i++) {
                        oneRow.add(resultSet.getObject(i + 1));
                    }
                    System.out.println(oneRow.stream().map(obj -> String.valueOf(obj)).collect(Collectors.joining(", ")));
                }
            }
        }
    }
}
