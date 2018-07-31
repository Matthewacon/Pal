package io.github.matthewacon.pal.util;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CompilerUtils {
 private static final Constructor javac_util_List;

 static {
  try {
   javac_util_List = com.sun.tools.javac.util.List.class.getDeclaredConstructors()[0];
   javac_util_List.setAccessible(true);
  } catch (Throwable t) {
   throw new RuntimeException(t);
  }
 }

 //com.sun.tools.javac.util.List utilities
 public static <T> List<T> constructList(final T... elements) {
  List<T> head;
  try {
   head = (List<T>)javac_util_List.newInstance(null, null);
   for (int i = elements.length-1; i > -1; i--) {
    head = (List<T>)javac_util_List.newInstance(elements[i], head);
   }
  } catch (Throwable t) {
   throw new RuntimeException(t);
  }
  return head;
 }

 public static <T> List<T> constructList(final Set<T> set) {
  return constructList((T[])set.toArray());
 }

 public static <T> List<T> constructList(final Collection<T> collection) {
  return constructList((T[])collection.toArray());
 }

 public static <T> java.util.List toList(final List<T> list) {
  return new LinkedList<T>() {{
   addAll(list);
  }};
 }

 //JCTree utilities
 public static Vector<JCTree.JCAnnotation> processAnnotations(final JCTree.JCCompilationUnit compilationUnit) {
  final Vector<JCTree.JCAnnotation> annotations = new Vector<>();
  annotations.addAll(processAnnotations(compilationUnit.defs));
  return annotations;
 }

 public static List<JCTree.JCAnnotation> processAnnotations(final List<? super JCTree> trees) {
  final Stream<? super JCTree> stream = trees.stream();
  final Vector<? super JCTree> definitions = new Vector<>();
  //JCErroneous case
//  stream
//   .filter(JCTree.JCErroneous.class::isInstance)
//   .map(JCTree.JCErroneous.class::cast)
//   .forEach(error -> definitions.addAll(error.errs));
  CompilerUtils.<JCTree.JCErroneous>filterMapAndIterate(
   stream,
   error -> definitions.addAll(error.errs)
  );
  //

  return processAnnotations(constructList(definitions));
 }

 //Stream utilities
 public static <T> Stream<T> filterMapAndIterate(final Stream<?> stream, final Consumer<? super T> consumer) {
  final T firstElement = (T)stream
   .filter(Objects::nonNull)
   .findFirst()
   .orElse(null);
  if (firstElement != null) {
   final Stream<T> mappedStream = stream
    .filter(firstElement.getClass()::isInstance)
    .map(element -> (T)element);
   mappedStream.forEach(consumer);
   return mappedStream;
  }
  return (Stream<T>)stream;
 }
}
