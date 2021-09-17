package oliv.regexp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Patterns {
	public static void main(String... args) {
		// URL
		String str = "http://localhost:8080/apis/v0/dashboards/262/modules/354";
		String patternStr = "http://.*/dashboards/([0-9]+)/modules/([0-9]+)";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(str);
		System.out.println("Match:" + matcher.matches());
		String dId = matcher.group(1);
		System.out.println("dId:" + dId);

		// String with CR
		str = "SELECT A\nFROM B\n WHERE blah";
		patternStr = "^(?i)SELECT.*FROM.*"; // Case Insensitive
		pattern = Pattern.compile(patternStr, Pattern.MULTILINE);
		matcher = pattern.matcher(str);
		System.out.println("Match:" + matcher.matches());
		String grp1 = matcher.group(1);
		System.out.println("Group 1:" + grp1);

	}
}
