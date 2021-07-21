package oliv.omrl;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * See the spec:
 * Spider ASDL at https://github.com/ElementAI/duorat/blob/master/duorat/asdl/lang/spider/Spider.asdl
 *
 * Get the query # as System Property -Dquery.index (defaulted to 0)
 */
public class UnparserTestOne {

    private final static boolean ORACLE_ALIASES = true;

    private final static boolean verbose = false;
    private final static boolean dumpQueries = true; // To visualize the available queries

    private final static String TABLE_JSON = "race_track_tables.json";
    private final static String TRAINED_JSON = "trained_race_track.json";
    private final static String OMRL_JSON = "";
    private final static String DEFAULT_SQLITE_DB_PATH = "/Users/olivierlediouris/repos/oracle/OMRL_v0_PoC/duorat/data/database/%s/%s.sqlite";

    private final static List<String> WHERE_OPS =
            Arrays.asList("not", "between", "=", ">", "<", ">=", "<=", "!=", "in", "like", "is", "exists");
    private final static List<String> AGG_OPS =
            Arrays.asList("none", "max", "min", "count", "sum", "avg");

    private static String getQualifiedColumnName(int colIdx, List<Object> schemaColumnList) {
        int tableIdx = (int)((List<Object>) schemaColumnList.get(colIdx)).get(0);
        String columnName = (String) ((List<Object>) schemaColumnList.get(colIdx)).get(1);
        String qualifiedName = (tableIdx == -1) ? columnName : String.format("T%d.%s", (tableIdx + 1), columnName);

        return qualifiedName;
    }

    public static String unparseToSQL(String dbId, List<Object> jsonTableMap, Map<String, Object> jsonOMRLMap) {

        String finalQuery;

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
            // 2 lists used several times below
            List<Object> schemaColumnList = (List<Object>)schema.get("column_names_original");
            List<String> schemaColumnTypeList = (List<String>)schema.get("column_types");

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
            // jsonOMRLMap.get("from")).get("conds"). Joins?
            StringBuffer joinClause = new StringBuffer();
            String finalJoin = "";
            List<List<Object>> conds = (List<List<Object>>)((Map<String, Object>)jsonOMRLMap.get("from")).get("conds");
            if (conds != null && conds.size() > 0) {
                conds.stream().forEach(cond -> {
                    boolean notOp = (boolean) cond.get(0); // -, +, x, / . TODO See that
                    int opId = (int) cond.get(1);
                    List<Object> valUnit = (List<Object>) cond.get(2);
                    Object val1 = cond.get(3);
                    Object val2 = cond.get(4);
                    int colIdx = (int) ((List<Object>) valUnit.get(1)).get(1);

//                    String tableName = schemaTableNames.get((int) ((List<Object>) schemaColumnList.get(colIdx)).get(0));
                    String columnName = getQualifiedColumnName(colIdx, schemaColumnList);
//                            (String) ((List<Object>) schemaColumnList.get(colIdx)).get(1);
                    String colType = schemaColumnTypeList.get(colIdx);
                    String op = WHERE_OPS.get(opId);
                    String value1;
                    boolean columnNameOnRight = false;
                    if (val1 instanceof List) {
                        // A valUnit
                        int rightColIdx = (int) ((List<Object>) val1).get(1);
                        // String righTableName = schemaTableNames.get((int) ((List<Object>) schemaColumnList.get(rightColIdx)).get(0));
                        String rightColumnName = getQualifiedColumnName(rightColIdx, schemaColumnList);
                        // (String) ((List<Object>) schemaColumnList.get(rightColIdx)).get(1);
                        value1 = /*righTableName + "." + */ rightColumnName;
                        columnNameOnRight = true;
                    } else {
                        value1 = val1.toString();
                    }
                    String oneWhereCondition = String.format("%s %s %s%s",
                            columnName,
                            op,
                            ((!columnNameOnRight && "text".equals(colType)) ? String.format("'%s'", value1) : value1), // Quotes around if text
                            (opId == 1 ? String.format(" AND %s", ("text".equals(colType) ? String.format("'%s'", val2) : val2)) : "")
                    );
                    joinClause.append((joinClause.length() == 0 ? "" : " DUH ") + oneWhereCondition); // TODO Find out the connector AND/OR with parenthesis...
                });
                finalJoin = joinClause.toString();
            }

            if (verbose) {
                String tableList = queryTableNames.stream()
                        .map(tName -> String.format("%s %sT%d", tName, ORACLE_ALIASES ? "" : "AS ",schemaTableNames.indexOf(tName) + 1))
                        .collect(Collectors.joining(", "));
                System.out.println("FROM TABLE(S): " + tableList);
            }
            if (verbose) {
                System.out.println(">> Three");
            }
            // Manage SELECT clause
            List<Object> select = (List<Object>)jsonOMRLMap.get("select");
            boolean isDistinct = (Boolean)select.get(0); // Global level?
            List<List<Object>> agg = (List<List<Object>>)select.get(1); // The column
            List<String> querySelect = new ArrayList<>();
            agg.stream().forEach(one -> {
                int aggId = (int)one.get(0);
                List<Object> valUnit = (List<Object>)one.get(1);
                int unitOp = (int)valUnit.get(0); // TODO Manage that one
                List<Object> colUnit1 = (List<Object>)valUnit.get(1);
                List<Object> colUnit2 = (List<Object>)valUnit.get(2);

                int colAggId = (int)colUnit1.get(0); // TODO What do we use that one for? aggId does the job
                int colIndex = (int)colUnit1.get(1);
                boolean colIsDistinct = (boolean)colUnit1.get(2); // TODO Manage that one?
                int tableIndex = (Integer)((List<Object>)schemaColumnList.get(colIndex)).get(0);
//                String tableName = schemaTableNames.get(tableIndex);
                String colName = getQualifiedColumnName(colIndex, schemaColumnList);
//                        (String)((List<Object>)schemaColumnList.get(colIndex)).get(1);
                String fullColumnName = colName; // tableIndex > -1 ? String.format("%s.%s", schemaTableNames.get(tableIndex), colName) : colName;

                querySelect.add(String.format("%s%s",
                        (isDistinct ? "DISTINCT " : ""),
                        (aggId == 0 ? fullColumnName : String.format("%s(%s)", AGG_OPS.get(aggId), fullColumnName))));

                if (colUnit2 != null) {
                    // TODO Do it.
                    System.out.println("Bam.");
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
                    String columnName = getQualifiedColumnName(colIdx, schemaColumnList);
//                            (String) ((List<Object>) schemaColumnList.get(colIdx)).get(1);
                    String colType = schemaColumnTypeList.get(colIdx);
                    String op = WHERE_OPS.get(opId);
                    String value1 = val1.toString();
                    String oneWhereCondition = String.format("%s %s %s%s",
                            columnName,
                            op,
                            ("text".equals(colType) ? String.format("'%s'", value1) : value1), // Quotes around if text
                            (opId == 1 ? String.format(" AND %s", ("text".equals(colType) ? String.format("'%s'", val2) : val2)) : "")
                            );
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
                List<String> queryOrderByClause = new ArrayList<>();
                orderByColumns.stream().forEach(col -> {
                    int colIdx = (int)(((List<Object>)col.get(1)).get(1));
                    int aggIdx = (int)(((List<Object>)col.get(1)).get(0));
                    String columnName = getQualifiedColumnName(colIdx, schemaColumnList);
                            // (String) ((List<Object>) schemaColumnList.get((int)(((List<Object>)col.get(1)).get(1)))).get(1);
                    queryOrderByClause.add(String.format("%s",
                            (aggIdx == 0 ? columnName : String.format("%s(%s)", AGG_OPS.get(aggIdx), columnName))) + " " + globalOrder); // TODO Not good
                });
                finalOrderByClause = queryOrderByClause.stream().collect(Collectors.joining(", "));
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
                    String colName = getQualifiedColumnName(colIdx, schemaColumnList);
//                            (String)((List<Object>)schemaColumnList.get(colIdx)).get(1);

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
            String tableList = queryTableNames.stream()
                    .map(tName -> String.format("%s %sT%d", tName, ORACLE_ALIASES ? "" : "AS ",schemaTableNames.indexOf(tName) + 1))
                    .collect(Collectors.joining(", "));

            finalQuery = String.format("SELECT %s FROM %s%s%s%s%s",
                    //                         |       | | | | |
                    //                         |       | | | | Order By
                    //                         |       | | | Group By
                    //                         |       | | where
                    //                         |       | Join
                    //                         |       Table list
                    //                         Column list
                    querySelect.stream().collect(Collectors.joining(", ")),
                    tableList, // With Aliases
                    (finalJoin.isEmpty() ? "" : " WHERE " + finalJoin),                       // JOIN !!!! TODO Concatenate with WHERE?
                    (finalWhereClause.isEmpty() ? "" : " WHERE " + finalWhereClause),
                    (finalGroupByClause.isEmpty() ? "" : " GROUP BY " + finalGroupByClause),
                    (finalOrderByClause.isEmpty() ? "" : " ORDER BY " + finalOrderByClause));
        } else {
            System.out.println("Schema not found");
            throw new RuntimeException(String.format("Schema [%s] not found.", dbId));
        }
        return finalQuery;
    }

    public static List<Map<String, Object>> executeQuery(Connection dbConnection, String sqlQuery) throws Exception {

        List<Map<String, Object>> result = new ArrayList<>();
        try {
            // This is a dynamic execution, we do not know anything about the query, the columns it returns, etc.
            String sqlStatement = sqlQuery;
            Statement statement = dbConnection.createStatement();
            ResultSet rs = statement.executeQuery(sqlStatement);

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> oneLine = new HashMap<>(); // One row.
                for (int i = 0; i < columnCount; i++) {
                    String colName = metaData.getColumnName(i + 1);
                    int columnType = metaData.getColumnType(i + 1);
                    // TODO Ensure column name unicity, with col aliases?
                    Object colValue;
                    if (columnType == JDBCType.INTEGER.getVendorTypeNumber()) {
                        colValue = rs.getInt(colName);    // String.format("%d", rs.getInt(colName));
                    } else if (columnType == JDBCType.VARCHAR.getVendorTypeNumber()) {
                        colValue = rs.getString(colName); // String.format("%s", rs.getString(colName));
                    } else {
                        colValue = rs.getObject(colName); // String.format("%s", rs.getObject(colName)); // Big fallback
                    }
                    oneLine.put(colName, colValue);
                }
                result.add(oneLine);
            }
            rs.close();
            statement.close();
        } catch (Exception ex) {
            throw ex;
        }
        return result;
    }

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
                        System.out.println(row);
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
