package oliv.omrl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JDBCUrlParser {

    private final static String JDBC_URL = "jdbc:oracle:thin:@//100.111.136.104:1521/BOTS.localdomain";

    private final static String JDBC_UR_PATTERN_STR = "^.*\\/\\/(.*):([0-9]*)\\/(.*)$";
    //                                                           |    |          |
    //                                                           |    |          Group 3 (domain)
    //                                                           |    Group 2 (port)
    //                                                           Group 1 (host)
    private final static Pattern JDBC_URL_PATTERN = Pattern.compile(JDBC_UR_PATTERN_STR);
    // ...and what else?

    public static void main(String... args) {
        Matcher matcher = JDBC_URL_PATTERN.matcher(JDBC_URL);
        if (matcher.matches()) {
            String one = matcher.group(1);
            String two = matcher.group(2);
            String three = matcher.group(3);

            System.out.println("Machine is " + one);
            System.out.println("Port is " + two);
            System.out.println("Domain is " + three);
        } else {
            System.out.println("Oops");
        }
    }
}
