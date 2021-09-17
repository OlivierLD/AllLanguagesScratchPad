import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Processing 4 supports Java8 constructs!
 */
void setup() {
  List<String> testList = Arrays.asList("One", "Two", "Three", "Four", "Five");
  testList.forEach(elem -> {
    System.out.println(elem);
  });
  System.out.println("-------------");

  testList.forEach(System.out::println);
  System.out.println("-------------");
  
  String concat = testList
    .stream()
    .collect(Collectors.joining(", "));
  System.out.println(concat);  
  System.out.println("-------------");

  testList.stream()
    .filter(elmt -> elmt.startsWith("T") && elmt.endsWith("e"))
    .forEach(System.out::println);
}
