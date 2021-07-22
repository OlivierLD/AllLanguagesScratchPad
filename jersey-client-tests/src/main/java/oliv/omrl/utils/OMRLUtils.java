package oliv.omrl.utils;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class OMRLUtils {

    private final static boolean ORACLE_ALIASES = true;

    private final static boolean verbose = false; // This is for debug.

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

    private static String buildWhereClause(String join, String where) {
        String finalWhere = "";
        if (!join.isEmpty() && !where.isEmpty()) {
            finalWhere = String.format(" WHERE (%s) AND (%s)", join, where);
        } else if (!join.isEmpty()) {
            finalWhere = String.format(" WHERE %s", join);
        } else if (!where.isEmpty()) {
            finalWhere = String.format(" WHERE %s", where);
        }
        return finalWhere;
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
            // jsonOMRLMap.get("from")).get("conds"). Joins
            StringBuffer joinClause = new StringBuffer();
            String finalJoin = "";
            List<Object> conds = (List<Object>)((Map<String, Object>)jsonOMRLMap.get("from")).get("conds");
            if (conds != null && conds.size() > 0) {
                conds.stream().forEach(condObj -> {
                    if (condObj instanceof List) {
                        List<Object> cond = (List<Object>)condObj;
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
//                                ((!columnNameOnRight && "text".equals(colType)) ? String.format("'%s'", value1) : value1), // Quotes around if text
                                value1,
                                (opId == 1 ? String.format(" AND %s", val2) : "") // between
//                                ((!columnNameOnRight && "text".equals(colType)) ? String.format("'%s'", value1) : value1), // Quotes around if text
//                                (opId == 1 ? String.format(" AND %s", ("text".equals(colType) ? String.format("'%s'", val2) : val2)) : "") // between
                        );
                        // joinClause.append((joinClause.length() == 0 ? "" : " DUH ") + oneWhereCondition); // TODO Find out the connector AND/OR with parenthesis...
                        joinClause.append(oneWhereCondition);
                    } else {
                        joinClause.append(String.format(" %s ", condObj));
                    }
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
            List<Object> where = (List<Object>)jsonOMRLMap.get("where"); // List of conditions
            StringBuffer whereClause = new StringBuffer();
            if (where != null && where.size() > 0) {
                where.stream().forEach(condObj -> {
                    if (condObj instanceof List) {
                        List<Object> cond = (List<Object>)condObj;
                        boolean notOp = (boolean) cond.get(0); // -, +, x, / . TODO See that
                        int opId = (int) cond.get(1);
                        List<Object> valUnit = (List<Object>) cond.get(2);
                        Object val1 = cond.get(3);
                        Object val2 = cond.get(4);
                        int aggIdx = (int) ((List<Object>) valUnit.get(1)).get(0);
                        int colIdx = (int) ((List<Object>) valUnit.get(1)).get(1);
                        String colName = getQualifiedColumnName(colIdx, schemaColumnList);
//                            (String) ((List<Object>) schemaColumnList.get(colIdx)).get(1);
                        String columnName = String.format("%s",
                                (aggIdx == 0 ? colName : String.format("%s(%s)", AGG_OPS.get(aggIdx), colName)));
                        String colType = schemaColumnTypeList.get(colIdx);
                        String op = WHERE_OPS.get(opId);
                        String value1 = val1.toString();
                        String oneWhereCondition = String.format("%s %s %s%s",
                                columnName,
                                op,
//                                ("text".equals(colType) ? String.format("'%s'", value1) : value1), // Quotes around if text
                                value1,
//                                (opId == 1 ? String.format(" AND %s", ("text".equals(colType) ? String.format("'%s'", val2) : val2)) : "")
                                (opId == 1 ? String.format(" AND %s", val2) : "")
                        );
                        // whereClause.append((whereClause.length() == 0 ? "" : " DUH ") + oneWhereCondition); // TODO Find out the connector AND/OR with parenthesis...
                        whereClause.append(oneWhereCondition);
                    } else {
                        whereClause.append(String.format(" %s ", condObj));
                    }
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

            // Manage HAVING clause
            String finalHavingClause = "";
            List<Object> having = (List<Object>)jsonOMRLMap.get("having"); // List of conditions
            StringBuffer havingClause = new StringBuffer();
            if (having != null && having.size() > 0) {
                having.stream().forEach(condObj -> {
                    if (condObj instanceof List) {
                        List<Object> cond = (List<Object>)condObj;
                        boolean notOp = (boolean) cond.get(0); // -, +, x, / . TODO See that
                        int opId = (int) cond.get(1);
                        List<Object> valUnit = (List<Object>) cond.get(2);
                        Object val1 = cond.get(3);
                        Object val2 = cond.get(4);
                        int aggIdx = (int) ((List<Object>) valUnit.get(1)).get(0);
                        int colIdx = (int) ((List<Object>) valUnit.get(1)).get(1);
                        String colName = getQualifiedColumnName(colIdx, schemaColumnList);
//                            (String) ((List<Object>) schemaColumnList.get(colIdx)).get(1);
                        String columnName = String.format("%s",
                                (aggIdx == 0 ? colName : String.format("%s(%s)", AGG_OPS.get(aggIdx), colName)));
                        String colType = schemaColumnTypeList.get(colIdx);
                        String op = WHERE_OPS.get(opId);
                        String value1 = val1.toString();
                        String oneWhereCondition = String.format("%s %s %s%s",
                                columnName,
                                op,
//                                ("text".equals(colType) ? String.format("'%s'", value1) : value1), // Quotes around if text
                                value1,
//                                (opId == 1 ? String.format(" AND %s", ("text".equals(colType) ? String.format("'%s'", val2) : val2)) : "")
                                (opId == 1 ? String.format(" AND %s", val2) : "")
                        );
                        havingClause.append(oneWhereCondition);
                    } else {
                        havingClause.append(String.format(" %s ", condObj));
                    }
                });
                finalHavingClause = havingClause.toString();
            }
            if (verbose) {
                System.out.println("HAVING: " + finalHavingClause);
            }

            // LIMIT (does Oracle support it?)
            Integer limit = (Integer)jsonOMRLMap.get("limit");
            String queryLimit = "";
            if (limit != null) {
                queryLimit = String.format(" LIMIT %d", limit);
            }

            // TODO intersect, union, except

            // Final query
            String tableList = queryTableNames.stream()
                    .map(tName -> String.format("%s %sT%d", tName, ORACLE_ALIASES ? "" : "AS ",schemaTableNames.indexOf(tName) + 1))
                    .collect(Collectors.joining(", "));

            finalQuery = String.format("SELECT %s FROM %s%s%s%s%s%s",
                    //                         |       | | | | | |
                    //                         |       | | | | | Limit
                    //                         |       | | | | Order By
                    //                         |       | | | Having
                    //                         |       | | Group By
                    //                         |       | where
                    //                         |       Table list
                    //                         Column list
                    querySelect.stream().collect(Collectors.joining(", ")),
                    tableList, // With Aliases
                    buildWhereClause(finalJoin, finalWhereClause), // WHERE
                    (finalGroupByClause.isEmpty() ? "" : " GROUP BY " + finalGroupByClause),
                    (finalHavingClause.isEmpty() ? "" : " HAVING " + finalHavingClause),
                    (finalOrderByClause.isEmpty() ? "" : " ORDER BY " + finalOrderByClause),
                    (queryLimit.isEmpty() ? "" : queryLimit));
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
                    Object colValue;
                    if (columnType == JDBCType.INTEGER.getVendorTypeNumber()) {
                        colValue = rs.getInt(colName);    // String.format("%d", rs.getInt(colName));
                    } else if (columnType == JDBCType.VARCHAR.getVendorTypeNumber()) {
                        colValue = rs.getString(colName); // String.format("%s", rs.getString(colName));
                    } else {
                        colValue = rs.getObject(colName); // String.format("%s", rs.getObject(colName)); // Big fallback
                    }
                    oneLine.put(String.format("%s_COL_%d", colName, i), colValue);
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
}
