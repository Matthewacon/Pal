package io.github.matthewacon.pal.api.bytecode;

import io.github.matthewacon.pal.api.PalBytecodeProcessor;
import net.bytebuddy.dynamic.DynamicType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.AbstractMap.SimpleEntry;

@PalAnnotation
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PalProcessor {
 String value() default "DEFAULT STRING";
}

@PalProcessor("CUSTOM STRING")
final class PalProcessorProcessor implements PalBytecodeProcessor<PalProcessor> {
 public PalProcessorProcessor() {}

 @Override
 public SimpleEntry<Class<?>, DynamicType.Builder<?>> process(final Class<?> clazz, final PalProcessor annotation) {
//  PalMain.registerProcessor(this);
  return new SimpleEntry<>(clazz, null);
 }
}