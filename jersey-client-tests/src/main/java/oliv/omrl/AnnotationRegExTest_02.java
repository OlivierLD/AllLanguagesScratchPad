package oliv.omrl;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AnnotationRegExTest_02 {

    private final static String ONE_ANNOTATION_PATTERN_STR = "(@\\w+\\([^\\)]+\\))";
    private final static Pattern ONE_ANNOTATION_PATTERN = Pattern.compile(ONE_ANNOTATION_PATTERN_STR);

    private final static String ANNOTATION_CONTENT_PATTERN_STR = "^(@\\w+)\\((([^\\)]+))\\)";
    private final static Pattern ANNOTATION_CONTENT_PATTERN = Pattern.compile(ANNOTATION_CONTENT_PATTERN_STR);

    private final static String NAME_PATTERN_STR = "(name[\\s]*=[\\s]*)(.*)";
    private final static Pattern NAME_PATTERN = Pattern.compile(NAME_PATTERN_STR);

    private final static String ENTITY_PATTERN_STR = "(entity[\\s]*=[\\s]*)(.*)";
    private final static Pattern ENTITY_PATTERN = Pattern.compile(ENTITY_PATTERN_STR);

    enum KeyType {
        PK("@primary_key"),
        FK("@foreign_key");

        private final String annotation;

        KeyType(String annotation) {
            this.annotation = annotation;
        }
        public String annotation() {
            return this.annotation;
        }
    }

    private static List<String> getKeys(String annotation, KeyType keyType) {
        List<String> cols = null;

        Matcher matcher = ONE_ANNOTATION_PATTERN.matcher(annotation);
        while (matcher.find()) {
            String theGroup = matcher.group(1);
            if (theGroup.startsWith(keyType.annotation())) {
                // Looking for the name value, as a csv string
                Matcher pkMatcher = ANNOTATION_CONTENT_PATTERN.matcher(theGroup);
                while (pkMatcher.find()) {
                    String content = pkMatcher.group(2);
                    Matcher nameMatcher = NAME_PATTERN.matcher(content);

                    if (nameMatcher.find()) {
                        String afterName = nameMatcher.group(2);
                        Matcher entityMatcher = ENTITY_PATTERN.matcher(afterName);

                        boolean entityFound = entityMatcher.find();
                        String columnNames = afterName;
                        if (entityFound) {
                            columnNames = columnNames.substring(0, columnNames.indexOf("entity"));
                        }
                        String[] keys = columnNames.split(",");
                        cols = Arrays.asList(keys).stream()
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());
                    }
                }
            }
        }
        return cols;
    }

    public static void main(String... args) throws Exception {

//        String annotations = "@column(name=Race_ID), @primary_key(name=Race_ID, Two_ID, entity=race)";
//        String annotations = "@column(name=Race_ID), @primary_key(name=Race_ID, Two_ID, entity = race)";
        String annotations = "@column(name=Race_ID), @primary_key(name= Race_ID, Two_ID, entity = race)";
//        String annotations = "@column(name=Race_ID), @primary_key(entity = race, name= Race_ID, Two_ID )";
//        String annotations = "@column(name=Race_ID), @primary_key(name= One_ID, entity = race)";
        String annotations_02 = "@column(name=Race_ID)";

        Matcher matcher = ONE_ANNOTATION_PATTERN.matcher(annotations);
        System.out.printf("Match:%b (%d group(s))\n\n", matcher.matches(), matcher.groupCount());
        while (matcher.find()) {
            String theGroup = matcher.group(1);
            System.out.println("Group: " + theGroup);
            if (theGroup.startsWith("@primary_key")) {
                // Looking for the name value, as a csv string
                Matcher pkMatcher = ANNOTATION_CONTENT_PATTERN.matcher(theGroup);
                while (pkMatcher.find()) {
                    System.out.println("Full match: " + pkMatcher.group(0));
                    for (int i = 1; i <= pkMatcher.groupCount(); i++) {
                        System.out.println("Group " + i + ": " + pkMatcher.group(i));
                    }
                    String content = pkMatcher.group(2);
                    System.out.printf("Parsing [%s]\n", content);
                    Matcher nameMatcher = NAME_PATTERN.matcher(content);

                    System.out.println("------------------------");
                    if (nameMatcher.find()) {
                        String afterName = nameMatcher.group(2);
                        System.out.printf("After Name [%s]\n", afterName);
                        Matcher entityMatcher = ENTITY_PATTERN.matcher(afterName);

                        boolean entityFound = entityMatcher.find();
                        System.out.println("Entity match:" + entityFound);

                        String columnNames = afterName;
                        if (entityFound) {
                            columnNames = columnNames.substring(0, columnNames.indexOf("entity"));
                        }
                        String[] cols = columnNames.split(",");
                        System.out.println("---- COLS -----");
                        Arrays.asList(cols).stream()
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .forEach(c -> System.out.printf("[%s]\n", c));
                        System.out.println("---------------");
                    }
                    System.out.println("Done");
                }
            }
        }

        System.out.println("--- ONE ---");
        List<String> keys = getKeys(annotations, KeyType.PK);
        if (keys != null) {
            keys.forEach(System.out::println);
        } else {
            System.out.println("No key!");
        }

        System.out.println("--- TWO ---");
        keys = getKeys(annotations_02, KeyType.PK);
        if (keys != null) {
            keys.forEach(System.out::println);
        } else {
            System.out.println("No key!");
        }
    }
}
