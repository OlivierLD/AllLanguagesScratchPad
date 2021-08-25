package oliv.omrl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotationRegExTest {


    private final static String ONE_ANNOTATION_PATTERN_STR = "(@\\w+\\([^\\)]+\\))";
    private final static Pattern ONE_ANNOTATION_PATTERN = Pattern.compile(ONE_ANNOTATION_PATTERN_STR);

    public static void main(String... args) throws Exception {

        String annotations = "@column(name=Race_ID), @primary_key(name=Race_ID, entity=race)";

        Matcher matcher = ONE_ANNOTATION_PATTERN.matcher(annotations);
        System.out.printf("Match:%b (%d group(s))\n\n", matcher.matches(), matcher.groupCount());
        while (matcher.find()) {
            System.out.println("Group: " + matcher.group(1));
        }
    }
}
