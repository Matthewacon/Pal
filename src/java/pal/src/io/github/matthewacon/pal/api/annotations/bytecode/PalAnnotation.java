package io.github.matthewacon.pal.api.annotations.bytecode;

import io.github.matthewacon.pal.api.IPalAnnotation;
import io.github.matthewacon.pal.api.PalBytecodeProcessor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;

import java.lang.annotation.*;
import java.util.AbstractMap.SimpleEntry;

/**Defines a Pal library annotation.*/
@PalAnnotation
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PalAnnotation {
 boolean runtimeAccessible() default false;

 final class PalAnnotationProcessor implements PalBytecodeProcessor<PalAnnotation> {
  public PalAnnotationProcessor() {}

  //TODO Runtime annotation stripping
  @Override
  public SimpleEntry<Class<?>, DynamicType.Builder<?>> process(Class<?> clazz, PalAnnotation annotation) {
   final DynamicType.Builder<?> builder = new ByteBuddy()
    .redefine(clazz)
//    .visit()
    .implement(IPalAnnotation.class);
//   if (!annotation.runtimeAccessible()) {
//
//   }
   return new SimpleEntry<>(clazz, builder);
  }
 }
}
