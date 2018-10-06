import io.github.matthewacon.pal.api.annotations.sourcecode.Literal;

@Literal(target="PFC", replacement="public final class")
PFC InvalidClassWithBody {
@Literal(target="sout", replacement="System.out.println();")
 public static void main(String[] args) {
  sout
 }
}