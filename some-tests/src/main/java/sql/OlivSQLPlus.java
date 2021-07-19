package sql;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.*;

public class OlivSQLPlus {

    private final static String JDBC_HOSTNAME = "100.111.136.104";
    //    private final static String JDBC_HOSTNAME = "100.102.84.101";
    private final static int JDBC_PORT = 1521;
    private final static String JDBC_SERVICE_NAME = "BOTS.localdomain";

    //    private final static String USERNAME = "sys as SYSDBA"; // ""OMCE_BOTS";
//    private final static String PASSWORD = "DBA4bots12345678!";
    private final static String USERNAME = "races";
    private final static String PASSWORD = "races";

    private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    private static BufferedReader input = stdin;
    private static boolean fromConsole = true;

    public static String userInput(String prompt) {
        String retString = "";
        if (fromConsole) {
            System.err.print(prompt);
        }
        try {
            retString = input.readLine();
        } catch (Exception e) {
            System.out.println(e);
            String s;
            try {
                s = userInput("<Oooch/>");
            } catch (Exception exception) {
            }
        }
        return retString;
    }

    private static boolean connected = false;
    private static Connection connection = null;
    private static boolean keepworking = true;

    private static String serviceName = "";
    private static String username = "";
    private static String password = "";

    public static void main(String... args) throws Exception {
        if (args.length > 0) {
            if (args.length > 0) {
                serviceName = args[0];
            }
            if (args.length > 1) {
                username = args[1];
            }
            if (args.length > 2) {
                password = args[2];
            }
        }
        if (serviceName.isEmpty()) {
            serviceName = JDBC_SERVICE_NAME;
        }
        if (username.isEmpty()) {
            username = USERNAME;
        }
        if (password.isEmpty()) {
            password = PASSWORD;
        }

        while (keepworking) {
            String str = userInput("Sql > ");
            manageCommand(str);
        }
        if (connected) {
            // disconnect
            connection.close();
//            SQLUtil.shutdown(connection);
        }
        System.out.println("Bye...");
    }

    private static void manageCommand(String str) throws Exception {
        if (str.trim().toUpperCase().equals("EXIT") ||
                str.trim().toUpperCase().equals("EXIT;") ||
                str.trim().toUpperCase().equals("QUIT") ||
                str.trim().toUpperCase().equals("QUIT;")) {
            keepworking = false;
        } else if (str.trim().toUpperCase().equals("HELP") ||
                str.trim().toUpperCase().equals("HELP;")) {
            displayHelp();
        } else if (str.trim().toUpperCase().equals("CONNECT") ||
                str.trim().toUpperCase().equals("CONNECT;")) {
            if (!serviceName.isEmpty()) {
                String jdbcUrl = String.format("jdbc:oracle:thin:@//%s:%d/%s",
//                USERNAME,
//                PASSWORD,
                        JDBC_HOSTNAME,
                        JDBC_PORT,
                        JDBC_SERVICE_NAME);

                Class.forName("oracle.jdbc.driver.OracleDriver");
                System.out.println(">> Driver loaded");
                System.out.printf("Connecting with [%s]\n", jdbcUrl);
                connection = DriverManager.getConnection(jdbcUrl, USERNAME, PASSWORD);
                System.out.println(">> Connected");
            } else {
                // connection = SQLUtil.getConnection();
                System.out.println("What??");
            }
            connected = true;
        } else if (str.toUpperCase().startsWith("ECHO ")) {
            System.out.println(str.substring("ECHO ".length()));
        } else if (str.toUpperCase().startsWith("--") || str.toUpperCase().startsWith("REM ")) {
            // Comment
        } else if (str.toUpperCase().startsWith("@")) {
            String filename = str.substring("@".length());
            try {
                BufferedReader br = new BufferedReader(new FileReader(filename));
                input = br;
                fromConsole = false;
                String s = null;
                while ((s = input.readLine()) != null) {
//        System.out.println("[" + s + "]");
                    manageCommand(s);
                }
                input.close();
                input = stdin;
                fromConsole = true;
            } catch (Exception ex) {
                System.err.println("Ooops");
                ex.printStackTrace();
            }
        } else if (str.toUpperCase().trim().startsWith("SELECT")) { // No trailing blank!!
            if (connected) {
                boolean completed = str.trim().endsWith(";");
                while (!completed) {
                    str += (" " + userInput("   + > "));
                    completed = str.trim().endsWith(";");
                }
                str = str.trim();
                if (str.endsWith(";")) { // Remove the trailing ; for Oracle JDBC.
                    str = str.substring(0, str.length() - 1);
                }
                if (false) {
                    System.out.println("Executing");
                    System.out.println(str);
                }
                try {
                    Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(str);
                    ResultSetMetaData rsmd = rs.getMetaData();

                    String header = "";
                    for (int i = 0; i < rsmd.getColumnCount(); i++) {
                        String colName = rsmd.getColumnName(i + 1);
                        int ds = rsmd.getColumnDisplaySize(i + 1);
                        int scale = rsmd.getScale(i + 1);
                        int precision = rsmd.getPrecision(i + 1);
                        header += (colName + "  ");
                    }
                    System.out.println(header);
                    for (int i = 0; i < header.length(); i++) {
                        System.out.print("-");
                    }
                    System.out.println();
                    while (rs.next()) {
                        for (int i = 0; i < rsmd.getColumnCount(); i++) {
                            String col = rs.getString(i + 1);
                            System.out.print(col + "  ");
                        }
                        System.out.println();
                    }
                } catch (Exception ex) {
                    System.out.println("Oops:" + ex.toString());
                }
            } else {
                System.out.println("Not connected. Connect first.");
            }
        } else if (str.trim().toUpperCase().equals("COMMIT;")) {
            if (connected) {
                connection.commit();
            } else {
                System.out.println("Not connected...");
            }
        } else if (str.trim().toUpperCase().equals("ROLLBACK;")) {
            if (connected) {
                connection.rollback();
            } else {
                System.out.println("Not connected...");
            }
        } else if (str.toUpperCase().trim().startsWith("INSERT") ||
                str.toUpperCase().trim().startsWith("DELETE") ||
                str.toUpperCase().trim().startsWith("UPDATE")) {
            boolean completed = str.trim().endsWith(";");
            while (!completed) {
                str += (" " + userInput("   + > "));
                completed = str.trim().endsWith(";");
            }
            if (connected) {
                try {
                    Statement stmt = connection.createStatement();
                    stmt.execute(str.trim());
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            } else {
                System.out.println("Not connected...");
            }
        } else if (str.trim().length() > 0) // Other commands ( CREATE TABLE, etc)
        {
            boolean completed = str.trim().endsWith(";");
            while (!completed) {
                str += (" " + userInput("   + > "));
                completed = str.trim().endsWith(";");
            }
            if (connected) {
                try {
                    Statement stmt = connection.createStatement();
                    stmt.execute(str.trim());
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            } else {
                System.out.println("Not connected...");
            }
        }
    }

    private static void displayHelp() {
        // Commands are case insensitive
        System.out.println("connect[;]");
        System.out.println("select ... ;");
        System.out.println("commit;");
        System.out.println("rollback;");
        System.out.println("echo <whatever string>");
        System.out.println("@<SQL Command File>");
        System.out.println("-- Comment");
        System.out.println("rem Comment");
        System.out.println("help[;]");
        System.out.println("exit[;]");
        System.out.println("quit[;]");
        System.out.println("More help comes soon...");
    }
}
