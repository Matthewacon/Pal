package io.github.matthewacon.pal.util;

import io.github.matthewacon.pal.PalMain;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import static io.github.matthewacon.pal.util.ArrayUtils.*;

public final class ClassUtils {
 //Returns an unordered array of superclasses and superinterfaces
 public static <T> Class<? super T>[] resolveSuperclasses(final Class<T> t) {
  final LinkedHashSet<Class<? super T>> superclasses = new LinkedHashSet<>();
  superclasses.add(t);
  superclasses.add(t.getSuperclass());
  for (final Class<?> clazz : t.getInterfaces()) {
   superclasses.add((Class<? super T>)clazz);
  }
  LinkedHashSet<Class<? super T>> lastSuperclasses = new LinkedHashSet<>();
  while (!lastSuperclasses.equals(superclasses)) {
   lastSuperclasses.addAll(superclasses);
   for (Class<? super T> clazz : lastSuperclasses) {
    if (clazz != null) {
     superclasses.add(clazz.getSuperclass());
     for (final Class<? super T> iface : (Class<? super T>[])clazz.getInterfaces()) {
      superclasses.add(iface);
     }
    }
   }
  }
  return (Class<? super T>[])superclasses
   .stream()
   .filter(elem -> elem != null)
   .collect(Collectors.toList())
   .toArray(new Class[0]);
 }

 //Returns either a common ancestor of both classes or null if none exists
 public static <T> Class<? super T> findCommonAncestor(final Class<T> first, final Class<?> second) {
  if (first.equals(second)) {
   return first;
  } else {
   final Class<? super T>[] firstSuperclasses = resolveSuperclasses(first);
   final Class<?>[] secondSuperclasses = resolveSuperclasses(second);
   final LinkedHashMap<Integer, Integer> commonElements = findCommonElements(firstSuperclasses, secondSuperclasses);
   if (commonElements.size() != 0) {
    final int
     firstIndex = commonElements.keySet().iterator().next(),
     secondIndex = commonElements.get(firstIndex);
    //TODO redundant check
    if (firstSuperclasses[firstIndex].equals(secondSuperclasses[secondIndex])) {
     return firstSuperclasses[firstIndex];
    }
   }
  }
  return null;
 }

 //Returns either a common ancestor of all classes or null if none exists
 public static <T> Class<? super T> findCommonAncestor(final Class<T>... classes) {
  if (classes.length < 2) {
   if (classes.length == 0) {
    return null;
   } else if (classes.length == 1) {
    return classes[0];
   }
  }
  Class<? super T>[] ancestors = classes;
  while (ancestors.length != 1) {
   final Class<? super T>[] ancestorsCopy = Arrays.copyOf(ancestors, ancestors.length);
   ancestors = new Class[ancestors.length - 1];
   for (int i = 0; i < ancestorsCopy.length - 1; i++) {
    ancestors[i] = findCommonAncestor(ancestorsCopy[i], ancestorsCopy[i + 1]);
   }
  }
  return ancestors[0];
 }

 //TODO add notation for accessing type parameter information (compiletime and runtime)
 //Ex 1) Example.class.$0 for Example<T extends Number> would return a type information object with the bound direction,
 //      parameter name and bound type
 //Ex 2) Example<Double> example; example.$0
 //ALTERNATIVE
 //Since all bodies with generic parameters must declare a type variable, adapt the notation to match the variable
 //Ex) Example.class.<T> for Example<T extends Number> would return a type information object with the bound direction,
 //    parameter name, and bound type: Upper, "T" and Class<Number>, respectively
 public static <T> Class<T> getGenericParameter(final Class<?> clazz) {
  final Type genericSuperclass = clazz.getGenericSuperclass();
  final Type[] genericInterfaces = clazz.getGenericInterfaces();
  final Class<T> targetParameter;
  GenericDiscovery:
  try {
   if (ParameterizedType.class.isAssignableFrom(genericSuperclass.getClass())) {
    final Type typeArgument = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
    targetParameter = (Class<T>)PalMain.PAL_CLASSLOADER.findClass(typeArgument.getTypeName());
   } else {
    for (final Type type : genericInterfaces) {
     if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
      final Type typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
      targetParameter = (Class<T>)PalMain.PAL_CLASSLOADER.findClass(typeArgument.getTypeName());
      break GenericDiscovery;
     }
    }
    targetParameter = null;
   }
  } catch (ClassNotFoundException e) {
   throw new RuntimeException(e);
  }
  return targetParameter;
 }

 //TODO implement stubs
 public static <T> Class<T> getGenericParameter(final Field field) {
  return null;
 }

 public static <T> Class<T> getGenericParameter(final Method method) {
  return null;
 }

 public static <T> Class<T> getGenericParameter(final Constructor<T> constructor) {
  return null;
 }
}