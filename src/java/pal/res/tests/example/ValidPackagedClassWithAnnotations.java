package example;

import io.github.matthewacon.pal.api.annotations.sourcecode.Literal;

@Literal(target="CLASS", replacement="SSALC")
@io.github.matthewacon.pal.api.annotations.bytecode.DummyAnnotation
public final class ValidPackagedClassWithAnnotations {
 public static void main(String[] args) {
  System.out.println("I'm a packaged class with mixed fully quantified and imported annotation uses!");
 }
}