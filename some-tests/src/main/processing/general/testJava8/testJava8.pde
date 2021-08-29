import java.util.List;
import java.util.Arrays;

/**
 * Processing 4 supports Java8 constructs!
 */
void setup() {
  List<String> testList = Arrays.asList("One", "Two", "Three");
  testList.forEach(elem -> {
    System.out.println(elem);
  });
  System.out.println("-------------");

  testList.forEach(System.out::println);
  System.out.println("-------------");
  
  testList.stream()
  .filter(elmt -> elmt.startsWith("T") && elmt.endsWith("e"))
  .forEach(System.out::println);
}
