package oliv.omrl.v2.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The method to invoke is {@link #omrlToSQLQuery(Map, Map)}.
 *
 * See https://confluence.oraclecorp.com/confluence/display/IBS/ORML+to+SQL%2C+step+2
 * Earlier version, see https://confluence.oraclecorp.com/confluence/display/IBS/OMRL+to+SQL+-+Scratch+pad
 *
 * Steve's page: https://confluence.oraclecorp.com/confluence/pages/viewpage.action?spaceKey=IBS&title=Learnings+from+OMRL+v0+and+Next+Steps
 * OMRL Current spec: https://confluence.oraclecorp.com/confluence/display/AARCH/OMRL+v0+Representations+and+Schema
 *
 * OMRL: Oracle Meaning Representation Language
 *
 * TODO ORDER-BY, ALIASES
 */
public class OMRL2SQL {

    private final static boolean VERBOSE = false;

    private final static boolean ADD_WHERE_CLAUSE_COLUMNS_TO_SELECT = false;

    /**
     * Finds and returns the (full) entity holding the expected CompositeEntity attribute.
     * The corresponding "attribute" look like this:
     * <pre>
     *     {
     *       "name" : "track",
     *       "type" : "CompositeEntity",
     *       "entity" : "track",
     *       "@foreignKey" : "Track_ID",
     *       "@targetForeignKey" : "Track_ID"
     *     }
     * </pre>
     * @param name name of the expected CompositeEntity
     * @param schema the full schema
     * @return the entity
     */
    private static Map<String, Object> getCompositeEntity(String name, Map<String, Object> schema) {

        List<Map<String, Object>> entities = (List<Map<String, Object>>)schema.get("entities");
        return entities.stream().filter(ent -> {
            List<Map<String, Object>> attributes = (List<Map<String, Object>>)((Map<String, Object>)ent).get("attributes");
            Optional<Map<String, Object>> compositeEntityAttribute = attributes.stream()
                    .filter(att -> "CompositeEntity".equals(att.get("type")) &&
                            name.equals(att.get("name"))).findFirst();
            return compositeEntityAttribute.isPresent(); // the filter condition
        }).findFirst().orElse(null);
    }

    /**
     * Finds and returns the (full) entity holding the expected CompositeArray attribute.
     * The corresponding "attribute" look like this:
     * <pre>
     *     {
     *       "name" : "races",
     *       "type" : "CompositeArray",
     *       "entity" : "race",
     *       "@foreignKey" : "Track_ID",
     *       "@targetForeignKey" : "Track_ID"
     *     }
     * </pre>
     * @param name name of the expected CompositeArray
     * @param schema the full schema
     * @return the entity
     */
    private static Map<String, Object> getCompositeArray(String name, Map<String, Object> schema) {

        List<Map<String, Object>> entities = (List<Map<String, Object>>)schema.get("entities");
        return entities.stream().filter(ent -> {
            List<Map<String, Object>> attributes = (List<Map<String, Object>>)((Map<String, Object>)ent).get("attributes");
            Optional<Map<String, Object>> compositeEntityAttribute = attributes.stream()
                    .filter(att -> "CompositeArray".equals(att.get("type")) &&
                            name.equals(att.get("name"))).findFirst();
            return compositeEntityAttribute.isPresent(); // the filter condition
        }).findFirst().orElse(null);
    }

    /**
     * Get the DB table name (entity.@table) from the entity name (entity.name)
     * @param entityName member "name" of the "entity".
     * @param schema The one to refer to.
     * @return the DB table name, as in "@table"
     */
    private static String findDBTableByEntityName(String entityName, Map<String, Object> schema) {
        List<Map<String, Object>> entities = (List<Map<String, Object>>)schema.get("entities");
        return entities.stream()
                .filter(ent -> entityName.equals(ent.get("name")))
                .map(ent -> (String)ent.get("@table")).
                findFirst().orElse(null);
    }

    /**
     * Resolve entity-name.column-name to db-table-name.column-name
     *
     * @param relationName find the "type": "CompositeEntity" or "CompositeArray" with "name" = relationName, find the "entity" , and get the corresponding "@table"
     * @param columnName
     * @param schema
     * @param joinClause
     * @return
     */
    private static String resolveCompositeEntity(String relationName, String columnName, Map<String, Object> schema, List<String> joinClause) {
        String expandedItem = "";
        Map<String, Object> holdingEntity = getCompositeEntity(relationName, schema);
        if (holdingEntity != null) {
            /* Find the CompositeEntity in the returned entity, like
               {
                  "name" : "track",
                  "type" : "CompositeEntity",
                  "entity" : "track",
                  "@foreignKey" : "Track_ID",
                  "@targetForeignKey" : "Track_ID"
                }
             */
            Map<String, Object> compositeEntityDefinition = ((List<Map<String, Object>>) holdingEntity.get("attributes")).stream()
                    .filter(att -> "CompositeEntity".equals(att.get("type")) &&
                            relationName.equals(att.get("name"))).findFirst().orElse(null);
            if (compositeEntityDefinition == null) {
                throw new RuntimeException(String.format("Should not happen, %s was found, and then gone.", relationName));
            } else {
                // WHERE condition, and JOIN
                // 1 - WHERE
                String dbTableName = findDBTableByEntityName((String) compositeEntityDefinition.get("entity"), schema);
                expandedItem = String.format("%s.%s",
                        dbTableName,
                        columnName);
                // 2 - JOIN
                String oneJoinClause = String.format("JOIN %s ON %s.%s = %s.%s",
                        dbTableName,
                        dbTableName,
                        compositeEntityDefinition.get("@foreignKey"),
                        findDBTableByEntityName((String) holdingEntity.get("name"), schema),
                        compositeEntityDefinition.get("@targetForeignKey"));
                if (!joinClause.contains(oneJoinClause)) {
                    joinClause.add(oneJoinClause);
                }
            }
        } else {
            holdingEntity = getCompositeArray(relationName, schema);
            if (holdingEntity != null) {
            /* Find the CompositeArray in the returned entity, like
               {
                  "name" : "track",
                  "type" : "CompositeArray",
                  "entity" : "track",
                  "@foreignKey" : "Track_ID",
                  "@targetForeignKey" : "Track_ID"
                }
             */
                Map<String, Object> compositeArrayDefinition = ((List<Map<String, Object>>) holdingEntity.get("attributes")).stream()
                        .filter(att -> "CompositeArray".equals(att.get("type")) &&
                                relationName.equals(att.get("name"))).findFirst().orElse(null);
                if (compositeArrayDefinition == null) {
                    throw new RuntimeException(String.format("Should not happen, %s was found, and then gone.", relationName));
                } else {
                    // WHERE condition, and JOIN
                    // 1 - WHERE
                    String dbTableName = findDBTableByEntityName((String) compositeArrayDefinition.get("entity"), schema);
                    expandedItem = String.format("%s.%s",
                            dbTableName,
                            columnName);
                    // 2 - JOIN
                    String oneJoinClause = String.format("JOIN %s ON %s.%s = %s.%s",
                            dbTableName,
                            dbTableName,
                            compositeArrayDefinition.get("@foreignKey"),
                            findDBTableByEntityName((String) holdingEntity.get("name"), schema),
                            compositeArrayDefinition.get("@targetForeignKey"));
                    if (!joinClause.contains(oneJoinClause)) {
                        joinClause.add(oneJoinClause);
                    }
                }
            } else {
                // Raise Exception
                throw new RuntimeException(String.format("Relation name %s not found in the schema.", relationName));
            }
        }
        return expandedItem;
    }

    private static boolean thereIsNoAggFuncIn(List<String> clause) {

        Optional<String> firstAgg = clause.stream().filter(item -> item.startsWith("COUNT(") ||
                        item.startsWith("MIN(") ||
                        item.startsWith("MAX(") ||
                        item.startsWith("AVG("))
                .findFirst();
        return !firstAgg.isPresent();
    }

    private static String expandWhereHavingClauseItem(Object oneMember, Map<String, Object> schema, String tableRef, List<String> joinClause) {
        String expandedItem = "";
        if (oneMember instanceof List) {
            List<String> oneElem = (List<String>)oneMember;
            if (((List<String>)oneMember).size() == 2) {
                if (oneElem.get(0).startsWith("#")) {
                    String aggFunc = oneElem.get(0).substring(1);
                    if ("*".equals(oneElem.get(1))) {
                        expandedItem = String.format("%s(%s)", aggFunc, oneElem.get(1));
                    } else {
                        if (oneElem.size() == 2) { // Current table
                            expandedItem = String.format("%s(%s.%s)", aggFunc, tableRef, oneElem.get(1));
                        } else { // Composite Entity
                            String relationName = oneElem.get(1);
                            String columnName = oneElem.get(2);
                            String resolved = resolveCompositeEntity(relationName, columnName, schema, joinClause);
                            expandedItem = String.format("%s(%s)", aggFunc, resolved);
                        }
                    }
                } else { // CompositeEntity. table.column, and join.
                    // look for a relationship
                    String relationName = (String) ((List) oneMember).get(0);
                    String columnName = (String) ((List) oneMember).get(1);
                    //
                    expandedItem = resolveCompositeEntity(relationName, columnName, schema, joinClause);
                }
            } else { // One column, in the current table.
                String colName = (String) ((List) oneMember).get(0);
                String tableName = findDBTableByEntityName(tableRef, schema);
                expandedItem = String.format("%s.%s", tableName, colName);
            }
        } else if (oneMember instanceof String) {
            expandedItem = String.format("'%s'", oneMember);
        } else {
            expandedItem = String.format("%s", oneMember);
        }
        return expandedItem;
    }

    private static String expandWhereHavingClause(List<Object> where, Map<String, Object> schema, String tableRef, List<String> selectClause, List<String> joinClause) {

        String sqlWhereClause = "";
        if (where.size() == 0) {
            return sqlWhereClause;
        }

        int i = 0;
        String cond = (String)where.get(i);
        if ("AND".equals(cond) || "OR".equals(cond)) {
            // Expect 2 Lists after that. Recurse.
            String left = expandWhereHavingClause((List<Object>)where.get(i + 1), schema, tableRef, selectClause, joinClause);
            String right = expandWhereHavingClause((List<Object>)where.get(i + 2), schema, tableRef, selectClause, joinClause);
            sqlWhereClause = String.format("(%s) %s (%s)", left, cond, right);
        } else {
            String unaryOp = cond;
            Object left = where.get(i+1);
            Object right = where.get(i+2);

            String leftItem = expandWhereHavingClauseItem(left, schema, tableRef, joinClause);
            String rightItem = expandWhereHavingClauseItem(right, schema, tableRef, joinClause);

            if (ADD_WHERE_CLAUSE_COLUMNS_TO_SELECT) {
                // see if the item is in the select clause already
                if (left instanceof List) { // Means it is not a literal
                    if (!selectClause.contains(leftItem) && thereIsNoAggFuncIn(selectClause)) {
                        selectClause.add(leftItem);
                    }
                }
                if (right instanceof List) { // Means it is not a literal
                    if (!selectClause.contains(rightItem) && thereIsNoAggFuncIn(selectClause)) {
                        selectClause.add(rightItem);
                    }
                }
            }
            sqlWhereClause = String.format("%s %s %s", leftItem, unaryOp, rightItem);
        }
        return sqlWhereClause;
    }

    private static String expandOneItem(Object item, Map<String, Object> schema, String tableRef, List<String> joinClause) {
        if (VERBOSE) {
            System.out.printf("Item is a %s\n", item.getClass().getName());
        }
        if (item instanceof List) {
            List<String> oneElem = (List<String>)item;
            if (oneElem.size() == 1) {
                String oneClause = String.format("%s.%s",
                        findDBTableByEntityName(tableRef, schema),
                        oneElem.get(0));
                return oneClause;
            } else { // Agg function or CompositeEntity (join), size = 2
                if (oneElem.get(0).startsWith("#")) {
                    String aggFunc = oneElem.get(0).substring(1);
                    String oneClause;
                    Object prm1 = oneElem.get(1);
//                    System.out.println("oneElem.get(1) type:" + prm1);
                    if ((prm1 instanceof List && "*".equals(((List)prm1).get(0))) || "*".equals(prm1)) {
                        oneClause = String.format("%s(%s)", aggFunc, "*"); // oneElem.get(1));
                    } else {
                        if (oneElem.size() == 2) { // Current table
                            oneClause = String.format("%s(%s.%s)", aggFunc, tableRef, oneElem.get(1));
                        } else { // Composite Entity
                            String relationName = oneElem.get(1);
                            String columnName = oneElem.get(2);
                            String resolved = resolveCompositeEntity(relationName, columnName, schema, joinClause);
                            oneClause = String.format("%s(%s)", aggFunc, resolved);
                        }
                    }
                    return oneClause;
                } else { // CompositeEntity. table.column, and join.
                    if (VERBOSE) {
                        System.out.println("CompositeEntity...");
                    }
                    // look for a relationship
                    String relationName = oneElem.get(0);
                    String columnName = oneElem.get(1);
                    //
                    String expandedItem = resolveCompositeEntity(relationName, columnName, schema, joinClause);
                    return expandedItem;
                }
            }
        } else {
            throw new RuntimeException(String.format("Un-managed %s in select\n", item.getClass().getName()));
        }
//        return null;
    }

    /**
     * Top most function, callable externally
     * @param schema OMRL mapping Schema
     * @param query the OMRL representation of the query
     * @return a String containing the SQL query to perform.
     */
    public static Map<String, Object> omrlToSQLQuery(Map<String, Object> schema, Map<String, Object> query) {
        String sql = "";

        List<String> joinClause = new ArrayList<>();
        Object from = query.get("from"); // We assume ONE element, as a String

        // SELECT
        List<Object> select = (List<Object>)query.get("select");

        List<String> selectClause = new ArrayList<>();
        select.forEach(one -> {
            String expanded = expandOneItem(one, schema, (String)from, joinClause);
            selectClause.add(expanded);
        });
//        String sqlSelectClause = selectClause.stream()
//                .collect(Collectors.joining(", "));

        // FROM
        List<String> fromClause = new ArrayList<>();
        if (from instanceof String) { // Just one entity
            List<Map<String, Object>> entities = (List<Map<String, Object>>)schema.get("entities");
            Optional<Map<String, Object>> entityObject = entities.stream()
                    .filter(entity -> {
                        String name = (String) entity.get("name");
                        return from.equals(name);
                    })
                    .findFirst();
            if (entityObject.isPresent()) {
                Map<String, Object> entityMap = entityObject.get();
                String tableName = (String)entityMap.get("@table");
                fromClause.add(tableName);
            }
        } else {
            if (VERBOSE) {
                System.out.printf("FROM Un-managed [%s]\n", from.getClass().getName());
            }
            throw new RuntimeException(String.format("FROM Un-managed [%s]", from.getClass().getName()));
        }
        String sqlFromClause = fromClause.stream()
                .collect(Collectors.joining(", "));

        // WHERE (and JOIN)
        List<Object> where = (List<Object>)query.get("where");
        String expandedWhere = (where != null ?
                expandWhereHavingClause(where, schema, (String)from, selectClause, joinClause) :
                "");

        // GROUP-BY
        List<Object> groupBy = (List<Object>)query.get("groupBy"); // ""group-by");
        String sqlGroupByClause = "";
        if (groupBy != null) {
            List<String> groupByClause = new ArrayList<>();
            groupBy.forEach(one -> {
                String expanded = expandOneItem(one, schema, (String) from, joinClause);
                groupByClause.add(expanded);
            });
            sqlGroupByClause = groupByClause.stream()
                    .collect(Collectors.joining(", "));
        }

        // HAVING
        List<Object> having = (List<Object>)query.get("having");
        String expandedHaving = (having != null ?
                expandWhereHavingClause(having, schema, (String)from, selectClause, joinClause) :
                "");

        String sqlSelectClause = selectClause.stream()
                .collect(Collectors.joining(", "));

        // Build result-set map
        List<String> resultSetMap = new ArrayList<>();
        final String fromPrefix = String.format("%s.", from);
        selectClause.stream()
                .forEach(one -> {
                    if (one.startsWith(fromPrefix)) {
                        resultSetMap.add(one.substring(fromPrefix.length()));
                    } else {
                        resultSetMap.add(one);
                    }
                });

        // LIMIT
        String sqlLimit = "";
        Object limit = query.get("limit");
        if (limit != null) {
            sqlLimit = String.format("ROWNUM <= %s", limit);
        }

        String sqlJoinClause = joinClause.stream()
                .collect(Collectors.joining(" "));

        if (!sqlLimit.isEmpty()) {
            if (!expandedWhere.isEmpty()) {
                expandedWhere = String.format("(%s) AND (%s)", expandedWhere, sqlLimit);
            } else {
                expandedWhere = sqlLimit;
            }
        }

        sql = String.format("SELECT %s FROM %s%s%s%s%s",
                //                  |       | | | | |
                //                  |       | | | | having
                //                  |       | | | group by clause
                //                  |       | | where clause (and limit)
                //                  |       | join clause
                //                  |       from
                //                  select
                sqlSelectClause,
                sqlFromClause,
                (sqlJoinClause.isEmpty() ? "" : String.format(" %s", sqlJoinClause)),
                (expandedWhere.isEmpty() ? "" : String.format(" WHERE %s", expandedWhere)),
                (sqlGroupByClause.isEmpty() ? "" : String.format(" GROUP BY %s", sqlGroupByClause)),
                (expandedHaving.isEmpty() ? "" : String.format(" HAVING %s", expandedHaving)));
        return Map.of("query", sql, "rs-map", resultSetMap);
    }
}
