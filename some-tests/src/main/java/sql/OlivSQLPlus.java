package sql;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Arrays;

public class OlivSQLPlus {

    private final static String HOST_NAME_PREFIX = "--host-name:";
    private final static String PORT_PREFIX = "--port:";
    private final static String SERVICE_NAME_PREFIX = "--service-name:";
    private final static String USER_PSWD_PREFIX = "--connect:";

    private final static String JDBC_HOSTNAME = "100.111.136.104";
    //    private final static String JDBC_HOSTNAME = "100.102.84.101";
    private final static int JDBC_PORT = 1521;
    private final static String JDBC_SERVICE_NAME = "BOTS.localdomain";

    //    private final static String USERNAME = "sys as SYSDBA"; // ""OMCE_BOTS";
//    private final static String PASSWORD = "DBA4bots12345678!";
    private final static String USERNAME = "races";
    private final static String PASSWORD = "racesracesracesraces";

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

    private static String hostName = "";
    private static int port = -1;
    private static String serviceName = "";
    private static String username = "";
    private static String password = "";

    public static void main(String... args) throws Exception {

        Arrays.asList(args).stream()
                .forEach(arg -> {
                    if (arg.startsWith(HOST_NAME_PREFIX)) {
                        hostName = arg.substring(HOST_NAME_PREFIX.length());
                    } else if (arg.startsWith(PORT_PREFIX)) {
                        port = Integer.parseInt(arg.substring(PORT_PREFIX.length()));
                    } else if (arg.startsWith(SERVICE_NAME_PREFIX)) {
                        serviceName = arg.substring(SERVICE_NAME_PREFIX.length());
                    } else if (arg.startsWith(USER_PSWD_PREFIX)) {
                        String[] userPswd = arg.substring(USER_PSWD_PREFIX.length()).split("/");
                        if (userPswd.length == 2) {
                            username = userPswd[0];
                            password = userPswd[1];
                        } else {
                            throw new RuntimeException(String.format("Invalid connection parameter %s", arg));
                        }
                    }
                });


        if (serviceName.isEmpty()) {
            serviceName = JDBC_SERVICE_NAME;
        }
        if (username.isEmpty()) {
            username = USERNAME;
        }
        if (password.isEmpty()) {
            password = PASSWORD;
        }
        if (port == -1) {
            port = JDBC_PORT;
        }
        if (hostName.isEmpty()) {
            hostName = JDBC_HOSTNAME;
        }

        System.out.println("Type 'help' for help...");

        while (keepworking) {
            String str = userInput("SQL > ");
            manageCommand(str);
        }

        if (connected) {
            // disconnect
            connection.close();
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
        } else if (str.trim().toUpperCase().equals("SHOW") ||
                str.trim().toUpperCase().equals("SHOW;")) {
            System.out.println("Hostname: " + hostName);
            System.out.println("Port    : " + port);
            System.out.println("Service : " + serviceName);
            System.out.println("UserName: " + username);
            System.out.println("Password: " + password);
        } else if (str.trim().toUpperCase().equals("CONNECT") ||
                str.trim().toUpperCase().startsWith("CONNECT ") ||
                str.trim().toUpperCase().equals("CONNECT;")) {
            if (str.trim().toUpperCase().startsWith("CONNECT ") && str.trim().length() > "CONNECT ".length()) {
                String connect = str.trim().substring("CONNECT ".length()).trim();
                String[] userPswd = connect.split("/");
                username = userPswd[0];
                password = userPswd[1];
            }
            if (connected) {
                connection.close();
            }
            if (!serviceName.isEmpty()) {
                String jdbcUrl = String.format("jdbc:oracle:thin:@//%s:%d/%s",
                        hostName,
                        port,
                        serviceName);
                try {
                    Class.forName("oracle.jdbc.driver.OracleDriver");
                    System.out.println(">> Driver loaded");
                    System.out.printf("Connecting with [%s] as %s/%s\n", jdbcUrl, username, password);
                    connection = DriverManager.getConnection(jdbcUrl, username, password);
                    System.out.println(">> Connected");
                    connected = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
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
        System.out.println("-- CLI parameters --");
        System.out.printf("%s<host name or IP> %sPortNum %s<service-name> %suser/pswd\n", HOST_NAME_PREFIX, PORT_PREFIX, SERVICE_NAME_PREFIX, USER_PSWD_PREFIX);
        System.out.println("-- Commands --");
        // Commands are case insensitive
        System.out.println("connect[;]");
        System.out.println("connect user/pswd");
        System.out.println("select ... ;");
        System.out.println("commit;");
        System.out.println("rollback;");
        System.out.println("echo <whatever string>");
        System.out.println("@<SQL Command File>");
        System.out.println("-- Comment");
        System.out.println("rem Comment");
        System.out.println("show[;]");
        System.out.println("help[;]");
        System.out.println("exit[;]");
        System.out.println("quit[;]");
        System.out.println("More help comes soon...");
    }
}
