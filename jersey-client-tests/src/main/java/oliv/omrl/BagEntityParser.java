package oliv.omrl;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BagEntityParser {

    private static ObjectMapper mapper = new ObjectMapper();

    private static String stripQuotes(String from) {
        String stripped = from.trim();
        if (stripped.startsWith("'") || stripped.startsWith("\"")) {
            stripped = stripped.substring(1, stripped.length() - 1);
        }
        return stripped;
    }

    private static String extractMember(String from, String name) {
        String value = null;
        String[] split = from.split(",");
        for (String prm : split) {
            String trimmed = prm.trim();
            if (trimmed.startsWith(name + "=")) {
                value = trimmed.substring((name + "=").length());
                break;
            }
        }
        return value;
    }

    private final static String ONE_ANNOTATION_PATTERN_STR = "(@\\w+\\([^\\)]+\\))";
    private final static Pattern ONE_ANNOTATION_PATTERN = Pattern.compile(ONE_ANNOTATION_PATTERN_STR);

    /**
     *
     * @param annotations like "@database(name=\"race_track\"), @table(name=race)"
     * @param annName like "@database"
     * @param annMember like "name"
     * @return
     */
    private static String getAnnotation(String annotations, String annName, String annMember) {

        /* Pattern to match ONE annotation, like @primary_key(name=Race_ID, entity=race)
        (@\w+\([^\)]+\))
         */
        String finalValue = null;

        Matcher matcher = ONE_ANNOTATION_PATTERN.matcher(annotations);
//        System.out.printf("Match:%b (%d group(s))\n\n", matcher.matches(), matcher.groupCount());
        List<String> annotationList = new ArrayList<>();
        while (matcher.find()) {
            String oneAnnotation = matcher.group(1);
            annotationList.add(oneAnnotation);
//            System.out.println(oneAnnotation);
        }

//        String[] split = annotations.split(",");

        for (String member : annotationList) {
            String trimmed = member.trim();
            String prefix = annName + "(";
            if (trimmed.startsWith(prefix)) {
                String value = trimmed.substring(prefix.length(), trimmed.length() - ")".length());
                // System.out.println(value);
                finalValue = extractMember(value, annMember);
                break;
            }
        }
        return finalValue;
    }

    private static String getDBName(String annotation) {
        return getAnnotation(annotation, "@database", "name");
    }
    private static String getTableName(String annotation) {
        return getAnnotation(annotation, "@table", "name");
    }
    private static String getColumnName(String annotation) {
        return getAnnotation(annotation, "@column", "name");
    }
    private static String getPKName(String annotation) {
        return getAnnotation(annotation, "@primary_key", "name");
    }
    private static String getPKEntity(String annotation) {
        return getAnnotation(annotation, "@primary_key", "entity");
    }
    private static String getFKName(String annotation) {
        return getAnnotation(annotation, "@foreign_key", "name");
    }
    private static String getFKEntity(String annotation) {
        return getAnnotation(annotation, "@foreign_key", "entity");
    }

    private static String getOMRLType(Map<String, String> type) {
        String omrlType = "text";
        if ("entity".equals(type.get("type"))) {
            omrlType = type.get("name").toLowerCase();
        }
        return omrlType;
    }
    /**
     * Hard coded
     * @return
     */
    private static Map<String, Object> buildRaceBagEntity() {
        Map<String, Object> bagEntity = new HashMap<>();
        bagEntity.put("description", "@database(name=\"race_track\"), @table(name=race)");

        List<Map<String, Object>> bagItems = new ArrayList<>();
        Map<String, Object> bagItem = new HashMap<>();
        bagItem.put("name", "race_id");
        bagItem.put("type", Map.of("type", "entity", "name", "NUMBER"));
        bagItem.put("sequence", 1);
        bagItem.put("description", "@column(name=Race_ID),@primary_key(name=Race_ID, entity=race)");
        bagItems.add(bagItem);

        bagItem = new HashMap<>();
        bagItem.put("name", "name");
        bagItem.put("type", Map.of("type", "String"));
        bagItem.put("sequence", 2);
        bagItem.put("description", "@column(name=Name)");
        bagItems.add(bagItem);

        bagItem = new HashMap<>();
        bagItem.put("name", "class");
        bagItem.put("type", Map.of("type", "String"));
        bagItem.put("sequence", 3);
        bagItem.put("description", "@column(name=Class)");
        bagItems.add(bagItem);

        bagItem = new HashMap<>();
        bagItem.put("name", "date");
        bagItem.put("type", Map.of("type", "String"));
        bagItem.put("sequence", 4);
        bagItem.put("description", "@column(name=Race_Date)");
        bagItems.add(bagItem);

        bagItem = new HashMap<>();
        bagItem.put("name", "track_id");
        bagItem.put("type", Map.of("type", "entity", "name", "NUMBER"));
        bagItem.put("sequence", 5);
        bagItem.put("description", "@column(name=Track_ID),@foreign_key(name=Track_ID, entity=track)");
        bagItems.add(bagItem);

        bagEntity.put("items", bagItems);

        return bagEntity;
    }

    private static Map<String, Object> buildTrackBagEntity() {
        Map<String, Object> bagEntity = new HashMap<>();
        bagEntity.put("description", "@database(name=\"race_track\"), @table(name=track)");

        List<Map<String, Object>> bagItems = new ArrayList<>();
        Map<String, Object> bagItem = new HashMap<>();
        bagItem.put("name", "track_id");
        bagItem.put("type", Map.of("type", "entity", "name", "NUMBER"));
        bagItem.put("sequence", 1);
        bagItem.put("description", "@column(name=Track_ID),@primary_key(name=Track_ID, entity=track),@foreign_key(name=Track_ID,entity=race)");
        //                                                                             |
        //                                                                             Is that necessary?
        bagItems.add(bagItem);

        bagItem = new HashMap<>();
        bagItem.put("name", "name");
        bagItem.put("type", Map.of("type", "String"));
        bagItem.put("sequence", 2);
        bagItem.put("description", "@column(name=Name)");
        bagItems.add(bagItem);

        bagItem = new HashMap<>();
        bagItem.put("name", "location");
        bagItem.put("type", Map.of("type", "String"));
        bagItem.put("sequence", 3);
        bagItem.put("description", "@column(name=Location)");
        bagItems.add(bagItem);

        bagItem = new HashMap<>();
        bagItem.put("name", "seating");
        bagItem.put("type", Map.of("type", "entity", "name", "NUMBER"));
        bagItem.put("sequence", 4);
        bagItem.put("description", "@column(name=Seating)");
        bagItems.add(bagItem);

        bagItem = new HashMap<>();
        bagItem.put("name", "year_opened");
        bagItem.put("type", Map.of("type", "entity", "name", "NUMBER"));
        bagItem.put("sequence", 5);
        bagItem.put("description", "@column(name=Year_Opened)");
        bagItems.add(bagItem);

        bagEntity.put("items", bagItems);

        return bagEntity;
    }

    private static List<Map<String, Object>> buildBagEntityList() {
        List<Map<String, Object>> entityList = new ArrayList<>();
        entityList.add(buildRaceBagEntity());
        entityList.add(buildTrackBagEntity());
        return entityList;
    }

    public static void main(String... args) throws Exception {

        List<Map<String, Object>> entityList = buildBagEntityList();

        // 1 - Populate original map
//        Map<String, Object> bagEntity = buildRaceBagEntity();

//        String value = mapper.writeValueAsString(bagEntity);
        String value = mapper.writeValueAsString(entityList);
        System.out.println(value);

        // Parse and generate new Structure
        Map<String, Object> dbStructure = new HashMap<>();

        entityList.forEach(bagEntity -> {

            Object entityDescription = bagEntity.get("description");
            // Get table name, and db name.
            String dbName = getDBName((String) entityDescription);
            if (dbName != null) {
                dbName = stripQuotes(dbName);
            }
            System.out.println("DBName: " + dbName);
            if (dbName == null) {
                System.exit(1);
            }
            Map<String, Object> db = (Map)dbStructure.get(dbName);
            if (db == null) {
                db = new HashMap<>();
                dbStructure.put(dbName, db);
            }
            List<Object> entities = (List)db.get("entities");
            if (entities == null) {
                entities = new ArrayList<>();
                db.put("entities", entities);
            }

            String tableName = getTableName((String) entityDescription);
            // Leave the quotes if they exist.
            System.out.println("Table Name: " + tableName);
            if (tableName == null) {
                System.exit(1);
            }
            Map<String, Object> entity = new HashMap<>();
            entities.add(entity);
            entity.put("name", tableName); // Entity name, equals to table name here.
            entity.put("@database", dbName);
            entity.put("@table", tableName);
            List<Object> attributes = new ArrayList<>();
            entity.put("attributes", attributes);

            // Attributes processing
            List<Map<String, Object>> items = (List) bagEntity.get("items");
            items.forEach(item -> {
                String name = (String) item.get("name");
                String description = (String) item.get("description");
                Map<String, String> type = (Map) item.get("type");
                String columnName = getColumnName(description);
                String pkName = getPKName(description);
                String pkEntity = getPKEntity(description);
                String fkName = getFKName(description);
                String fkEntity = getFKEntity(description);
                Map<String, String> attribute = new HashMap<>();
                attribute.put("name", name);
                attribute.put("@column", columnName);
                attribute.put("type", getOMRLType(type));
                if (pkName != null) { // && tableName.equals(pkEntity)) { // Entity Name!!
                    entity.put("@primaryKey", columnName);
                }
                if (fkName != null) {
                    // Add Relationship
                    Map<String, String> relationShip = new HashMap<>();
                    if (pkName != null && pkEntity != null) {
                        relationShip.put("name", fkEntity + "s");
                        relationShip.put("entity", fkEntity);
                        relationShip.put("type", "CompositeArray");
                        relationShip.put("@foreignKey", columnName);
                        relationShip.put("@targetForeignKey", fkName);
                    } else {
                        relationShip.put("name", fkEntity);
                        relationShip.put("entity", fkEntity);
                        relationShip.put("type", "CompositeEntity");
                        relationShip.put("@foreignKey", columnName);
                        relationShip.put("@targetForeignKey", fkName);
                    }
                    attributes.add(relationShip);
                }
                attributes.add(attribute);
            });
        });

        String omrlSchema = mapper.writeValueAsString(dbStructure);
        System.out.println(omrlSchema);

    }
}
