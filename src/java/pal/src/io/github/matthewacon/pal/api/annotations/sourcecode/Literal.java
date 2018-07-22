package io.github.matthewacon.pal.api.annotations.sourcecode;

import io.github.matthewacon.pal.api.PalSourcecodeProcessor;
import io.github.matthewacon.pal.api.annotations.bytecode.PalAnnotation;
import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.annotation.*;

@PalAnnotation
@Target(ElementType.ANNOTATION_TYPE)
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

 final class LiteralProcessor implements PalSourcecodeProcessor<Literal> {
  public LiteralProcessor() {}
 }
}
