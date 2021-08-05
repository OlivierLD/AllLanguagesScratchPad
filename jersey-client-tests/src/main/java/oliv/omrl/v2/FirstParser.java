package oliv.omrl.v2;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Hard coded file names
 */
public class FirstParser {

    private final static String OMRL_SCHEMA_PATH = "omrl.airports.schema.01.json";
    private final static String OMRL_QUERY_PATH  = "omrl.airports.query.01.json";

    private static String getTableNameFromLink(String linkName, Map<String, Object> omrlSchema) {
        String tableName = "null";
        List<Map<String, Object>> tables = (List<Map<String, Object>>)omrlSchema.get("tables");
        Optional<Map<String, Object>> linkObj = tables.stream() // Find the table
                .filter(table -> {
                    List<Object> links = (List<Object>) table.get("links");
                    if (links != null) {
                        for (int i = 0; i < links.size(); i++) {
                            Map<String, Object> link = (Map<String, Object>) links.get(i);
                            if (linkName.equals(link.get("link_name"))) {
                                return true;
                            }
                        }
                    }
                    return false;
                }).findFirst();
        if (linkObj.isPresent()) {
            Map<String, Object> table = linkObj.get();
            List<Map<String, String>> links = (List<Map<String, String>>)table.get("links");
            tableName = links.stream()
                    .filter(link -> linkName.equals(link.get("link_name")))
                    .map(link -> link.get("linked_table"))
                    .findFirst()
                    .get();
        }
        return tableName;
    }

    private static String expandWhereClauseElement(Object obj, Map<String, Object> omrlSchema) {
        String expanded = "";
        if (obj instanceof String) {
            expanded = (String) obj;
        } else if (obj instanceof Map) {
            Map<String, Object> elMap = (Map<String, Object>)obj;
            String op = (String)elMap.get("op");
            if ("link_column".equals(op)) {
                String linkName = (String)elMap.get("link_name");
                String colName = (String)elMap.get("column_name");
                // Look for the link_name in the schema
                String tableName = getTableNameFromLink(linkName, omrlSchema);
                expanded = String.format("%s.%s", tableName, colName);
            } else {
                System.out.printf("Un-managed op: %s\n", op);
            }
        } else {
            System.out.printf("Un-managed where element type %s.", obj.getClass().getName());
            expanded = "-";
        }
        return expanded;
    }

    private static String omrlToSQLQuery(Map<String, Object> omrlSchema, Map<String, Object> omrlQuery) {

        String sql;

        List<Object> selectColumns = (List<Object>)omrlQuery.get("select");
        List<Object> fromTables = (List<Object>)omrlQuery.get("from");
        List<Object> whereConds = (List<Object>)omrlQuery.get("where");

        // SELECT
        List<String> select = new ArrayList<>();
        selectColumns.forEach(col -> {
//            System.out.printf("col is a %s\n", col.getClass().getName());
            if (col instanceof Map) {
                Map<String, Object> colMap = (Map<String, Object>)col;
                String op = (String)colMap.get("op");
                if (op != null) {
                    String selectElement = String.format("%s(%s)", op, colMap.get("arg"));
                    select.add(selectElement);
                }
            } else {
                System.out.println("TODO Select...");
            }
        });
        String selectClause = select.stream().collect(Collectors.joining(", "));

        // FROM
        String fromClause = fromTables.stream().map(t -> (String) t).collect(Collectors.joining(", "));

        // WHERE
        List<String> where = new ArrayList<>();
        whereConds.forEach(cond -> {
            if (cond instanceof Map) {
                Map<String, Object> condMap = (Map<String, Object>)cond;
                String op = (String)condMap.get("op");
                if (op != null) {
                    Object arg1 = condMap.get("arg1");
                    Object arg2 = condMap.get("arg2");
//                    System.out.printf("arg1 is a %s, arg2 is a %s\n",
//                            arg1.getClass().getName(),
//                            arg2.getClass().getName());
                    String left, right;
                    left = expandWhereClauseElement(arg1, omrlSchema);
                    right = expandWhereClauseElement(arg2, omrlSchema);

                    where.add(String.format("%s %s %s", left, op, right));
                } else {
                    System.out.println(">> Other case.");
                }
            } else {
                System.out.println("TODO where...");
            }
        });
        String whereClause = where.stream().collect(Collectors.joining(" AND ")); // TODO AND, OR, etc

        sql = String.format("SELECT %s FROM %s WHERE %s",
                selectClause,
                fromClause,
                whereClause);
        return sql;
    }

    public static void main(String... args) {
        URL schemaResource;
        URL queryResource;

        try {
            schemaResource = new File(OMRL_SCHEMA_PATH).toURI().toURL();
            queryResource = new File(OMRL_QUERY_PATH).toURI().toURL();

            ObjectMapper mapper = new ObjectMapper();
            // Schema Object
            Map<String, Object> schema = mapper.readValue(schemaResource.openStream(), Map.class);
            // Query Object
            Map<String, Object> query = mapper.readValue(queryResource.openStream(), Map.class);
            System.out.println("Done generating resources.");

            String sql = omrlToSQLQuery(schema, query);
            System.out.println("SQL Query:");
            System.out.println(sql);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
