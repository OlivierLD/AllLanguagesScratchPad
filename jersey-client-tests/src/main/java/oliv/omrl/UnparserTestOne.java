package oliv.omrl;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * See the spec:
 * Spider ASDL at https://github.com/ElementAI/duorat/blob/master/duorat/asdl/lang/spider/Spider.asdl
 */
public class UnparserTestOne {

    private static boolean verbose = false;
    private static boolean dumpQueries = true; // To visualize the available queries

    private final static String TABLE_JSON = "race_track_tables.json";
    private final static String TRAINED_JSON = "trained_race_track.json";
    private final static String OMRL_JSON = "";

    private final static List<String> WHERE_OPS =
            Arrays.asList("not", "between", "=", ">", "<", ">=", "<=", "!=", "in", "like", "is", "exists");
    private final static List<String> AGG_OPS =
            Arrays.asList("none", "max", "min", "count", "sum", "avg");

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
            // 16: group by, 0 where and order by, 4 min. max
            Integer queryIndex = 0; // null; // Set it to the number of the query you want.

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
        // 1 - Find the right schema in table.json
        if (verbose) {
            System.out.println(">> One");
        }
        Optional<Map<String, Object>> optDbId = jsonTableMap.stream()
                .filter(schema -> dbId.equals(((Map<String, Object>) schema).get("db_id")))
                .map(duh -> (Map<String, Object>)duh)
                .findFirst();
        if (optDbId.isPresent()) {
            Map<String, Object> schema = optDbId.get();
            if (verbose) {
                System.out.println(">> Two");
            }
            // Get the table name(s)
            List<List<Object>> tableUnits = (List<List<Object>>)((Map<String, Object>) jsonOMRLMap.get("from")).get("table_units");
            // Table Units contain the index(es) of the tables in the schema["table_names_original"]
            List<String> schemaTableNames = (List<String>)schema.get("table_names_original"); // Definitions
            List<String> queryTableNames = new ArrayList<>(); // List to use in the query
            tableUnits.stream().forEach(tu -> {
//                System.out.println(tu);
                if ("table_unit".equals(tu.get(0))) {
                    queryTableNames.add(schemaTableNames.get((int)tu.get(1)));
                }
            });
            if (verbose) {
                System.out.println("FROM TABLE(S): " + queryTableNames.stream().collect(Collectors.joining(", ")));
            }
            // TODO jsonOMRLMap.get("from")).get("conds") ?
            if (verbose) {
                System.out.println(">> Three");
            }
            // Manage SELECT clause
            List<Object> select = (List<Object>)jsonOMRLMap.get("select");
            boolean isDistinct = (Boolean)select.get(0); // TODO Manage that distinct
            List<List<Object>> agg = (List<List<Object>>)select.get(1); // The column
            List<Object> schemaColumnList = (List<Object>)schema.get("column_names_original");
            List<String> schemaColumnTypeList = (List<String>)schema.get("column_types");
            List<String> querySelect = new ArrayList<>();
            agg.stream().forEach(one -> {
                int aggId = (int)one.get(0);
                List<Object> valUnit = (List<Object>)one.get(1);
                int unitOp = (int)valUnit.get(0); // TODO Manage that one
                List<Object> colUnit1 = (List<Object>)valUnit.get(1);
                List<Object> colUnit2 = (List<Object>)valUnit.get(2);

                int colAggId = (int)colUnit1.get(0); // TODO WHat do we use that one for? aggId does the job
                int colIndex = (int)colUnit1.get(1);
                boolean colIsDistinct = (boolean)colUnit1.get(2); // TODO Manage that one
                String colName = (String)((List<Object>)schemaColumnList.get(colIndex)).get(1);

                querySelect.add(String.format("%s",
                        (aggId == 0 ? colName : String.format("%s(%s)", AGG_OPS.get(aggId), colName))));

                if (colUnit2 != null) {
                    // TODO Do it.
                }
//                System.out.println("Let's loop");
            });
            if (verbose) {
                System.out.println("SELECT : " + querySelect.stream().collect(Collectors.joining(", ")));
            }

            if (verbose) {
                System.out.println(">> Four");
            }
            // Manage WHERE clause
            String finalWhereClause = "";
            List<List<Object>> where = (List<List<Object>>)jsonOMRLMap.get("where"); // List of conditions
            StringBuffer whereClause = new StringBuffer();
            if (where != null && where.size() > 0) {
                where.stream().forEach(cond -> {
                    boolean notOp = (boolean) cond.get(0); // -, +, x, / . TODO See that
                    int opId = (int) cond.get(1);
                    List<Object> valUnit = (List<Object>) cond.get(2);
                    Object val1 = cond.get(3);
                    Object val2 = cond.get(4);
                    int colIdx = (int)((List<Object>)valUnit.get(1)).get(1);
                    String columnName = (String) ((List<Object>) schemaColumnList.get(colIdx)).get(1);
                    String colType = schemaColumnTypeList.get(colIdx);
                    String op = WHERE_OPS.get(opId);
                    String value1 = val1.toString();
                    String oneWhereCondition = String.format("%s %s %s",
                            columnName,
                            op,
                            ("text".equals(colType) ? String.format("'%s'", value1) : value1)); // Quotes around if text
                    whereClause.append((whereClause.length() == 0 ? "" : " DUH ") + oneWhereCondition); // TODO Find out the connector AND/OR with parenthesis...
                });
                finalWhereClause = whereClause.toString();
            }
            if (verbose) {
                System.out.println("WHERE: " + finalWhereClause);
            }

            if (verbose) {
                System.out.println(">> Five");
            }
            // Manage ORDER BY clause
            String finalOrderByClause = "";
            List<Object> orderBy = (List<Object>)jsonOMRLMap.get("orderBy");
            if (orderBy != null && orderBy.size() > 0) { // order_by = (order order, val_unit* val_units). TODO Why an order at the top level? -- 'orderBy': ('asc'/'desc', [val_unit1, val_unit2, ...])
                String globalOrder = (String)orderBy.get(0);
                List<List<Object>> orderByColumns = (List<List<Object>>)orderBy.get(1); // valUnits
                List<String> queryWhereClause = new ArrayList<>();
                orderByColumns.stream().forEach(col -> {
                    String columnName = (String) ((List<Object>) schemaColumnList.get((int)(((List<Object>)col.get(1)).get(1)))).get(1);
                    queryWhereClause.add(columnName + " " + globalOrder); // TODO Not good
                });
                finalOrderByClause = queryWhereClause.stream().collect(Collectors.joining(", "));
            }
            if (verbose) {
                System.out.println("ORDER BY: " + finalOrderByClause);
            }

            if (verbose) {
                System.out.println(">> Six");
            }

            // Manage GROUP BY clause
            String finalGroupByClause = "";
            List<List<Object>> groupBy = (List<List<Object>>)jsonOMRLMap.get("groupBy");
            if (groupBy != null && groupBy.size() > 0) {
                List<String> groupByQuery = new ArrayList<>();
                groupBy.stream().forEach(gb -> { // gb is a col_unit
                    int aggId = (int)gb.get(0); // See AGG_OPS
                    int colIdx = (int)gb.get(1);
                    boolean distinct = (boolean)gb.get(2);
                    String colName = (String)((List<Object>)schemaColumnList.get(colIdx)).get(1);

                    groupByQuery.add(String.format("%s",
                            (aggId == 0 ? colName : String.format("%s(%s)", AGG_OPS.get(aggId), colName))));
                });
                finalGroupByClause = groupByQuery.stream().collect(Collectors.joining(", "));
            }
            if (verbose) {
                System.out.println("GROUP BY: " + finalGroupByClause);
            }

            if (verbose) {
                System.out.println(">> Seven");
            }

            // TODO having, limit, intersect, union, except

            // Final query
            String finalQuery = String.format("SELECT %s FROM %s%s%s%s",
                    querySelect.stream().collect(Collectors.joining(", ")),
                    queryTableNames.stream().collect(Collectors.joining(", ")),
                    (finalWhereClause.isEmpty() ? "" : " WHERE " + finalWhereClause),
                    (finalGroupByClause.isEmpty() ? "" : " GROUP BY " + finalGroupByClause),
                    (finalOrderByClause.isEmpty() ? "" : " ORDER BY " + finalOrderByClause));
            System.out.println("-------------------------------------------------------------");
            System.out.println("Final Query:\n" + finalQuery);
            System.out.println("-------------------------------------------------------------");
        } else {
            System.out.println("Schema not found");
        }
        System.out.println("Done");
    }
}
