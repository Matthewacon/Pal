@Literal(target="PFC", replacement="public final class")
@Literal(target="PSV", replacement="public static void")
PFC InvalidClassWithBody {
@Literal(target="sout", replacement="System.out.println();")
 public static void main(String[] args) {
  sout
 }
}