@Literal(target="PFC", replacement="public final class")
PFC InvalidClassWithBody {
@Literal(target="sout", replacement="System.out.println();")
 public static void main(String[] args) {
  sout
 }
}