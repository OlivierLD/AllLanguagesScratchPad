package oliv.omrl.v2.utils;

// import oracle.cloud.bots.model.dialog.ExpressionResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The method to invoke is {@link #omrlToSQLQuery(Map, Map, Object)}.
 *
 * See https://confluence.oraclecorp.com/confluence/display/IBS/ORML+to+SQL%2C+step+2
 * Earlier version, see https://confluence.oraclecorp.com/confluence/display/IBS/OMRL+to+SQL+-+Scratch+pad
 *
 * Steve's page: https://confluence.oraclecorp.com/confluence/pages/viewpage.action?spaceKey=IBS&title=Learnings+from+OMRL+v0+and+Next+Steps
 * OMRL Current spec: https://confluence.oraclecorp.com/confluence/display/AARCH/OMRL+v0+Representations+and+Schema
 *
 * OMRL: Oracle Meaning Representation Language
 *
 * TODO ALIASES, COLUMN_EXPANSIONS through Relations/Links (WiP)
 */
public class OMRL2SQL {

    private final static boolean VERBOSE = false;

    private final static boolean LOGICAL_RESULT_SET_MAP = true;
    private final static boolean ENTITY_BASED_RESULT_SET_MAP = true;
    public static boolean entityBasedResultMap = ENTITY_BASED_RESULT_SET_MAP;

    private final static boolean ADD_WHERE_CLAUSE_COLUMNS_TO_SELECT = false;
    public static boolean usePreparedStmt = true;

    private final static String SELECT_REGEX = "^(?i)SELECT.*[\\s\\S].*FROM.*[\\s\\S].*"; // Case Insensitive, include NewLines
    private final static Pattern SELECT_PATTERN = Pattern.compile(SELECT_REGEX, Pattern.MULTILINE);

    private final static String COLUMN_EXPANSION_STR = "\\$\\{([^\\}]*)\\}";
    private final static Pattern COLUMN_EXPANSION_PATTERN = Pattern.compile(COLUMN_EXPANSION_STR, Pattern.MULTILINE);

    private final static String ALIAS_DETECTOR_STR = "as \"(.*?)\"";
    private final static Pattern ALIAS_DETECTOR_PATTERN = Pattern.compile(ALIAS_DETECTOR_STR, Pattern.MULTILINE);

    private final static String SELECT = "select";
    private final static String FROM = "from";
    private final static String WHERE = "where";
    private final static String ORDER_BY = "order_by";
    private final static String GROUP_BY = "group_by";
    private final static String HAVING = "having";
    private final static String LIMIT = "limit";
    private final static String DISTINCT = "distinct";  // true | false

    // TODO the names like SQL_MAPPING, COMPOSITE_ENTITY, ENTITY_NAME, etc.

    /**
     * Finds and returns the (full) entity holding the expected CompositeEntity attribute.
     * The corresponding "attribute" look like this:
     * <pre>
     *     {
     *       "name": "track",
     *       "type": "composite_entity",
     *       "entity_name": "track",
     *       "multiple_values": false
     *     }
     * </pre>
     *
     * @param name   name of the expected CompositeEntity
     * @param schema the full base schema
     * @return the entity
     */
    private static Map<String, Object> getCompositeEntity(String fromEntity, String name, Map<String, Object> schema) {

        List<Map<String, Object>> entities = (List<Map<String, Object>>) schema.get("entities");
        Map<String, Object> entity = entities.stream().filter(ent -> fromEntity.equals(ent.get("name"))).findFirst().orElse(null);
        if (entity != null) {
            List<Map<String, Object>> attributes = (List<Map<String, Object>>) (entity).get("attributes");
            Optional<Map<String, Object>> compositeEntityAttribute = attributes.stream()
                    .filter(att -> "composite_entity".equals(att.get("type")) &&
                            name.equals(att.get("name"))).findFirst(); // TODO and multipleValues = false?
            if (compositeEntityAttribute.isPresent()) {
                return entity; // compositeEntityAttribute.get();
            }
        }
        return null;
    }

    private static String getEntityFromRelation(String fromEntity, String relationName, Map<String, Object> schema) {
        String entityRelName = null;
        Object currentEntity = ((List<Object>) schema.get("entities")).stream()
                .filter(entity -> fromEntity.equals(((Map<String, Object>) entity).get("name")))
                .findFirst().orElse(null);
        if (currentEntity != null) {
            Object relAttribute = ((List<Object>) ((Map<String, Object>) currentEntity).get("attributes")).stream()
                    .filter(att -> relationName.equals(((Map<String, Object>) att).get("name")))
                    .findFirst().orElse(null);
            if (relAttribute != null) {
                entityRelName = (String)((Map<String, Object>)relAttribute).get("entity_name");
            }
        }
        return entityRelName;
    }

    /**
     * Get the DB table name (entity.tableName) from the entity name (entity.name)
     *
     * @param entityName member "name" of the "entity".
     * @param sqlSchema  The one to refer to.
     * @return the DB table name, as in "tableName"
     */
    private static String findDBTableByEntityName(String entityName, Map<String, Object> sqlSchema) {
        List<Map<String, Object>> entities = (List<Map<String, Object>>) sqlSchema.get("entities");
        Map<String, Object> sqlMapping = entities.stream()
                .filter(ent -> entityName.equals(ent.get("name")))
                .map(ent -> (Map) ((Map) ent.get("sql_mapping")))
                .findFirst().orElse(null);
        if (sqlMapping != null) {
            String tableName = (String)sqlMapping.get("table_name");
            return tableName;
        }
        return null;
    }

    private static String expandStarTableColumns(String entityName, Map<String, Object> sqlSchema) {

        String expanded = "*";
        List<Map<String, Object>> entities = (List)sqlSchema.get("entities");
        Map<String, Object> entity = entities.stream()
                .filter(_entity -> entityName.equals(_entity.get("name")))
                .findFirst()
                .orElse(null);
        if (entity != null) {
            String tableName = (String)((Map)entity.get("sql_mapping")).get("table_name"); // tableName == null => from SQLSelect
            List<Object> attributes = (List)entity.get("attributes");
            List<String> columnList = new ArrayList<>();
            attributes.stream()
                    .filter(att -> ((Map<String, Object>)((Map)att).get("sql_mapping")).get("column_name") != null)
                    .forEach(att -> {
                        String columnName = (String)((Map<String, Object>)((Map)att).get("sql_mapping")).get("column_name");
                        String tabCol = (tableName != null) ? String.format("%s.%s", tableName, columnName) : String.format("%s", columnName);
                        columnList.add(tabCol);
                    });
            expanded = columnList.stream().collect(Collectors.joining(", "));
        }
        return expanded;
    }

    private static String findDBColumnByEntityName(String entityName, String attributeName, Map<String, Object> sqlSchema) {
        List<Map<String, Object>> entities = (List<Map<String, Object>>) sqlSchema.get("entities");
        Map<String, Object> entity = entities.stream()
                .filter(ent -> entityName.equals(ent.get("name")))
                .findFirst().orElse(null);
        if (entity != null) {
            Stream<Map<String, Object>> attributes = ((List<Map<String, Object>>) entity.get("attributes")).stream()
                    .filter(att -> attributeName.equals(att.get("name")));
            // Try column name
            String matchingColumn = attributes
                    .filter(att -> {
                        String columnName = (String) ((Map)att.get("sql_mapping")).get("column_name");
                        if (columnName == null) {
                            if (((Map)att.get("sql_mapping")).get("foreign_key") != null) {
                                columnName = (String) ((List) ((Map)att.get("sql_mapping")).get("foreign_key")).get(0); // TODO Make sure that works...
                            }
                        }
                        return columnName != null;
                    })
                    .map(att -> {
                        String columnName = (String) ((Map)att.get("sql_mapping")).get("column_name");
                        if (columnName == null) {
                            if (((Map)att.get("sql_mapping")).get("foreign_key") != null) {
                                columnName = (String) ((List) ((Map)att.get("sql_mapping")).get("foreign_key")).get(0); // TODO Make sure that works...
                            }
                        }
                        return columnName;
                    }).findFirst().orElse(null);
            return matchingColumn; // .isPresent() ? matchingColumn.get() : null;
        } else {
            return null;
        }
    }

    private static List<String> findEntityPrimaryKey(String entityName, Map<String, Object> sqlSchema) {

        return (List<String>) ((List) sqlSchema.get("entities")).stream()
                .filter(entity -> entityName.equals(((Map) entity).get("name")))
                .map(entity -> ((Map)((Map) entity).get("sql_mapping")).get("primary_key"))
                .findFirst().orElse(null);
    }

    private static List<String> findEntityForeignKey(String entityName, String linkEntityName, Map<String, Object> sqlSchema) {

        Map<String, Object> _entity = (Map) ((List) sqlSchema.get("entities")).stream()
                .filter(entity -> entityName.equals(((Map) entity).get("name"))).findFirst().orElse(null);
        if (_entity != null) {
            return (List<String>) ((List) _entity.get("attributes")).stream()
                    .filter(attribute -> linkEntityName.equals(((Map) attribute).get("name")))
                    .map(attribute -> ((Map) ((Map)attribute).get("sql_mapping")).get("foreign_key"))
                    .findFirst().orElse(null);
        } else {
            return null;
        }
    }

    private static String generateOnJoinClause(String fromTable, List<String> pkColumns, String toTable, List<String> fkColumns) {
        // %s.%s = %s.%s AND ...
        assert(pkColumns.size() == fkColumns.size());

        StringBuffer onClause = new StringBuffer();
        onClause.append(String.format("(%s.%s = %s.%s)", fromTable, pkColumns.get(0), toTable, fkColumns.get(0)));
        for (int i=1; i<pkColumns.size(); i++) {
            onClause.append(String.format(" AND (%s.%s = %s.%s)", fromTable, pkColumns.get(i), toTable, fkColumns.get(i)));
        }
        return onClause.toString();
    }

    /**
     * Resolve entity-name.column-name to db-table-name.column-name
     *
     * @param relationName
     * @param attributeName
     * @param schema
     * @param joinClause
     * @return
     */
    private static String resolveCompositeEntity(String fromEntityName,
                                                 String relationName,
                                                 String attributeName,
                                                 Map<String, Object> schema,
                                                 Map<String, Object> sqlSchema,
                                                 List<String> joinClause,
                                                 List<Object> where,
                                                 List<Object> usedForExpansion) {
        return resolveCompositeEntity(fromEntityName, relationName, attributeName, schema, sqlSchema, joinClause, where, usedForExpansion, false);
    }
    private static String resolveCompositeEntity(String fromEntityName,
                                                 String relationName,
                                                 String attributeName,
                                                 Map<String, Object> schema,
                                                 Map<String, Object> sqlSchema,
                                                 List<String> joinClause,
                                                 List<Object> where,
                                                 List<Object> usedForExpansion,
                                                 boolean inWhereClause) {
        String expandedItem = "";
        Map<String, Object> holdingEntity = getCompositeEntity(fromEntityName, relationName, schema);
        if (holdingEntity != null) {
            /* Find the CompositeEntity in the returned entity, like
               {
                    "name": "races",
                    "type": "composite_entity",
                    "entity_name": "race",
                    "multiple_values": true
                }
             */
            Map<String, Object> compositeEntityDefinition = ((List<Map<String, Object>>) holdingEntity.get("attributes")).stream()
                    .filter(att -> "composite_entity".equals(att.get("type")) &&
                            relationName.equals(att.get("name"))).findFirst().orElse(null);
            if (compositeEntityDefinition == null) {
                throw new RuntimeException(String.format("Should not happen, %s was found, and then gone.", relationName));
            } else {
                // WHERE condition, and JOIN
                // 1 - WHERE
                String entityName = (String) compositeEntityDefinition.get("entity_name");
                String dbTableName = findDBTableByEntityName(entityName, sqlSchema);
                if ("*".equals(attributeName)) {
                    expandedItem = expandStarTableColumns(entityName, sqlSchema);
                } else {
                    // sqlExpression ?
                    String sqlExp = sqlExpression(entityName, attributeName, sqlSchema);
                    if (sqlExp != null) {
                        if (inWhereClause) {
                            expandedItem = String.format("(%s)", sqlExp);
                        } else {
                            expandedItem = String.format("(%s) as %s", sqlExp, attributeName);
                        }
                    } else if (isColumnExpansion(entityName, attributeName, sqlSchema)) { // Column Expansion ?
                        // Use the where clause to find the patch values
                        List<String> expandedColumns = expandColumns(entityName,
                                attributeName,
                                schema,
                                sqlSchema,
                                where,
                                usedForExpansion);
                        if (expandedColumns != null && expandedColumns.size() > 0) {
                            expandedItem = expandedColumns.stream().collect(Collectors.joining(", "));
                        } else {
                            expandedItem = "";
                        }
                    } else {
                        String columnName = findDBColumnByEntityName(entityName, attributeName, sqlSchema);
                        expandedItem = String.format("%s.%s",
                                dbTableName,
                                columnName);
                    }
                }
                // 2 - JOIN
                List<String> keyFrom;
                if ((boolean)compositeEntityDefinition.get("multiple_values")) {
                    keyFrom = findEntityForeignKey((String) compositeEntityDefinition.get("entity_name"), (String) holdingEntity.get("name"), sqlSchema);
                } else {
                    keyFrom = findEntityPrimaryKey((String) compositeEntityDefinition.get("entity_name"), sqlSchema);
                }
                String toTable = findDBTableByEntityName((String) holdingEntity.get("name"), sqlSchema);
                List<String> fkTo = findEntityForeignKey((String) holdingEntity.get("name"), (String) compositeEntityDefinition.get("name"), sqlSchema);
                String oneJoinClause = String.format("JOIN %s ON %s",
                        dbTableName,
                        generateOnJoinClause(
                                dbTableName,
                                keyFrom, // PK or FK
                                toTable,
                                fkTo  // FK
                        )
                );
//                        findEntityForeignKey((String)holdingEntity.get("name"), (String)compositeEntityDefinition.get("entityName"), sqlSchema)); //FK
                if (!joinClause.contains(oneJoinClause)) {
                    joinClause.add(oneJoinClause);
                }
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

    private static Object expandWhereHavingClauseItem(Object oneMember,
                                                      Map<String, Object> schema,
                                                      Map<String, Object> sqlSchema,
                                                      String tableRef,
                                                      List<String> joinClause) {
        return expandWhereHavingClauseItem(oneMember, schema, sqlSchema, tableRef, joinClause, null, null);
    }

    private static Object expandWhereHavingClauseItem(Object oneMember,
                                                      Map<String, Object> schema,
                                                      Map<String, Object> sqlSchema,
                                                      String entityRef,
                                                      List<String> joinClause,
                                                      List<Object> where,
                                                      List<Object> usedForExpansion) {
        Object expandedItem = null;
        if (oneMember instanceof List) {
            List<Object> oneElem = (List<Object>) oneMember;
            if (((List<Object>) oneMember).size() > 1) {
                if (oneElem.get(0) instanceof String && ((String)oneElem.get(0)).startsWith("#")) {
                    String aggFunc = ((String)oneElem.get(0)).substring(1);
                    if ("*".equals(oneElem.get(1))) {
                        expandedItem = String.format("%s(%s)", aggFunc, oneElem.get(1));
                    } else {
                        if (oneElem.size() == 2) { // Current table
                            if (oneElem.get(1) instanceof List) {
                                String targetEntity = entityRef;
                                if (((List)oneElem.get(1)).size() > 1) {
                                    targetEntity = String.valueOf(((List)oneElem.get(1)).get(0));
                                }
                                String tableName = findDBTableByEntityName(targetEntity, sqlSchema);
                                String attName = ((List)oneElem.get(1)).size() > 1 ?
                                        String.valueOf(((List)oneElem.get(1)).get(1)) :
                                        String.valueOf(((List)oneElem.get(1)).get(0));
                                String colName = findDBColumnByEntityName(entityRef, attName, sqlSchema);
                                if (colName != null) {
                                    expandedItem = String.format("%s(%s.%s)",
                                            aggFunc,
                                            tableName,
                                            colName);
                                } else { // Consider it a character literal. TODO What if a character literal is also an attribute name?
                                    if (((List)oneElem.get(1)).get(0) instanceof String) {
                                        expandedItem = String.format("%s('%s')",
                                                aggFunc,
                                                ((List) oneElem.get(1)).get(0));
                                    } else {
                                        expandedItem = String.format("%s(%s)",
                                                aggFunc,
                                                ((List) oneElem.get(1)).get(0));
                                    }
                                }
                            } else {
//                                expandedItem = String.format("%s(%s.%s)", aggFunc, entityRef, oneElem.get(1));
                                expandedItem = String.format("%s(%s.%s)",
                                        aggFunc,
                                        findDBTableByEntityName(entityRef, sqlSchema),
                                        findDBColumnByEntityName(entityRef, (String)oneElem.get(1), sqlSchema)); // Pfooo...
                            }
                        } else { // Composite Entity
                            String relationName = (String)oneElem.get(1);
                            String columnName = (String)oneElem.get(2);
                            String resolved = resolveCompositeEntity(entityRef, relationName, columnName, schema, sqlSchema, joinClause, where, usedForExpansion,true); // TODO tableRef?
                            expandedItem = String.format("%s(%s)", aggFunc, resolved);
                        }
                    }
                } else { // CompositeEntity. table.column, and join. Or column_expansion
                    // look for a relationship
                    String relationName = (String) ((List) oneMember).get(0);
                    String columnName = (String) ((List) oneMember).get(1);
                    // Manage column_expansion here
                    if (isColumnExpansion(entityRef, columnName, sqlSchema) && // column_expansion at the entity level (no relation)
                            columnName.equals(relationName)) { // like [ "cost", "cost" ]
                        // Use the where clause to find the patch values
                        String attributeName = columnName;
                        List<String> expandedColumns = expandColumns(entityRef,
                                attributeName,
                                schema,
                                sqlSchema,
                                where,
                                usedForExpansion);
                        if (expandedColumns != null && expandedColumns.size() > 0) {
                            expandedItem = expandedColumns.stream()
                                    .map(col -> String.format("%s", col))
                                    .collect(Collectors.joining(", "));
                        } else {
                            expandedItem = "";
                        }
                    } else {
                        expandedItem = resolveCompositeEntity(entityRef, relationName, columnName, schema, sqlSchema, joinClause, where, usedForExpansion, true); // TODO tableRef?
                    }
                }
            } else { // One column, in the current table.
                String attName = (String) ((List) oneMember).get(0);
                String colName = findDBColumnByEntityName(entityRef, attName, sqlSchema);
                String tableName = findDBTableByEntityName(entityRef, sqlSchema);
                // sqlExpression ?
                String sqlExp = sqlExpression(entityRef, attName, sqlSchema);
                if (sqlExp != null) {
//                    expandedItem = String.format("(%s) as %s", sqlExp, colName);
                    expandedItem = String.format("(%s)", sqlExp);
                } else if (isColumnExpansion(entityRef, (String) oneElem.get(0), sqlSchema)) { // Column Expansion ? TODO Should not happen here anymore
                    // Use the where clause to find the patch values
                    List<String> expandedColumns = expandColumns(entityRef,
                            (String) oneElem.get(0),
                            schema,
                            sqlSchema,
                            where,
                            usedForExpansion);
                    if (expandedColumns != null && expandedColumns.size() > 0) {
                        // Will generate the condition for each item?
                        expandedItem = expandedColumns.stream().collect(Collectors.joining(", "));
                    } else {
                        expandedItem = "";
                    }
                } else {
                    // tableName == null => from SQLSelect
                    expandedItem = (tableName != null) ? String.format("%s.%s", tableName, colName) : String.format("%s", colName);
                }
            }
        } else if (oneMember instanceof String) {
            if (!usePreparedStmt) {
                expandedItem = String.format("'%s'", oneMember);
            } else {
                expandedItem = String.format("%s", oneMember);
            }
        } else if (oneMember instanceof Map) { // Like a NOT IN select...
            if (((Map)oneMember).get("select") != null) { // select and from are mandatory members of an OMRL query
                Map<String, Object> stringObjectMap = OMRL2SQL.omrlToSQLQuery(schema, sqlSchema, oneMember);
                expandedItem = String.format("(%s)", stringObjectMap.get("query"));
            } else {
                // TODO What's that?
            }
        } else {
            // Type this guy?
            if (!usePreparedStmt) {
                expandedItem = String.format("%s", oneMember);
            } else {
                expandedItem = oneMember;
            }
        }
        return expandedItem;
    }

    private static void rescanExpansionList(List<Object> where,
                                            List<Object> originalWhere,
                                            Map<String, Object> schema,
                                            Map<String, Object> sqlSchema,
                                            String from,
                                            List<Object> usedForExpansion) {
        if (where == null || where.size() == 0) {
            return;
        }

        int i = 0;
        String cond = (String) where.get(i);
        if ("AND".equals(cond) || "OR".equals(cond)) {
            // Expect 2 Lists after that. Recurse.
//            rescanExpansionList((List<Object>) where.get(i + 1),
//                    originalWhere,
//                    schema,
//                    sqlSchema,
//                    from,
//                    usedForExpansion);
            for (int w=1 /*2*/; w<where.size(); w++) {
                rescanExpansionList((List<Object>) where.get(i + w),
                        originalWhere,
                        schema,
                        sqlSchema,
                        from,
                        usedForExpansion);
            }
        } else {
            String unaryOp = cond;
            Object left = where.get(i + 1);
            Object right = where.get(i + 2); // Not used

//            System.out.println("Left is a " + left.getClass().getName());
            String fromEntity = from;
            String attributeName = "";
            if (left instanceof List) {
                attributeName = (String)((List)left).get(0);
                if (((List)left).size() == 2) {
                    fromEntity = (String)((List)left).get(0); // TODO This can be the column_to_expand name...
                    if (((List)left).get(1) instanceof String) {
                        attributeName = (String) ((List) left).get(1);
                    }
                }
            } else {
                // TODO Honk?
                System.out.println("Atch!");
            }
            boolean colExpPrm = isColumnExpansion(fromEntity, attributeName, sqlSchema); // like [ "cost", "year" ]
            boolean entExp = isColumnExpansion(from, fromEntity, sqlSchema);             // like [ "race", "cost" ]
            if (usedForExpansion != null && (colExpPrm || entExp)) {
                if (colExpPrm) {
                    List<String> unused = expandColumns(fromEntity,
                            attributeName,
                            schema,
                            sqlSchema,
                            originalWhere,
                            usedForExpansion); // Just here to add to usedForExpansion if needed
                } else {
                    List<String> unused = expandColumns(from,
                            fromEntity,
                            schema,
                            sqlSchema,
                            originalWhere,
                            usedForExpansion); // Just here to add to usedForExpansion if needed
                }
            }
        }
    }

    private static String expandWhereHavingClause(List<Object> where,
                                                  Map<String, Object> schema,
                                                  Map<String, Object> sqlSchema,
                                                  String entityRef,
                                                  List<String> selectClause,
                                                  List<String> joinClause,
                                                  List<Object> prmValues) {
        return expandWhereHavingClause(where,
                null,
                schema,
                sqlSchema,
                entityRef,
                selectClause,
                joinClause,
                prmValues, null);
    }

    private static String expandWhereHavingClause(List<Object> where,
                                                  List<Object> originalWhere,
                                                  Map<String, Object> schema,
                                                  Map<String, Object> sqlSchema,
                                                  String entityRef,
                                                  List<String> selectClause,
                                                  List<String> joinClause,
                                                  List<Object> prmValues,
                                                  List<Object> usedForExpansion) {
        String sqlWhereClause = "";
        if (where.size() == 0) {
            return sqlWhereClause;
        }

        int i = 0;
        String cond = (String) where.get(i);
        if ("AND".equals(cond) || "OR".equals(cond)) { // TODO Ignore case?
            // Expect 2 (or more) Lists after that. Recurse.
            List<String> whereClauseElements = new ArrayList<>();
            for (int w=1; w<where.size(); w++) {
                String one = expandWhereHavingClause((List<Object>) where.get(i + w),
                        originalWhere,
                        schema,
                        sqlSchema,
                        entityRef,
                        selectClause,
                        joinClause,
                        prmValues,
                        usedForExpansion);
                whereClauseElements.add(one);
            }
            // Remove blanks & nulls. This can happen when there are column expansions
            List<String> noBlanks = whereClauseElements.stream()
                    .filter(elmt -> elmt != null && !elmt.isEmpty())
                    .collect(Collectors.toList());
            whereClauseElements = noBlanks;

            if (whereClauseElements.size() > 0) {
                if (whereClauseElements.size() == 1) {
                    sqlWhereClause = whereClauseElements.get(0);
                } else {
                    sqlWhereClause = String.format("(%s) %s (%s)", whereClauseElements.get(0), cond, whereClauseElements.get(1));
                    for (int w=2; w<whereClauseElements.size(); w++) {
                        sqlWhereClause = String.format("%s %s (%s)", sqlWhereClause, cond, whereClauseElements.get(w));
                    }
                }
            } else {
                sqlWhereClause = "";
            }
        } else {
            String unaryOp = cond;
//            System.out.println("Where size:" + where.size());
            assert (where.size() >= 3);
            Object left = where.get(i + 1);
            Object right = where.get(i + 2);
            Object thirdValue = null;

            if (usedForExpansion != null && "=".equals(unaryOp)) { // For expansion, assume '='
//                System.out.println(left);
                if (usedForExpansion.contains(left)) {
//                    System.out.printf("%s was used for column expansion\n", left);
                    return null;
                }
            }

            // TODO Consolidate expandWhereHavingClauseItem and expandOneItem
            Object leftItem;
            if (left instanceof List && ((List)left).size() > 2) {
                leftItem = expandOneItem(left, schema, sqlSchema, entityRef, joinClause, originalWhere, usedForExpansion);
            } else {
                leftItem = expandWhereHavingClauseItem(left, schema, sqlSchema, entityRef, joinClause, originalWhere, usedForExpansion);
            }
//            Object leftItem = expandOneItem(left, schema, sqlSchema, tableRef, joinClause, originalWhere, usedForExpansion);
            Object rightItem;
            if (right instanceof List && ((List)right).size() > 2) {
                rightItem = expandOneItem(right, schema, sqlSchema, entityRef, joinClause, originalWhere, usedForExpansion);
            } else {
                rightItem = expandWhereHavingClauseItem(right, schema, sqlSchema, entityRef, joinClause, originalWhere, usedForExpansion);
            }
//            Object rightItem = expandOneItem(right, schema, sqlSchema, tableRef, joinClause, originalWhere, usedForExpansion);
            Object thirdItem = null; // For BETWEEN

            if ("BETWEEN".equalsIgnoreCase(unaryOp)) {
                if (where.size() > 3) {
                    thirdValue = where.get(i + 3);
                    thirdItem = expandWhereHavingClauseItem(thirdValue, schema, sqlSchema, entityRef, joinClause, originalWhere, usedForExpansion);
                } else {
                    // Bad Syntax
                }
            }
            boolean rightIsASelectStmt = false;
            // This is a trick. This can also be generated from OMRL too.
            if ("NOT IN".equalsIgnoreCase(unaryOp) || "IN".equalsIgnoreCase(unaryOp)) {
                if (right instanceof String) {
                    final Matcher matcher = SELECT_PATTERN.matcher((String) right);
                    if (matcher.find()) {
                        rightIsASelectStmt = true;
                        rightItem = String.format("(%s)", right);
                    }
                }
            }

            if (!rightIsASelectStmt && usePreparedStmt) {
                // Literal, or literal in a function
                if (right instanceof List) {
                    if (String.valueOf(((List)right).get(0)).startsWith("#")) {
                        if (((List)right).get(1) instanceof List && ((List)(((List)right).get(1))).size() == 1) {
                            String value = String.valueOf(((List)(((List)right).get(1))).get(0));
                            prmValues.add(value);
                            rightItem = String.format("%s(?)", String.valueOf(((List)right).get(0)).substring(1));
                        }
                    }
                } else {
                    prmValues.add(rightItem); // And put the value in its list
                    rightItem = "?";
                    if (thirdItem != null) {
                        prmValues.add(thirdItem);
                        thirdItem = "?";
                    }
                }
            }

            if (ADD_WHERE_CLAUSE_COLUMNS_TO_SELECT) {
                // see if the item is in the select clause already
                if (left instanceof List) { // Means it is not a literal
                    if (!selectClause.contains(leftItem) && thereIsNoAggFuncIn(selectClause)) {
                        selectClause.add((String) leftItem); // TODO Verify that String cast
                    }
                }
                if (right instanceof List) { // Means it is not a literal
                    if (!selectClause.contains(rightItem) && thereIsNoAggFuncIn(selectClause)) {
                        selectClause.add((String) rightItem); // TODO Verify that String cast
                    }
                }
            }
            if (left instanceof List && ((List)left).size() > 0 && isColumnExpansion(entityRef, (String)((List)left).get(0), sqlSchema)) {
                // If column expansion generate mor than one column, then apply condition on each of them, and connect them with OR.
                String[] colArray = ((String)leftItem).split(",");
                sqlWhereClause = String.format("%s %s %s", colArray[0].trim(), unaryOp, rightItem);
                for (int colIndex=1; colIndex< colArray.length; colIndex++) {
                    sqlWhereClause = String.format("%s OR %s %s %s", sqlWhereClause, colArray[colIndex].trim(), unaryOp, rightItem);
                }
            } else {
                if ("BETWEEN".equalsIgnoreCase(unaryOp)) {
                    sqlWhereClause = String.format("%s %s %s AND %s", leftItem, unaryOp, rightItem, thirdItem);
                } else {
                    sqlWhereClause = String.format("%s %s %s", leftItem, unaryOp, rightItem);
                }
            }
        }
        return sqlWhereClause;
    }

    private static String sqlExpression(String entity, String attribute, Map<String, Object> sqlSchema) {
        List<Object> entities = (List)sqlSchema.get("entities");
        Object attObj = entities.stream()
                .filter(ent -> entity.equals(((Map<String, Object>) ent).get("name")))
                .map(ent -> ((Map<String, Object>) ent).get("attributes"))
                .findFirst().orElse(null);
        if (attObj != null) {
            if (attObj instanceof List) {
                List<Map<String, Object>> attList = (List)attObj;
                Map<String, Object> attr = attList.stream()
                        .filter(att -> attribute.equals(att.get("name")))
                        .findFirst()
                        .orElse(null);
                if (attr != null) {
                    String sqlExpression = (String)((Map)attr.get("sql_mapping")).get("sql_expression");
                    return sqlExpression;
                }
            }
        }
        return null;
    }

    private static boolean isColumnExpansion(String entityName, String attributeName, Map<String, Object> sqlSchema) {
        AtomicBoolean foundColExpansion = new AtomicBoolean(false);
        List<Map<String, Object>> entities = (List<Map<String, Object>>) sqlSchema.get("entities");
        Map<String, Object> entity = entities.stream()
                .filter(ent -> entityName.equals(ent.get("name")))
                .findFirst().orElse(null);
        if (entity != null) {
            Stream<Map<String, Object>> attributes = ((List<Map<String, Object>>) entity.get("attributes")).stream()
                    .filter(att -> attributeName.equals(att.get("name")));
            Boolean found = attributes.map(att -> ((Map)att.get("sql_mapping")).get("column_expansions") != null).findFirst().orElse(null);
            foundColExpansion.set(found != null && found.booleanValue());
        }
        return foundColExpansion.get();
    }

    private static boolean isSQLSelect(String entityName, Map<String, Object> sqlSchema) {
        AtomicBoolean foundSqlSelect = new AtomicBoolean(false);
        List<Map<String, Object>> entities = (List<Map<String, Object>>) sqlSchema.get("entities");
        Map<String, Object> entity = entities.stream()
                .filter(ent -> entityName.equals(ent.get("name")))
                .findFirst().orElse(null);
        if (entity != null) {
            Map<String, Object> sqlMapping = ((Map<String, Object>) entity.get("sql_mapping"));
            if (sqlMapping != null) {
                Object sqlSelect = sqlMapping.get("sql_select");
                Boolean found = sqlSelect != null;
                foundSqlSelect.set(found != null && found.booleanValue());
            }
        }
        return foundSqlSelect.get();
    }

    /**
     *
     * @param valueNames like [ "cost", "year" ]
     * @param where
     * @return
     */
    private static Object findValueInWhereClause(List<String> valueNames, List<Object> where) {
        Object theValue = null;

        if (where == null || where.size() == 0) {
            return null;
        }

        int i = 0;
        String cond = (String) where.get(i);
        if ("AND".equals(cond) || "OR".equals(cond)) {
            for (int y=1; y<where.size(); y++) {
                Object o = findValueInWhereClause(valueNames, (List<Object>)where.get(i + y));
                if (o != null) {
                    return o;
                }
            }
        } else {
            String unaryOp = cond;
            Object left = where.get(i + 1);
            Object right = where.get(i + 2);
            if ("=".equals(unaryOp)) { // Required for the columnExpansions
                if (left != null && left instanceof List) {
                    if (((List)left).equals(valueNames)) {
                        theValue = right;
                    }
                }
            }
        }
        return theValue;
    }
    /*
     *  "sql_mapping": {
     *    "column_expansions": [
     *      "${year}_${quarter}_CST",
     *      "${period}_${type}_CST"
     *    ]
     *  }
     */
    private static List<String> expandColumns(String entityName,
                                              String attributeName,
                                              Map<String, Object> baseSchema,
                                              Map<String, Object> sqlSchema,
                                              List<Object> whereClause,
                                              List<Object> usedForExpansion) {
        return expandColumns(entityName,
                attributeName,
                baseSchema,
                sqlSchema,
                whereClause,
                null,
                usedForExpansion);
    }

    // Used for column expansion. Should also manage the wildcards.
    private static List<String> expandColumns(String entityName,
                                              String attributeName,
                                              Map<String, Object> baseSchema,
                                              Map<String, Object> sqlSchema,
                                              List<Object> whereClause,
                                              List<String> previousLinks,
                                              List<Object> usedForExpansion) {
        List<String> expandedColumns = new ArrayList<>();

        List<Map<String, Object>> entities = (List<Map<String, Object>>) sqlSchema.get("entities");
        Map<String, Object> entity = entities.stream()
                .filter(ent -> entityName.equals(ent.get("name")))
                .findFirst().orElse(null);
        if (entity != null) {
            Stream<Map<String, Object>> attributes = ((List<Map<String, Object>>) entity.get("attributes")).stream()
                    .filter(att -> attributeName.equals(att.get("name")));
            List<String> expansionFormulas = attributes.map(att -> (List<String>)((Map)att.get("sql_mapping")).get("column_expansions")).findFirst().orElse(null);
            if (expansionFormulas != null && expansionFormulas.size() > 0) {
                // Found it.
                expansionFormulas.stream().forEach(formula -> {
//                    System.out.println("Analyzing " + formula);
                    List<String> stringsToSubstitute = new ArrayList<>();
                    List<Object> valuesToPatch = new ArrayList<>();
                    int nbWildCards = 0;
                    final Matcher matcher = COLUMN_EXPANSION_PATTERN.matcher(formula);
                    while (matcher.find()) {
                        String fullMatch = matcher.group(0);
                        stringsToSubstitute.add(fullMatch);
//                        System.out.println("Full match: " + fullMatch);
                        for (int i = 1; i <= matcher.groupCount(); i++) {
                            String valueToFind = matcher.group(i); // Find in the where clause
//                            System.out.println("Group " + i + ": " + valueToFind);
                            List<String> whereElement;
                            if (previousLinks == null || previousLinks.size() == 0) {
                                whereElement = List.of(valueToFind);
                            } else {
                                whereElement = Stream.concat(previousLinks.stream(),
                                                List.of(valueToFind).stream())
                                        .collect(Collectors.toList());
                            }
                            Object obj = findValueInWhereClause(whereElement, whereClause); // Look for [ "=", [ "cost", "year" ], "VAL" ]

                            if (obj == null) { // Not found in where clause. Wildcard?
                                // look for entityName, whereElement.get(1) in baseSchema
                                Object currentEntity = ((List) baseSchema.get("entities")).stream()
                                        .filter(ent -> entityName.equals(((Map) ent).get("name"))).findFirst().orElse(null);
                                if (currentEntity != null) {
                                    Object vle = ((List) ((Map) currentEntity).get("attributes")).stream()
                                            .filter(att -> whereElement.get(whereElement.size() - 1).equals(((Map) att).get("name")))
                                            .findFirst().orElse(null);
                                    if (vle != null) {
                                        Object valueList = ((Map) vle).get("value_list");
                                        obj = valueList;
                                        nbWildCards += 1;
                                    }
                                }
                            }
                            valuesToPatch.add(obj);
                            if (obj != null && usedForExpansion != null) {
                                if (!usedForExpansion.contains(whereElement)) {
                                    usedForExpansion.add(whereElement); // Not to use in the SQL WHERE clause.
                                }
                            }
                        }
                    }
                    String finalColumnName = formula;
                    assert (stringsToSubstitute.size() == valuesToPatch.size());

                    List<String> finalColumnList = new ArrayList<>();
                    finalColumnList.add(finalColumnName);

                    // Possible Trick. Use the wildcards if there if just one parameter in the expansion
                    // if (stringsToSubstitute.size() == 1 || stringsToSubstitute.size() > nbWildCards) {
                    if (stringsToSubstitute.size() > nbWildCards) {

                        boolean noNull = true;
                        for (int i = 0; i < stringsToSubstitute.size(); i++) {
                            if (valuesToPatch.get(i) == null) {
                                noNull = false;
                            } else {
                                if (valuesToPatch.get(i) instanceof String) {
                                    final int _i = i;
                                    for (int j = 0; j < finalColumnList.size(); j++) {
                                        String col = finalColumnList.get(j);
                                        col = col.replace(stringsToSubstitute.get(_i), (String) valuesToPatch.get(_i));
                                        finalColumnList.set(j, col);
                                    }
                                } else if (valuesToPatch.get(i) instanceof List) {         // Wildcards
                                    String toSubstitute = stringsToSubstitute.get(i);
                                    List<String> patchValues = (List) ((List) valuesToPatch.get(i)).stream()
                                            .map(value -> value.toString())
                                            .collect(Collectors.toList());
                                    List<String> newList = new ArrayList<>();
                                    finalColumnList.forEach(col -> {
                                        String toPatch = col;
                                        patchValues.forEach(patch -> {
                                            String patched = toPatch.replace(toSubstitute, patch);
                                            newList.add(patched);
                                        });
//                                    System.out.println("New list:" + newList.stream().collect(Collectors.joining(", ")));
                                    });
                                    finalColumnList = newList;
                                }
                            }
                        }
                        if (noNull) {
                            finalColumnList.stream().forEach(oneCol -> {
                                expandedColumns.add(String.format("%s.%s",
                                        findDBTableByEntityName(entityName, sqlSchema),
                                        oneCol));
                            });
                        }
                    }
                });
            }
        }
        return expandedColumns;
    }

    /**
     * This assumes that a parameter like ${duh} is used only in ONE expandable attribute.
     * @param sqlSchema
     * @param entityName
     * @param attName
     * @return
     */
    private static String findParentAttribute(Map<String, Object> sqlSchema,
                                              String entityName,
                                              String attName) {
        String parentAttName = null;
        Map<String, Object> ent =  (Map<String, Object>)((List) sqlSchema.get("entities")).stream()
                .filter(entity -> entityName.equals(((Map<String, Object>) entity).get("name")))
                .findFirst().orElse(null);
        if (ent != null) {
            List attributes = (List)ent.get("attributes");
            if (attributes != null) {
                Object parentObj = attributes.stream()
                        .filter(att -> {
                            Map<String, Object> attMap = (Map<String, Object>) att;
                            if (((Map)attMap.get("sql_mapping")).get("column_expansions") != null) {
                                List<String> colExp = (List) ((Map)attMap.get("sql_mapping")).get("column_expansions");
                                String match = String.format("${%s}", attName);
                                String found = colExp.stream()
                                        .filter(ex -> ex.indexOf(match) > -1)
                                        .findFirst().orElse(null);
                                return found != null;
                            } else {
                                return false;
                            }
                        }).findFirst().orElse(null);
                if (parentObj != null && parentObj instanceof Map) {
                    parentAttName = (String)((Map<String, Object>)parentObj).get("name");
                }
            }
        }
        return parentAttName;
    }

    private static String expandOneItem(Object item,
                                        Map<String, Object> schema,
                                        Map<String, Object> sqlSchema,
                                        String entityName,
                                        List<String> joinClause) {
        return expandOneItem(item, schema, sqlSchema, entityName, joinClause, null, null);
    }
    private static String expandOneItem(Object item,
                                        Map<String, Object> schema,
                                        Map<String, Object> sqlSchema,
                                        String entityName,
                                        List<String> joinClause,
                                        List<Object> where,
                                        List<Object> usedForExpansion) {
        if (VERBOSE) {
            System.out.printf("Item is a %s\n", item.getClass().getName());
        }
        if (item instanceof List) {
            List<Object> oneElem = (List) item;
            if (oneElem.size() == 1) {
                if (oneElem.get(0) instanceof String && "*".equals(oneElem.get(0))) {
                    String expandStar = expandStarTableColumns(entityName, sqlSchema);
                    return expandStar;
                } else {
                    String oneClause;
                    String sqlExp = sqlExpression(entityName, (String) oneElem.get(0), sqlSchema);
                    if (sqlExp != null) {                                                           // SQL Expression
                        oneClause = String.format("(%s) as %s", sqlExp, oneElem.get(0));
                    } else if (isColumnExpansion(entityName, (String) oneElem.get(0), sqlSchema)) { // Column Expansion ? // TODO Can't be (oneElem.size() == 1). To remove
                        // Use the where clause to find the patch values
                        String attributeName = (String) oneElem.get(0);
                        List<String> expandedColumns = expandColumns(entityName,
                                attributeName,
                                schema,
                                sqlSchema,
                                where,
                                usedForExpansion);
                        if (expandedColumns != null && expandedColumns.size() > 0) {
                            AtomicInteger aliasIndex = new AtomicInteger(0);
                            oneClause = expandedColumns.stream()
//                                    .map(col -> String.format("%s as \"%s_%d\"", col, attributeName, aliasIndex.addAndGet(1)))
                                    .map(col -> String.format("%s as \"%s_%d\"", col, col.split("\\.")[1], aliasIndex.addAndGet(1)))
                                    .collect(Collectors.joining(", "));
                        } else {
                            oneClause = "";
                        }
//                    } else if (false && isSQLSelect(entityName, sqlSchema)) {
//                        // TODO SQLSelect through a relation/link? Agg function? etc.
//                        String columnName = findDBColumnByEntityName(entityName, (String) oneElem.get(0), sqlSchema);
//                        oneClause = String.format("%s", columnName);
                    } else { // It's a DB table and column
                        String tableName = findDBTableByEntityName(entityName, sqlSchema);
                        String columnName = findDBColumnByEntityName(entityName, (String) oneElem.get(0), sqlSchema);
                        if (columnName == null) {
                            // See if the value is in the where, about a column expansion
                            String attName = (String) oneElem.get(0);
                            Object prmValue = findValueInWhereClause(List.of(attName), where); // TODO Verify columnName
                            if (prmValue != null) {
                                // Find the expendable attribute this prm belongs to
                                String parentAttributeName = "X"; // Should be the columnName in the new syntax
                                String foundParentAttributeName = findParentAttribute(sqlSchema, entityName, attName);
                                if (foundParentAttributeName != null) {
                                    parentAttributeName = foundParentAttributeName;
                                }
                                oneClause = String.format("'%s' as \"%s.%s\"", prmValue, parentAttributeName, attName);
                            } else {
                                oneClause = String.format("%s.%s", tableName, columnName); // Fallback - Means if failed. Not found.
                            }
                        } else {
                            if (tableName != null) {
                                oneClause = String.format("%s.%s", tableName, columnName);
                            } else {
                                oneClause = String.format("%s", columnName);
                            }
                        }
                    }
                    return oneClause;
                }
            } else { // Agg function or CompositeEntity (join), size = 2, or more.
                if (((String) oneElem.get(0)).startsWith("#")) {
                    String aggFunc = ((String) oneElem.get(0)).substring(1);
                    String oneClause;
                    Object prm1 = oneElem.get(1);
//                    System.out.println("oneElem.get(1) type:" + prm1);
                    if ((prm1 instanceof List && "*".equals(((List) prm1).get(0))) || "*".equals(prm1)) {
                        oneClause = String.format("%s(%s)", aggFunc, "*"); // oneElem.get(1));
                    } else {
                        if (oneElem.size() == 2 && prm1 instanceof List && ((List)prm1).size() == 1) { // Current table
                            String tableName = findDBTableByEntityName(entityName, sqlSchema);
                            String columnName = findDBColumnByEntityName(entityName,
                                    (String) (prm1 instanceof List ? ((List) prm1).get(0) : oneElem.get(1)),
                                    sqlSchema);
                            if (tableName != null) {
                                oneClause = String.format("%s(%s.%s)",
                                        aggFunc,
                                        tableName,
                                        columnName);
                            } else {
                                oneClause = String.format("%s(%s)",
                                        aggFunc,
                                        columnName);
                            }
                        } else { // Composite Entity
                            String relationName = null;
                            String columnName = null;
                            if (prm1 instanceof List && ((List)prm1).size() > 1) {  // [ "#COUNT", [ "race", "race_id" ]]
                                relationName = (String)((List)prm1).get(0);
                                if (((List)prm1).get(1) instanceof List) {  // Like [ "#COUNT", [ "#DISTINCT", [ "race_id" ]]]
                                    columnName = expandOneItem(prm1, schema, sqlSchema, entityName, joinClause);
                                } else {
                                    columnName = (String) ((List) prm1).get(1);
                                }
                            } else {                                                // [ "#COUNT", "race", "race_id" ]
                                relationName = (String) oneElem.get(1);
                                columnName = (String) oneElem.get(2);
                            }
                            String resolved = relationName.startsWith("#") ? columnName : resolveCompositeEntity(entityName,
                                    relationName,
                                    columnName,
                                    schema,
                                    sqlSchema,
                                    joinClause,
                                    where,
                                    usedForExpansion);
                            oneClause = String.format("%s(%s)", aggFunc, resolved);
                        }
                    }
                    return oneClause;
                } else { // CompositeEntity. table.column, and join. column_expansion
                    if (VERBOSE) {
                        System.out.println("CompositeEntity...");
                    }
                    // look for a relationship or column_expansion
                    String relationName = (String) oneElem.get(0);

                    String entityFromLink = getEntityFromRelation(entityName, relationName, schema);
                    String tableName;
                    if (entityFromLink != null) {
                        tableName = findDBTableByEntityName(entityFromLink, sqlSchema);
                    } else {
                        tableName = findDBTableByEntityName(entityName, sqlSchema);
                    }

                    // TODO Question: if len = 3, what's item 1 (2nd pos) ? Will need to recurse.
                    String attName = (String) (/*oneElem.size() > 2 ? oneElem.get(2) :*/ oneElem.get(1));

                    String expandedItem = "";

                    if ("*".equals(attName)) {
                        expandedItem = expandStarTableColumns(entityFromLink, sqlSchema);
                        // 2 - JOIN
                        Map<String, Object> holdingEntity = getCompositeEntity(entityName, relationName, schema);
                        Map<String, Object> compositeEntityDefinition = ((List<Map<String, Object>>) holdingEntity.get("attributes")).stream()
                                .filter(att -> "composite_entity".equals(att.get("type")) &&
                                        relationName.equals(att.get("name"))).findFirst().orElse(null);

                        List<String> keyFrom;
                        if ((boolean)compositeEntityDefinition.get("multiple_values")) { // 1-1, 1-many
                            keyFrom = findEntityForeignKey((String) compositeEntityDefinition.get("entity_name"), (String) holdingEntity.get("name"), sqlSchema);
                        } else {
                            keyFrom = findEntityPrimaryKey((String) compositeEntityDefinition.get("entity_name"), sqlSchema);
                        }
                        String toTable = findDBTableByEntityName((String) holdingEntity.get("name"), sqlSchema); // entityFromLink?
                        List<String> fkTo = findEntityForeignKey((String) holdingEntity.get("name"), (String) compositeEntityDefinition.get("name"), sqlSchema);
                        String oneJoinClause = String.format("JOIN %s ON %s", // entityFromLink, entityName
                                tableName,
                                generateOnJoinClause(
                                        tableName, // findDBTableByEntityName(entityName, sqlSchema),
                                        keyFrom,  // PK
                                        toTable,
                                        fkTo // FK
                                )
                        );
                        if (!joinClause.contains(oneJoinClause)) {
                            joinClause.add(oneJoinClause);
                        }
                        return expandedItem;
                    }

                    String dbColumnName = findDBColumnByEntityName(entityFromLink == null ? entityName : entityFromLink, attName, sqlSchema);

                    // More elements in oneElem ?
                    if (oneElem.size() > 2) { // TODO Recursion ?
                        String entityFromRelation = getEntityFromRelation(entityName, relationName, schema);
                        tableName = findDBTableByEntityName(entityFromRelation, sqlSchema);
                        dbColumnName = findDBColumnByEntityName(entityFromRelation, attName, sqlSchema);
                        expandedItem = expandFromList(entityFromRelation,
                                (String)oneElem.get(1), // TODO Check this
                                (String)oneElem.get(2), // TODO Check this
                                dbColumnName,
                                tableName,
                                oneElem,
                                schema,
                                sqlSchema,
                                where,
                                joinClause,
                                usedForExpansion,
                                List.of(relationName), // TODO ... more than 1 relation ?
                                false);
//                        System.out.println("Aha!");
                    } else {
                        expandedItem = expandFromList(entityName,
                                relationName,
                                attName,
                                dbColumnName,
                                tableName,
                                oneElem,
                                schema,
                                sqlSchema,
                                where,
                                joinClause,
                                usedForExpansion,
                                false);
                    }
                    return expandedItem;
                }
            }
        } else {
            throw new RuntimeException(String.format("Un-managed %s in clause %s\n", item.getClass().getName(), item.toString()));
        }
    }

    private static String expandFromList(String entityName,
                                         String relationName,
                                         String attName,
                                         String dbColumnName,
                                         String tableName,
                                         List<Object> oneElem,
                                         Map<String, Object> schema,
                                         Map<String, Object> sqlSchema,
                                         List<Object> where,
                                         List<String> joinClause,
                                         List<Object> usedForExpansion,
                                         boolean inWhereClause) {
        return expandFromList(entityName,
                relationName,
                attName,
                dbColumnName,
                tableName,
                oneElem,
                schema,
                sqlSchema,
                where,
                joinClause,
                usedForExpansion,
                null,
                inWhereClause);
    }

    private static String expandFromList(String entityName,
                                         String relationName,
                                         String attName,
                                         String dbColumnName,
                                         String tableName,
                                         List<Object> elemList,
                                         Map<String, Object> schema,
                                         Map<String, Object> sqlSchema,
                                         List<Object> where,
                                         List<String> joinClause,
                                         List<Object> usedForExpansion,
                                         List<String> previousRelations,
                                         boolean inWhereClause) {

        String expandedItem = "";

        // SQLExpression ?
        String joinedHoldingEntity = getEntityFromRelation(entityName, relationName, schema);
        String sqlExp = sqlExpression(joinedHoldingEntity == null ? entityName : joinedHoldingEntity,
                attName,
                sqlSchema);
        if (sqlExp != null) {

            if (inWhereClause) {
                expandedItem = String.format("(%s)", sqlExp);
            } else {
                expandedItem = String.format("(%s) as %s", sqlExp, attName);
            }
            return expandedItem;
        }

        String entityToReach = joinedHoldingEntity != null ? joinedHoldingEntity : entityName;
        if (isColumnExpansion(entityToReach, attName, sqlSchema)) {
            // Use the where clause to find the patch values
            String attributeName = attName;
            List<String> previousRelationList = previousRelations;
            if (joinedHoldingEntity != null) {
                if (previousRelationList == null) {
                    previousRelationList = new ArrayList<>();
                }
                previousRelationList.add(relationName);
            }
            List<String> expandedColumns = expandColumns(entityToReach,
                    attributeName,
                    schema,
                    sqlSchema,
                    where,
                    previousRelationList,
                    usedForExpansion);
            if (expandedColumns != null && expandedColumns.size() > 0) {
                String relPrefix = attName;
                if (previousRelations != null && previousRelations.size() > 0) {
                    relPrefix = previousRelations.stream()
                            .collect(Collectors.joining(".")) +
                            "." + relPrefix;
                }
                final String _relPrefix = relPrefix;
                AtomicInteger aliasIndex = new AtomicInteger(0);
                expandedItem = expandedColumns.stream()
                        .map(col -> {
                            if (inWhereClause) {
                                return String.format("%s", col);
                            } else {
                                return String.format("%s as \"%s.%s.%s_%d\"", col, _relPrefix, attributeName, col.split("\\.")[1], aliasIndex.addAndGet(1));
                            }
                        })
                        .collect(Collectors.joining(", "));
            } else {
                expandedItem = "";
            }
        } else if (dbColumnName == null) {
            // Prm of a column expansion, like entityName: race, relationName: cost, columnName: year
            // See if the value is in the where, about a column expansion
            String expColName = (String)(elemList.size() > 2 ? elemList.get(1) : elemList.get(0)); // TODO Should go away
            // Object prmValue = findValueInWhereClause(List.of(expColName, attName), where); // TODO Verify columnName
            // elemList.stream().map(el -> (String)el).collect(Collectors.toList())
            Object prmValue = findValueInWhereClause(elemList.stream().map(el -> (String)el).collect(Collectors.toList()), where);
            if (prmValue != null) {
                // Find the expendable attribute this prm belongs to
                String parentAttributeName = expColName; // "X"; // Should be the columnName in the new syntax
//                                String foundParentAttributeName = findParentAttribute(sqlSchema, entityName, attName);
//                                if (foundParentAttributeName != null) {
//                                    parentAttributeName = foundParentAttributeName;
//                                }
                if (elemList.size() > 2) {
                    expandedItem = String.format("'%s' as \"%s\"", prmValue,
                            elemList.stream().map(el -> (String)el).collect(Collectors.joining("."))); // attName);
                } else {
                    expandedItem = String.format("'%s' as \"%s.%s\"", prmValue, parentAttributeName, attName); // attName);
                }
            } else {
                expandedItem = String.format("%s.%s", tableName, attName); // Fallback - Means if failed. Not found.
            }
        } else {
            // Find the entity matching the relationName
            String holdingEntity = getEntityFromRelation(entityName, relationName, schema);
            if (holdingEntity != null && isColumnExpansion(holdingEntity, attName, sqlSchema)) {
                // Use the where clause to find the patch values
                String attributeName = attName;
                List<String> expandedColumns = expandColumns(holdingEntity,
                        attributeName,
                        schema,
                        sqlSchema,
                        where,
                        usedForExpansion);

                if (expandedColumns != null && expandedColumns.size() > 0) {
                    AtomicInteger aliasIndex = new AtomicInteger(0);
                    expandedItem = expandedColumns.stream()
//                                    .map(col -> String.format("%s as \"%s_%d\"", col, attributeName, aliasIndex.addAndGet(1)))
                            .map(col -> {
                                if (inWhereClause) {
                                    return String.format("%s", col);
                                } else {
                                    return String.format("%s as \"%s.%s.%s_%d\"", col, relationName, attributeName, col.split("\\.")[1], aliasIndex.addAndGet(1));
                                }
                            })
                            .collect(Collectors.joining(", "));
                } else {
                    expandedItem = "";
                }
                // System.out.println("OneClause:" + expandedItem);
            } else {
//                if (dbColumnName != null && tableName != null) {
//                    expandedItem = String.format("%s.%s", tableName, dbColumnName);
//                } else {
                expandedItem = resolveCompositeEntity(entityName,
                        relationName,
                        attName,
                        schema,
                        sqlSchema,
                        joinClause,
                        where,
                        usedForExpansion);
//                }
            }
        }
        return expandedItem;
    }

    private static Map<String, Object> omrlToNestedSQLQuery(Map<String, Object> schema,
                                                            Map<String, Object> sqlSchema,
                                                            List<Object> query,
                                                            Object expressionResolver) {

        if (query.size() < 3) {
            throw new RuntimeException("Malformed query.");
        }

        List<String> subQueries = new ArrayList<>();
        List<String> resultSetMap = null;
        List<Object> prmValues = new ArrayList<>();


        String connector = (String) query.get(0);
        for (int i = 1; i < query.size(); i++) {
            Object oneQuery = query.get(i);
            if (oneQuery instanceof List) {
                Map<String, Object> map = omrlToNestedSQLQuery(schema, sqlSchema, (List) oneQuery, expressionResolver);
                subQueries.add((String) map.get("query"));
                List<Object> prms = (List) map.get("prm-values");
                if (prms != null) {
                    prmValues.addAll(prms);
                }
                if (resultSetMap == null) {
                    resultSetMap = (List) map.get("rs-map");
                }
            } else { // Assume it is a map
                Map<String, Object> map = omrlToOneSQLQuery(schema, sqlSchema, (Map) oneQuery, expressionResolver);
                subQueries.add((String) map.get("query"));
                List<Object> prms = (List) map.get("prm-values");
                if (prms != null) {
                    prmValues.addAll(prms);
                }
                if (resultSetMap == null) {
                    resultSetMap = (List) map.get("rs-map");
                }
            }
        }
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("(%s)", subQueries.get(0)));
        for (int i = 1; i < subQueries.size(); i++) {
            sb.append(String.format(" %s (%s)", connector, subQueries.get(i)));
        }
        String finalQuery = sb.toString();

        return Map.of("query", finalQuery,
                "prm-values", prmValues,
                "rs-map", resultSetMap);
    }

    private static boolean expressionToBeResolved(String formula) {
        final Matcher matcher = COLUMN_EXPANSION_PATTERN.matcher(formula);
        return matcher.find();
    }

    /**
     * Top most function, callable externally
     *
     * @param schema    OMRL mapping Schema
     * @param sqlSchema OMRL mapping SQL Schema
     * @param query     the OMRL representation of the query
     * @return a String containing the SQL query to perform.
     */
    private static Map<String, Object> omrlToOneSQLQuery(Map<String, Object> schema,
                                                         Map<String, Object> sqlSchema,
                                                         Map<String, Object> query,
                                                         Object expressionResolver) {
        String sql = "";

        boolean distinct = false;
        if (query.get(DISTINCT) != null) {
            distinct = (Boolean) query.get(DISTINCT);
        }

        List<String> joinClause = new ArrayList<>();
        Object from = query.get(FROM); // We assume ONE element, as a String

        // SELECT
        List<Object> select = (List<Object>) query.get(SELECT);
        List<Object> where = (List<Object>) query.get(WHERE); // Used for column expansions

        List<Object> usedForExpansion = new ArrayList<>();

        List<String> selectClause = new ArrayList<>();
        select.forEach(one -> {
            String expanded = expandOneItem(one, schema, sqlSchema, (String) from, joinClause, where, usedForExpansion);
            selectClause.add(expanded);
        });
//        String sqlSelectClause = selectClause.stream()
//                .collect(Collectors.joining(", "));

        // FROM
        List<String> fromClause = new ArrayList<>();
        if (from instanceof String) { // Just one entity
            List<Map<String, Object>> entities = (List<Map<String, Object>>) sqlSchema.get("entities");
            Optional<Map<String, Object>> entityObject = entities.stream()
                    .filter(entity -> {
                        String name = (String) entity.get("name");
                        return from.equals(name);
                    })
                    .findFirst();
            if (entityObject.isPresent()) {
                Map<String, Object> entityMap = entityObject.get();
                String tableName = (String)((Map)entityMap.get("sql_mapping")).get("table_name");
                if (tableName != null) {
                    fromClause.add(tableName);
                } else {
                    String sqlSelect = (String)((Map)entityMap.get("sql_mapping")).get("sql_select");
                    if (sqlSelect != null) {
                        if (expressionToBeResolved(sqlSelect)) { // Patch SQL Select stmt?
                            if (expressionResolver != null) {
                                if (false) { // expressionResolver instanceof ExpressionResolver) {
//                                    try {
//                                        sqlSelect = (String) ((ExpressionResolver)expressionResolver).resolveExpressions(sqlSelect, false);
//                                    } catch (IOException ioe) {
//                                        // Oops TODO Surface that?
//                                    }
                                } else if (expressionResolver instanceof Function) { // This is for the unit tests
                                    sqlSelect = ((Function<String, String>)expressionResolver).apply(sqlSelect);
                                }
                            }
                        }
                        fromClause.add(String.format("(%s)", sqlSelect));
                    } else {
                        // TODO Honk!
                        System.out.println("No Table Name, no SQL Select");
                    }
                }
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
        List<Object> prmValues = (usePreparedStmt ? new ArrayList<>() : null);

        // Review the usedForExpansion in case attributes to expand are only in the where clause (and not in select)...
        rescanExpansionList(where,
                where,
                schema,
                sqlSchema,
                (String) from,
                usedForExpansion);

        String expandedWhere = (where != null ?
                expandWhereHavingClause(where,
                        where, // Original
                        schema,
                        sqlSchema,
                        (String) from,
                        selectClause,
                        joinClause,
                        prmValues,
                        usedForExpansion) :
                "");

        // GROUP-BY
        List<Object> groupBy = (List<Object>) query.get(GROUP_BY);
        String sqlGroupByClause = "";
        if (groupBy != null) {
            List<String> groupByClause = new ArrayList<>();
            groupBy.forEach(one -> {
                String expanded = expandOneItem(one, schema, sqlSchema, (String) from, joinClause);
                groupByClause.add(expanded);
            });
            sqlGroupByClause = groupByClause.stream()
                    .collect(Collectors.joining(", "));
        }

        // HAVING
        List<Object> having = (List<Object>) query.get(HAVING);
        String expandedHaving = (having != null ?
                expandWhereHavingClause(having,
                        having, // Original
                        schema,
                        sqlSchema,
                        (String) from,
                        selectClause,
                        joinClause,
                        prmValues,
                        usedForExpansion) :
                "");

        // ORDER BY
        List<Object> orderBy = (List<Object>) query.get(ORDER_BY);
        String sqlOrderByClause = "";
        if (orderBy != null && !orderBy.isEmpty()) {
            List<String> orderByClause = new ArrayList<>();
            orderBy.forEach(one -> {
                Object theOneToExpand = one;
//                boolean desc = false;
                String sortDirective = ""; // Behind whatever starts with '#' in [ "#SORT", [ "ENT", "ATT" ] ]
                if (one instanceof List && ((List) one).size() > 1 && ((List) one).get(0) instanceof List) { // like [ "#desc", [ "ent", "att" ] ]
                    String sortDir = (String) ((List) one).get(0);
                    theOneToExpand = ((List) one).get(1);
                    if (sortDir.startsWith("#")) {
                        sortDirective = sortDir.substring(1);
                    }
//                    desc = ("#DESC".equalsIgnoreCase(sortDir));
                } else if (((List) one).get(0) instanceof List) { // No direction, only the sort key like [ [ "ent", "att" ] ]
                    theOneToExpand = ((List) one).get(0);
                } else if (((List) one).get(0) instanceof String && ((List) one).size() > 1) { // [ "#XX", [ "att" ]]
                    String sortDir = (String) ((List) one).get(0);
                    if (sortDir.startsWith("#")) {
                        sortDirective = sortDir.substring(1);
                    }
//                    desc = ("#DESC".equalsIgnoreCase(sortDir));
                    theOneToExpand = ((List) one).get(1);
                }
                // TODO Consolidate. Same expansion as in 'where'
                String entityName = (String) from;
                String attributeName = (theOneToExpand instanceof List) ?
                        (String)((List)theOneToExpand).get(0) :
                        theOneToExpand.toString();
                String relationName = attributeName; // Ugly
                String entityFromRelation = getEntityFromRelation(entityName, relationName, schema);
                boolean linkedExpansion = false;
                if (entityFromRelation != null && !entityFromRelation.isEmpty()) {
                    linkedExpansion = isColumnExpansion(entityFromRelation, (String)((List)theOneToExpand).get(1), sqlSchema);
                }
                if (theOneToExpand instanceof List && ((List)theOneToExpand).size() > 1 && linkedExpansion) { // Relation, and column expansion?
                    relationName = (String)((List)theOneToExpand).get(0);
                    entityFromRelation = getEntityFromRelation(entityName, relationName, schema);
                    String tableName = findDBTableByEntityName(entityFromRelation, sqlSchema);
                    String dbColumnName = findDBColumnByEntityName(entityFromRelation, (String)((List)theOneToExpand).get(1), sqlSchema);
                    String expandedItem = expandFromList(entityFromRelation,
                            (String)((List)theOneToExpand).get(0), //1), // TODO Verify
                            (String)((List)theOneToExpand).get(1), //2), // TODO Verify
                            dbColumnName,
                            tableName,
                            (List)theOneToExpand,
                            schema,
                            sqlSchema,
                            where,
                            joinClause,
                            usedForExpansion,
                            List.of(relationName), // TODO ... more than 1 relation ?
                            true);
//                    System.out.println(expandedItem);
                    List<String> expandedColumns = Arrays.asList(expandedItem.split(","));
                    if (expandedColumns != null && expandedColumns.size() > 0) {
                        final String _sortDirective = sortDirective;
                        expandedColumns.stream().forEach(exp -> {
                            orderByClause.add(String.format("%s%s", exp.trim(),
                                    (!_sortDirective.trim().isEmpty() ? String.format(" %s", _sortDirective.trim()) : "")));
                        });
                    }
                } else if (isColumnExpansion(entityName, attributeName, sqlSchema)) { // Column Expansion ?
                    // Use the where clause to find the patch values
                    List<String> expandedColumns = expandColumns(entityName,
                            attributeName,
                            schema,
                            sqlSchema,
                            where,
                            usedForExpansion);
                    if (expandedColumns != null && expandedColumns.size() > 0) {
                        final String _sortDirective = sortDirective;
                        expandedColumns.stream().forEach(exp -> {
                            orderByClause.add(String.format("%s%s", exp,
                                    (!_sortDirective.trim().isEmpty() ? String.format(" %s", _sortDirective.trim()) : "")));
                        });
                    }
                } else {
                    String expanded = expandOneItem(theOneToExpand, schema, sqlSchema, (String) from, joinClause);
                    orderByClause.add(String.format("%s%s", expanded,
                            (!sortDirective.trim().isEmpty() ? String.format(" %s", sortDirective.trim()) : "")));
                }
//                orderByClause.add(String.format("%s%s", expanded, (desc ? " DESC" : "")));
            });
            sqlOrderByClause = orderByClause.stream()
                    .collect(Collectors.joining(", "));
        } else { // Is there a default order by?
            String entityName = (String) from;
            Object tableSQLMapping = ((List) sqlSchema.get("entities")).stream()
                    .filter(ent -> entityName.equals(((Map) ent).get("name")))
                    .map(ent -> ((Map) ent).get("sql_mapping"))
                    .findFirst().orElse(null);
            Object defaultOrderBy = null;
            if (tableSQLMapping != null) {
                defaultOrderBy = ((Map)tableSQLMapping).get("default_order_by");
            }
            // Add default order by, except if there is a group by already.
            if (defaultOrderBy != null && !((List)defaultOrderBy).isEmpty() && (groupBy == null || groupBy.isEmpty())) {
                sqlOrderByClause = ((List<String>)defaultOrderBy).stream()
                        .collect(Collectors.joining(", "));
            }
        }

        String sqlSelectClause = selectClause.stream()
                .filter(one -> !one.trim().isEmpty())
                .collect(Collectors.joining(", "));

        // Build result-set map (on DB tables/columns)
        List<String> resultSetMap = new ArrayList<>();
        if (LOGICAL_RESULT_SET_MAP) {
            // result-set map on logical entities/attributes
            // See "select" and "from" elements in the OMRL Query
            select.stream()
                    .forEach(one -> {
                        if (((String)((List)one).get(0)).startsWith("#")) { // Agg function
                            // Agg function
                            Object argList = ((List)one).get(1);
                            if (argList instanceof String) {
                                String argOne = (String)argList;
                                String argTwo = ((List)one).size() > 2 ? (String)((List)one).get(2) : null;
                                if (argTwo == null) {
                                    resultSetMap.add(String.format("%s_%s", (String)((List)one).get(0), argOne));
                                } else {
                                    resultSetMap.add(String.format("%s.%s_%s", argOne, (String)((List)one).get(0), argTwo));
                                }
                            } else if (argList instanceof List) {
                                List two = (List)argList;
                                if (two.size() == 1) {
                                    resultSetMap.add(String.format("%s_%s", (String)((List)one).get(0), (String) two.get(0)));
                                } else {
                                    String secondTerm;
                                    Object secondObj = two.get(1); // .toString();
                                    if (secondObj instanceof List) {
                                        secondTerm = (String) ((List) secondObj).stream()
                                                .map(obj -> obj.toString())
                                                .collect(Collectors.joining("_"));
                                    } else {
                                        secondTerm = secondObj.toString();
                                    }
                                    String firstTerm = (String) two.get(0);
                                    if (firstTerm.startsWith("#")) {
                                        resultSetMap.add(String.format("%s_%s_%s", firstTerm, (String) ((List) one).get(0), (String) secondTerm));
                                    } else {
                                        resultSetMap.add(String.format("%s.%s_%s", firstTerm, (String) ((List) one).get(0), (String) secondTerm)); // (String) two.get(1)));
                                    }
                                }
                            }
                        } else {
                            if (((List) one).size() == 1) {
                                String oneVal = (String) ((List) one).get(0);
                                if ("*".equals(oneVal)) { // Expand star attributes (1st level)
                                    List<Map<String, Object>> entities = (List)sqlSchema.get("entities");
                                    String fromEntity = (String)from;
                                    Map<String, Object> entity = entities.stream()
                                            .filter(_entity -> fromEntity.equals(_entity.get("name")))
                                            .findFirst()
                                            .orElse(null);
                                    if (entity != null) {
                                        List<Object> attributes = (List)entity.get("attributes");
                                        attributes.stream()
                                                .filter(att -> ((Map<String, Object>) ((Map)att).get("sql_mapping")).get("column_name") != null)
                                                .forEach(att -> {
                                                    resultSetMap.add((String) ((Map<String, Object>) att).get("name"));
                                                });
                                    }
                                } else {
                                    int index = select.indexOf(one);
                                    String selectChunk = selectClause.get(index);
                                    // RegEx for string like [RACE.2020_Q1_CST as "cost_1", RACE.ITD_LBR_CST as "cost_2"]
                                    Matcher matcher = ALIAS_DETECTOR_PATTERN.matcher(selectChunk);
                                    List<String> aliases = new ArrayList<>();
                                    while (matcher.find()) {
                                        for (int i = 1; i <= matcher.groupCount(); i++) {
                                            aliases.add(matcher.group(i));
                                        }
                                    }
                                    if (aliases.size() > 0) {
                                        aliases.stream().forEach(alias -> resultSetMap.add(alias));
                                    } else {
                                        resultSetMap.add(oneVal);
                                    }
                                }
                            } else {
                                String relationName = (String) ((List) one).get(0);
                                String attName = (String) ((List) one).get(1);
                                if ("*".equals(attName)) { // expand star (2nd level)
                                    Map<String, Object> holdingEntity = getCompositeEntity((String)from, relationName, schema);
                                    if (holdingEntity != null) {
                                    /* Find the CompositeEntity in the returned entity, like
                                       {
                                            "name": "races",
                                            "type": "composite_entity",
                                            "entity_name": "race",
                                            "multiple_values": true
                                        }
                                     */
                                        Map<String, Object> compositeEntityDefinition = ((List<Map<String, Object>>) holdingEntity.get("attributes")).stream()
                                                .filter(att -> "composite_entity".equals(att.get("type")) &&
                                                        relationName.equals(att.get("name"))).findFirst().orElse(null);
                                        if (compositeEntityDefinition != null) {
                                            String entityName = (String)compositeEntityDefinition.get("entity_name");
                                            List<Map<String, Object>> entities = (List)sqlSchema.get("entities");
                                            Map<String, Object> entity = entities.stream()
                                                    .filter(_entity -> entityName.equals(_entity.get("name")))
                                                    .findFirst()
                                                    .orElse(null);
                                            if (entity != null) {
                                                List<Object> attributes = (List)entity.get("attributes");
                                                attributes.stream()
                                                        .filter(att -> ((Map<String, Object>) ((Map)att).get("sql_mapping")).get("column_name") != null)
                                                        .forEach(att -> {
                                                            resultSetMap.add(String.format("%s.%s", relationName, (String) ((Map<String, Object>) att).get("name")));
                                                        });
                                            }
                                        }
                                    } // else bizarre!
                                } else {
                                    int index = select.indexOf(one);
                                    String selectChunk = selectClause.get(index);
                                    // RegEx for string like [RACE.2020_Q1_CST as "cost_1", RACE.ITD_LBR_CST as "cost_2"]
                                    Matcher matcher = ALIAS_DETECTOR_PATTERN.matcher(selectChunk);
                                    List<String> aliases = new ArrayList<>();
                                    while (matcher.find()) {
                                        for (int i = 1; i <= matcher.groupCount(); i++) {
                                            aliases.add(matcher.group(i));
                                        }
                                    }
                                    if (aliases.size() > 0) {
//                                        aliases.stream().forEach(alias -> resultSetMap.add(alias));
                                        aliases.stream().forEach(alias -> resultSetMap.add(String.format("%s.%s", relationName, alias)));
                                    } else {
                                        if (one instanceof List) { // Should be always the case
                                            if (entityBasedResultMap) {
                                                // Map to entity name, from relation name.
                                                String topEntity = String.valueOf(from);
                                                List<String> entityList = new ArrayList<>();
                                                for (int i=0; i<((List) one).size()-1; i++) {
                                                    String link = String.valueOf(((List) one).get(i));
                                                    String linkedEntity = getEntityFromRelation(topEntity, link, schema);
                                                    topEntity = linkedEntity;
                                                    entityList.add(linkedEntity);
                                                }
                                                entityList.add(String.valueOf(((List) one).get(((List) one).size() - 1))); // The last one
                                                String collected = entityList.stream().collect(Collectors.joining("."));
                                                resultSetMap.add(collected);
                                            } else {
                                                String collected = (String) ((List) one).stream().collect(Collectors.joining("."));
                                                resultSetMap.add(collected);
                                            }
                                        } else {
                                            resultSetMap.add(String.format("%s.%s", relationName, attName));
                                        }
                                    }
//                                    resultSetMap.add(String.format("%s.%s", relationName, attName));
                                }
                            }
                        }
                    });
        } else {
            // On DB columns and tables
            final String fromPrefix = String.format("%s.", from);
            selectClause.stream()
                    .forEach(one -> {
                        if (one.startsWith(fromPrefix)) {
                            resultSetMap.add(one.substring(fromPrefix.length()));
                        } else {
                            resultSetMap.add(one);
                        }
                    });
        }

        // LIMIT
        String sqlLimit = "";
        Object limit = query.get(LIMIT);
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

        sql = String.format("SELECT %s%s FROM %s%s%s%s%s%s",
                //                  | |       | | | | | |
                //                  | |       | | | | | order by
                //                  | |       | | | | having
                //                  | |       | | | group by clause
                //                  | |       | | where clause (and limit)
                //                  | |       | join clause
                //                  | |       from
                //                  | select
                //                  Keyword (Distinct, All)
                (distinct ? "DISTINCT " : ""),
                sqlSelectClause,
                sqlFromClause,
                (sqlJoinClause.isEmpty() ?    "" : String.format(" %s", sqlJoinClause)),
                (expandedWhere.isEmpty() ?    "" : String.format(" WHERE %s", expandedWhere)),
                (sqlGroupByClause.isEmpty() ? "" : String.format(" GROUP BY %s", sqlGroupByClause)),
                (expandedHaving.isEmpty() ?   "" : String.format(" HAVING %s", expandedHaving)),
                (sqlOrderByClause.isEmpty() ? "" : String.format(" ORDER BY %s", sqlOrderByClause)));

        Map<String, Object> finalMap = new HashMap<>();
        finalMap.put("query", sql);
        finalMap.put("prm-values", prmValues);
        finalMap.put("rs-map", resultSetMap);
//        return Map.of("query", sql, "prm-values", prmValues, "rs-map", resultSetMap);
        return finalMap;
    }

    public static Map<String, Object> omrlToSQLQuery(Map<String, Object> schema,
                                                     Map<String, Object> sqlSchema,
                                                     Object query) {
        return omrlToSQLQuery(schema, sqlSchema, query, null);
    }

    public static Map<String, Object> omrlToSQLQuery(Map<String, Object> schema,
                                                     Map<String, Object> sqlSchema,
                                                     Object query,
                                                     Object expressionResolver) { // Can be an ExpressionResolver or a Function<String, String> (for tests)
        Map<String, Object> queryMap;
        if (query instanceof List) {
            queryMap = OMRL2SQL.omrlToNestedSQLQuery(schema, sqlSchema, (List)query, expressionResolver);
        } else {
            queryMap = OMRL2SQL.omrlToOneSQLQuery(schema, sqlSchema, (Map)query, expressionResolver);
        }
        return queryMap;
    }
}
