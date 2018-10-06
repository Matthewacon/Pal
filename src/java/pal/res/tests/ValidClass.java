import io.github.matthewacon.pal.api.annotations.bytecode.DummyAnnotation;
import io.github.matthewacon.pal.api.annotations.sourcecode.Literal;
import java.lang.annotation.Annotation;
import io.github.matthewacon.pal.api.IPalProcessor;

@Literal(target="TESTING", replacement="GNITSET")
@DummyAnnotation
public final class Test<@DummyAnnotation T extends Annotation & IPalProcessor<@DummyAnnotation T>> {
 @Literal(target="THE", replacement="END")
 public static String variable = "Hello World!";

 @DummyAnnotation("DUMMY VALUE")
 public static String @DummyAnnotation [] @DummyAnnotation [] @DummyAnnotation [] data;

 @Literal(target="ABCD", replacement=new String("DCBA"))
 public static void main(@Literal(target="EFGH", replacement="HGFE") String[] args) {
  System.out.println(variable);
 }
}