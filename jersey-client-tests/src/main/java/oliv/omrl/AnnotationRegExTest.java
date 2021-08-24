package oliv.omrl;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotationRegExTest {


    private final static String ONE_ANNOTATION_PATTERN_STR = "(@\\w+\\([^\\)]+\\))";
    private final static Pattern ONE_ANNOTATION_PATTERN = Pattern.compile(ONE_ANNOTATION_PATTERN_STR);

    public static void main(String... args) throws Exception {

        String annotations = "@column(name=Race_ID),@primary_key(name=Race_ID, entity=race)";

        Matcher matcher = ONE_ANNOTATION_PATTERN.matcher(annotations);
        System.out.printf("Match:%b (%d group(s))\n\n", matcher.matches(), matcher.groupCount());
        while(matcher.find()) {
            System.out.println(matcher.group(1));
        }
    }

    public static void main_(String[] args) {
        String str1 = "I'm the (First string) to be found. ()?";
        String str2 = "I'm the (SECOND string) to be found. Right? (haha?._,?)";

        matchPattern(str1, ONE_ANNOTATION_PATTERN);
        matchPattern(str2, ONE_ANNOTATION_PATTERN);
    }

    private static void matchPattern(String str1, Pattern pattern) {
        Matcher m = pattern.matcher(str1);
        while(m.find()) {
            System.out.println(m.group(1));
        }
    }
}
