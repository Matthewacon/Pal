package io.github.matthewacon.pal.api;

import net.bytebuddy.dynamic.DynamicType;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashMap;

public interface PalBytecodeProcessor extends PalProcessor {
 HashMap<Class<?>, DynamicType.Builder<?>> process(final LinkedHashMap<Class<?>, Annotation[]> classes);
}
