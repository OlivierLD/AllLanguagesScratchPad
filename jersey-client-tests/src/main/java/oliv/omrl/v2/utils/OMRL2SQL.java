package oliv.omrl.v2.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class OMRL2SQL {
    /**
     * Finds and returns the (full) entity holding the expected CompositeEntity attribute
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
     * Get the DB table name (entity.@table) from the entity name (entity.name)
     * @param entityName
     * @param schema
     * @return the DB table name
     */
    private static String findDBTableByEntityName(String entityName, Map<String, Object> schema) {
        List<Map<String, Object>> entities = (List<Map<String, Object>>)schema.get("entities");
        return entities.stream()
                .filter(ent -> entityName.equals(ent.get("name")))
                .map(ent -> (String)ent.get("@table")).
                findFirst().orElse(null);
    }

    private static String expandWhereClause() {
        return "";
    }

    /**
     * Top most function, callable externally
     * @param schema OMRL mapping Schema
     * @param query the OMRL representation of the query
     * @return a String containing the SQL query to perform.
     */
    public static String omrlToSQLQuery(Map<String, Object> schema, Map<String, Object> query) {
        String sql = "";

        List<String> joinClause = new ArrayList<>();
        Object from = query.get("from"); // We assume ONE element, as a String

        // SELECT
        List<Object> select = (List<Object>)query.get("select");

        List<String> selectClause = new ArrayList<>();
        select.forEach(one -> {
//            System.out.printf("one is a %s\n", one.getClass().getName());
            if (one instanceof List) {
                List<String> oneElem = (List<String>)one;
                if (oneElem.size() == 1) {
                    String oneClause = String.format("%s.%s",
                            findDBTableByEntityName((String)from, schema),
                            oneElem.get(0));
                    selectClause.add(oneClause);
                } else { // Agg function or CompositeEntity (join), size = 2
                    if (oneElem.get(0).startsWith("#")) {
                        String aggFunc = oneElem.get(0).substring(1);
                        String oneClause = String.format("%s(%s)", aggFunc, oneElem.get(1));
                        selectClause.add(oneClause);
                    } else { // CompositeEntity. table.column, and join.
                        System.out.println("CompositeEntity...");
                        // look for a relationship
                        String relationName = oneElem.get(0);
                        String columnName = oneElem.get(1);
                        //
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
                                String dbTableName = findDBTableByEntityName((String)compositeEntityDefinition.get("entity"), schema);
                                String oneClause = String.format("%s.%s",
                                        dbTableName,
                                        columnName);
                                selectClause.add(oneClause);
                                // 2 - JOIN
                                String oneJoinClause = String.format("JOIN %s ON %s.%s = %s.%s",
                                        dbTableName,
                                        dbTableName,
                                        compositeEntityDefinition.get("@foreignKey"),
                                        findDBTableByEntityName((String)holdingEntity.get("name"), schema),
                                        compositeEntityDefinition.get("@targetForeignKey"));
                                if (!joinClause.contains(oneJoinClause)) {
                                    joinClause.add(oneJoinClause);
                                }
                            }
                        } else {
                            System.out.println("Keep searching...");
                        }
                    }
                }
            } else {
                System.out.printf("Un-managed %s in select\n", one.getClass().getName());
            }
        });
        String sqlSelectClause = selectClause.stream()
                .collect(Collectors.joining(", "));

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
            System.out.println("");
        } else {
            System.out.printf("FROM Un-managed [%s]\n", from.getClass().getName());
        }
        String sqlFromClause = fromClause.stream()
                .collect(Collectors.joining(", "));

        // WHERE (and JOIN)
        List<Object> where = (List<Object>)query.get("where");



        List<String> whereClause = new ArrayList<>();

        for (int i=0; i<where.size(); i++) {
            Object cond = where.get(i);
            if (cond instanceof String) {
                if ("AND".equals(cond) || "OR".equals(cond)) {
                    System.out.println("Later");
                } else {
                    if ("=".equals(cond)) { // TODO other comparison operators
                        // Look for a relationship
                        Object left = where.get(i+1);
                        Object right = where.get(i+2);

                        String leftItem = "";
                        String rightItem = "";

                        // manage left item
                        if (left instanceof List) {
                            // look for a relationship
                            String relationName = (String)((List)left).get(0);
                            String columnName = (String)((List)left).get(1);
                            //
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
                                    String dbTableName = findDBTableByEntityName((String)compositeEntityDefinition.get("entity"), schema);
                                    leftItem = String.format("%s.%s",
                                            dbTableName,
                                            columnName);
                                    // 2 - JOIN
                                    String oneJoinClause = String.format("JOIN %s ON %s.%s = %s.%s",
                                            dbTableName,
                                            dbTableName,
                                            compositeEntityDefinition.get("@foreignKey"),
                                            findDBTableByEntityName((String)holdingEntity.get("name"), schema),
                                            compositeEntityDefinition.get("@targetForeignKey"));
                                    if (!joinClause.contains(oneJoinClause)) {
                                        joinClause.add(oneJoinClause);
                                    }
                                }
                            } else {
                                System.out.println("Keep searching...");
                            }
                        } else {
                            System.out.println("Left cond is not a list...");
                        }
                        // manage right item
                        if (right instanceof String) {
                            rightItem = String.format("'%s'", right);
                        } else {
                            System.out.println("Right is not a String");
                        }
                        whereClause.add(String.format("%s = %s", leftItem, rightItem));
                    } // end '='
                }
            }
        } // for, all the where
        String sqlWhereClause = whereClause.stream()
                .collect(Collectors.joining(", ")); // TODO tweak that. not correct.

        String sqlJoinClause = joinClause.stream()
                .collect(Collectors.joining(" "));
        sql = String.format("SELECT %s FROM %s%s%s",
                //                  |       | | |
                //                  |       | | where clause
                //                  |       | join clause
                //                  |       from
                //                  select
                sqlSelectClause,
                sqlFromClause,
                (sqlJoinClause.isEmpty() ? "" : String.format(" %s", sqlJoinClause)),
                (sqlWhereClause.isEmpty() ? "" : String.format(" WHERE %s", sqlWhereClause)));
        return sql;
    }
}
