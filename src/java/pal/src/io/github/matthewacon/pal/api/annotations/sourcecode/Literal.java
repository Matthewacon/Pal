package io.github.matthewacon.pal.api.annotations.sourcecode;

import io.github.matthewacon.pal.api.PalSourcecodeProcessor;
import io.github.matthewacon.pal.api.annotations.bytecode.PalAnnotation;
import io.github.matthewacon.pal.api.annotations.bytecode.PalProcessor;

import javax.lang.model.element.Element;
import java.lang.annotation.*;

@PalAnnotation
@Target({
 ElementType.TYPE,
 ElementType.ANNOTATION_TYPE,
 ElementType.CONSTRUCTOR,
 ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Literal.Literals.class)
public @interface Literal {
 String target();
 String replacement();

 @Target(ElementType.ANNOTATION_TYPE)
 @Retention(RetentionPolicy.RUNTIME)
 @interface Literals {
  Literal[] value();
 }

 @PalProcessor
 final class LiteralProcessor extends PalSourcecodeProcessor<Literal> {
  public LiteralProcessor() {
   super();
  }
//
//  public static String strip(Literal annotation, String code) {
//   return null;
//  }

  @Override
  public String process(Literal annotation, Element element, String code) {
   return code;
  }
 }
}
