package oliv.omrl.v2;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * From Spider (tables.json) to OMRL MappingSchema
 */
public class MappingSchemaBuilder {

    private final static String TABLES_JSON = "tables.json"; // In the resource folder

    private final static String TABLES_STATEMENT_PREFIX = "--tables:";

    public static void main(String... args) throws IOException {
        String tables = null;

        for (String arg : args) {
            if (arg.startsWith(TABLES_STATEMENT_PREFIX)) {
                tables = arg.substring(TABLES_STATEMENT_PREFIX.length());
            }
        }

        URL tablesResource;
        if (tables == null) {
            ClassLoader classLoader = MappingSchemaBuilder.class.getClassLoader();
            tablesResource = classLoader.getResource(TABLES_JSON); // At the root of the resources folder.
        } else {
            tablesResource = new File(tables).toURI().toURL();
        }
        System.out.println("Tables Resource: " + tablesResource);

        ObjectMapper mapper = new ObjectMapper();
        // Tables Object
        List<Object> jsonTableMap = mapper.readValue(tablesResource.openStream(), List.class);

        System.out.printf("Tables has %d entries\n", jsonTableMap.size());

        Map<String, Object> omrlMappingSchema = new HashMap<>();

        jsonTableMap.stream()
                .map(item -> (Map) item)
                .forEach(one -> {
                    Object db_id = one.get("db_id");
//                    System.out.println("DB-id:" + db_id);
                    List<String> tableNames = (List) one.get("table_names");
//                    System.out.println("Tables:" + tableNames.stream().collect(Collectors.joining(", ")));
                    List<String> tableNamesOriginal = (List) one.get("table_names_original");
                    List<List<Object>> columnNames = (List) one.get("column_names");
                    List<List<Object>> columnNamesOriginal = (List) one.get("column_names_original");
                    List<String> columnTypes = (List) one.get("column_types");
                    List<Integer> primaryKeys = (List) one.get("primary_keys");
                    List<List<Integer>> foreignKeys = (List) one.get("foreign_keys");

                    Map<String, Object> oneDbId = new HashMap<>();
                    List<Object> entities = new ArrayList<>();
                    oneDbId.put("entities", entities);
                    tableNames.forEach(table -> {
                        int idx = tableNames.indexOf(table);
                        Map<String, Object> entity = new HashMap<>();
                        entity.put("name", table);
                        entity.put("@database", db_id);
                        entity.put("@table", tableNamesOriginal.get(idx));

                        primaryKeys.forEach(pkIdx -> {
                            List<Object> colId = columnNamesOriginal.get(pkIdx);
                            if ((int)colId.get(0) == idx) {
                                entity.put("@primaryKey", colId.get(1));
                            }
                        });

                        List<Object> attributes = new ArrayList<>();
                        for (int i=0; i<columnNames.size(); i++) {
                            List<Object> idxAndName = columnNames.get(i);
                            int colEntityIndex = (int)idxAndName.get(0);
                            if (colEntityIndex == idx) {
                                Map<String, String> attribute = new HashMap<>();
                                attribute.put("name", (String)idxAndName.get(1));
                                attribute.put("type", columnTypes.get(i));
                                attribute.put("@column", (String)columnNamesOriginal.get(i).get(1));

                                attributes.add(attribute);
                            }
                        }
                        // Relationships?
                        foreignKeys.forEach(fkIds -> {
                            // fkIds is a [ x, y ]
                            List<Object> fkFromId = columnNamesOriginal.get(fkIds.get(0));
                            List<Object> fkToId = columnNamesOriginal.get(fkIds.get(1));

                            if (false) {
                                System.out.printf("FK on table %s [%s.%s -> %s.%s]\n",
                                        table,
                                        tableNamesOriginal.get((int) fkFromId.get(0)),
                                        fkFromId.get(1),
                                        tableNamesOriginal.get((int) fkToId.get(0)),
                                        fkToId.get(1));
                            }
                            if (idx == (int)fkFromId.get(0)) {
//                                System.out.println("1 to 1");
                                Map<String, String> relationShip = new HashMap<>();
                                relationShip.put("name", tableNamesOriginal.get((int)fkToId.get(0)));
                                relationShip.put("type", "CompositeEntity");
                                relationShip.put("entity", tableNamesOriginal.get((int)fkToId.get(0)));
                                relationShip.put("@foreignKey", (String)fkFromId.get(1));
                                relationShip.put("@targetForeignKey", (String)fkToId.get(1));

                                attributes.add(relationShip);
                            }
                            if (idx == (int)fkToId.get(0)) {
//                                System.out.println("1 to many");
                                Map<String, String> relationShip = new HashMap<>();
                                relationShip.put("name", tableNamesOriginal.get((int)fkFromId.get(0)) + "s");
                                relationShip.put("type", "CompositeArray");
                                relationShip.put("entity", tableNamesOriginal.get((int)fkFromId.get(0)));
                                relationShip.put("@foreignKey", (String)fkToId.get(1));
                                relationShip.put("@targetForeignKey", (String)fkFromId.get(1));

                                attributes.add(relationShip);
                            }
                        });

                        entity.put("attributes", attributes);

                        entities.add(entity);
                    });

                    omrlMappingSchema.put((String)db_id, oneDbId);
                });

        String finalSchemaAsString = mapper.writeValueAsString(omrlMappingSchema);
        System.out.println(finalSchemaAsString);

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("mapping.schema.json"));
            out.write(finalSchemaAsString + "\n");
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Done.");
    }
}
