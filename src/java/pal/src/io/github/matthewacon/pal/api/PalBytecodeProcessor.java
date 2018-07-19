package io.github.matthewacon.pal.api;

import net.bytebuddy.dynamic.DynamicType;

import java.lang.annotation.Annotation;
import java.util.AbstractMap.SimpleEntry;

//public interface PalBytecodeProcessor<T extends Annotation & IPalAnnotation<T>> extends IPalProcessor {
public interface PalBytecodeProcessor<T extends Annotation> extends IPalProcessor<T> {
// HashMap<Class<?>, DynamicType.Builder<?>> process(final LinkedHashMap<Class<?>, T> map);
 SimpleEntry<Class<?>, DynamicType.Builder<?>> process(final Class<?> clazz, final T annotation);
}
