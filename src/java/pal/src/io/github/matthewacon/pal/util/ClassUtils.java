package io.github.matthewacon.pal.util;

import io.github.matthewacon.pal.PalMain;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.matthewacon.pal.util.ArrayUtils.*;

public final class ClassUtils {
 //Returns an unordered array of superclasses and superinterfaces
 public static <T> Class<? super T>[] resolveSupers(final Class<T> t) {
  final LinkedHashSet<Class<? super T>> superClasses = new LinkedHashSet<>();
  superClasses.add(t);
  superClasses.add(t.getSuperclass());
  for (final Class<?> clazz : t.getInterfaces()) {
   superClasses.add((Class<? super T>)clazz);
  }
  LinkedHashSet<Class<? super T>> lastSuperclasses = new LinkedHashSet<>();
  while (!lastSuperclasses.equals(superClasses)) {
   lastSuperclasses.addAll(superClasses);
   for (Class<? super T> clazz : lastSuperclasses) {
    if (clazz != null) {
     superClasses.add(clazz.getSuperclass());
     for (final Class<? super T> iface : (Class<? super T>[])clazz.getInterfaces()) {
      superClasses.add(iface);
     }
    }
   }
  }
  return ArrayUtils.toArray(superClasses);
//  return (Class<? super T>[])superClasses
//   .stream()
//   .filter(elem -> elem != null)
//   .collect(Collectors.toList())
//   .toArray(new Class[0]);
 }

 public static <T> Class<? super T>[] resolveSuperClasses(final Class<T> t) {
  final LinkedHashSet<Class<? super T>> superClasses = new LinkedHashSet<>();
  Class<? super T> superClass = t;
  while ((superClass = superClass.getSuperclass()) != null) {
   superClasses.add(superClass);
  }
  return ArrayUtils.toArray(superClasses);
 }

 //TODO
// public static <T> ExampleLinkedTreeMap<Class<? super T>> resolveSuperInterfaces(final Class<T> t) {
//  ExampleLinkedTreeMap<Class<? super T>> root = new ExampleLinkedTreeMap<>(t);
//  Iterator<ExampleLinkedTreeMap<Class<? super T>>> iterator = root.getChildren().iterator();
//  while (true) {
//   final ExampleLinkedTreeMap<Class<? super T>> rootRef = root;
//   final List<Class<? super T>> interfaces = Arrays.asList((Class<? super T>[])root.getValue().getInterfaces());
//   root.addChildren(interfaces
//    .stream()
//    .map(iface -> new ExampleLinkedTreeMap<>(rootRef, iface))
//    .collect(Collectors.toList())
//   );
//   if (iterator.hasNext()) {
//    root = iterator.next();
//   } else {
//    root = root.getParent();
//   }
//   //Break before the last upward traversal
//   if (root.getValue().equals(t)) {
//    break;
//   }
//  }
//  return root;
// }

 //Returns either a common ancestor of both classes or null if none exists
 public static <T> Class<? super T> findCommonAncestor(final Class<T> first, final Class<?> second) {
  if (first.equals(second)) {
   return first;
  } else {
   final Class<? super T>[] firstSuperclasses = resolveSupers(first);
   final Class<?>[] secondSuperclasses = resolveSupers(second);
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

 //Returns true if the common ancestor of 'second' is equal to 'first'
 public static <T> boolean hasCommonAncestor(final Class<T> first, final Class<?> second) {
  return first.equals(findCommonAncestor(first, second));
 }

 //TODO this seems fishy, test this
 //Returns true if the common ancestor of all classes in 'seconds' is equal to 'first'
 public static <T> boolean hasCommonAncestor(final Class<T> first, final Class<?>... seconds) {
  return first.equals(findCommonAncestor((Class[])unsafeConcat(new Class[] {first}, seconds)));
 }

 //TODO add notation for accessing type parameter information (compiletime and runtime)
 //TODO clean up
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
//   throw new RuntimeException(e);
   return null;
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

 private static final Pattern ARRAY_PATTERN = Pattern.compile("\\[\\]");
 public static <T> int countDims(final Class<T> clazz) {
  final Matcher matcher = ARRAY_PATTERN.matcher(clazz.getCanonicalName());
  int dims = 0;
  while (matcher.find()) dims += 1;
  return dims;
 }

// public static ExampleLinkedTreeMap<String> lexGenericParameters(final String name) {
//  if (name.contains("<")) {
//   final String expr = name.replaceAll("\\s+", "");
//   int
//    index = 0,
//    lastMatch = -1;
//   char lastIdentifier = '\0';
//   ExampleLinkedTreeMap<String> root = null;
//   while (index < expr.length()) {
//    final char c = expr.charAt(index);
//    final String sub = expr.substring(lastMatch + 1, index);
//    if (c == '<') {
//     lastMatch = index;
//     final ExampleLinkedTreeMap<String> newParent = new ExampleLinkedTreeMap<>(root, sub);
//     if (root != null) {
//      root
//       .getChildren()
//       .add(newParent);
//     }
//     root = newParent;
//     lastIdentifier = c;
//    } else if (c == '>') {
//     lastMatch = index;
//     if (lastIdentifier != '>') {
//      root
//       .getChildren()
//       .add(new ExampleLinkedTreeMap<>(root, sub));
//     }
//     root = root.getParent() == null ? root : root.getParent();
//     lastIdentifier = c;
//    } else if (c == ',') {
//     lastMatch = index;
//     if (lastIdentifier != '>') {
//      root
//       .getChildren()
//       .add(new ExampleLinkedTreeMap<>(root, sub));
//     }
//     lastIdentifier = c;
//    }
//    index++;
//   }
//   return root;
//  } else {
//   return null;
//  }
// }
}