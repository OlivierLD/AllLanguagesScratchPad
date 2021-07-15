package oliv.oda.dtv3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

public class QueryDecisionTable {

    private final static String DT_JSON = "approval.strategy.dt.json"; // In the resource folder

    private static String jsonStatement;
    private static String userContext;
    private static DecisionTableStaticUtils.QueryOption queryOption = DecisionTableStaticUtils.QueryOption.QUERY;

    private final static String DT_DOCUMENT_PREFIX = "--decision-table:";
    private final static String USER_CONTEXT_PREFIX = "--context-file:";
    private final static String TX_FILE_PREFIX = "--transformation-file:";
    private final static String QUERY_OPTION_PREFIX = "--query-option:";

    public static void main(String... args) throws Exception {

        String cliDT = null;
        URL resource = null;

        for (String arg : args) {
            if (arg.startsWith(DT_DOCUMENT_PREFIX)) {
                cliDT = arg.substring(DT_DOCUMENT_PREFIX.length());
                resource = new File(cliDT).toURI().toURL();
            } else if (arg.startsWith(USER_CONTEXT_PREFIX)) {
                String userContextFileName = arg.substring(USER_CONTEXT_PREFIX.length());
                try (BufferedReader br = new BufferedReader(new FileReader(userContextFileName))) {
                    StringBuffer sb = new StringBuffer();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    userContext = sb.toString();
                }
            } else if (arg.startsWith(TX_FILE_PREFIX)) {
                String txFileName = arg.substring(TX_FILE_PREFIX.length());
                try (BufferedReader br = new BufferedReader(new FileReader(txFileName))) {
                    StringBuffer sb = new StringBuffer();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    jsonStatement = sb.toString();
                }
            } else if (arg.startsWith(QUERY_OPTION_PREFIX)) {
                String option = arg.substring(QUERY_OPTION_PREFIX.length());
                if (option.equalsIgnoreCase("QUERY")) {
                    queryOption = DecisionTableStaticUtils.QueryOption.QUERY;
                } else if (option.equalsIgnoreCase("BAG_ENTITY")) {
                    queryOption = DecisionTableStaticUtils.QueryOption.BAG_ENTITY;
                }
            }
        }

        if (cliDT == null) {
            ClassLoader classLoader = QueryDecisionTable.class.getClassLoader();
            resource = classLoader.getResource(DT_JSON); // At the root of the resources folder.
        }
        System.out.println("Resource: " + resource);
        System.out.println("------- Context -------");
        System.out.println(userContext);
        System.out.println("-----------------------");
        System.out.println("---- Query Statement -----");
        System.out.println(jsonStatement);
        System.out.println("-----------------------");

        String queryResult = DecisionTableStaticUtils.processQuery(resource.openStream(),
                userContext,
                jsonStatement,
                queryOption);

        System.out.println("Query Result:\n" + queryResult);

        System.out.println("Done");
    }
}
