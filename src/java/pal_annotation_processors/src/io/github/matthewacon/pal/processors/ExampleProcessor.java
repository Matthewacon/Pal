package io.github.matthewacon.pal.processors;

import io.github.matthewacon.pal.Literal;
import io.github.matthewacon.pal.api.PalBytecodeProcessor;
import io.github.matthewacon.pal.api.bytecode.PalProcessor;
import net.bytebuddy.dynamic.DynamicType;

import java.util.AbstractMap;

@PalProcessor
public class ExampleProcessor implements PalBytecodeProcessor<Literal> {
 @Override
 public AbstractMap.SimpleEntry<Class<?>, DynamicType.Builder<?>> process(Class<?> clazz, Literal annotation) {
  return null;
 }
}
