package io.github.matthewacon.pal.processors;

import io.github.matthewacon.pal.Literal;
import io.github.matthewacon.pal.api.PalBytecodeProcessor;
import net.bytebuddy.dynamic.DynamicType;

import java.util.AbstractMap.SimpleEntry;

public final class ExampleProcessor implements PalBytecodeProcessor<Literal> {
 @Override
 public SimpleEntry<Class<?>, DynamicType.Builder<?>> process(Class<?> clazz, Literal annotation) {
  return null;
 }
}
