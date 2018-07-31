@Literal(target="TESTING", replacement="GNITSET")
public final class Test {
 @Literal(target="THE", replacement="END")
 public static String variable = "Hello World!";

 @Literal(target="ABCD", replacement="DCBA")
 public static void main(@Literal(target="EFGH", replacement="HGFE") String[] args) {
  System.out.println(variable);
 }
}