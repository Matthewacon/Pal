package io.github.matthewacon.pal.api.annotations.bytecode;

import java.lang.annotation.*;

@PalAnnotation
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PalTarget {
// final class PalClassLoaderAdvice {
//  @Advice.OnMethodEnter
//  public static void intercept() {
//   final ClassLoader loader = new ClassLoader() {
//    @Override
//    public Class<?> loadClass(final String name, final boolean initialize) {
//
//    }
//
//    @Override
//    public Class<?> findClass(final String name) {
//     if (getResource().get) {
//
//     }
//    }
//   };
//  }
// }
}

//final class PalTargetProcessor implements PalBytecodeProcessor {
// @Override
// public HashMap<Class<?>, DynamicType.Builder<?>> process(LinkedHashMap<Class<?>, Annotation[]> map) {
//  return null;
// }
//}