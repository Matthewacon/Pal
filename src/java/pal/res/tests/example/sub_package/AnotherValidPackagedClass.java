package example.sub_package;

import io.github.matthewacon.pal.api.annotations.sourcecode.Literal;

@Literal(target = "EHT", replacement = new String("THE"))
public final class AnotherValidPackagedClass {
 public static void main(String[] args) {
  System.out.println("I'm another class in a named package!");
 }
}
