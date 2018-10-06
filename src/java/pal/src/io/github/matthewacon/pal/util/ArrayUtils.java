package io.github.matthewacon.pal.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ArrayUtils {
 //Concatenates two arrays, disregarding type information
 public static Object[] unsafeConcat(final Object[] first, final Object... seconds) {
  if (first.length == 0 && seconds.length == 0) {
   return first;
  }
  if (first.length == 0 && seconds.length != 0) {
   return seconds;
  }
  if (first.length != 0 && seconds.length == 0) {
   return first;
  }
  final Object[] output = new Object[first.length + seconds.length];
  for (int i = 0; i < first.length; i++) {
   output[i] = first[i];
  }
  for (int i = first.length; i < output.length; i++) {
   output[i] = seconds[output.length - 1 - i];
  }
  return output;
 }

 //Concatenates two arrays in a type-safe manner
 public static <T> T[] safeConcat(final T[] first, final T... seconds) {
  if (first.length == 0 && seconds.length == 0) {
   return first;
  }
  if (first.length == 0 && seconds.length != 0) {
   return seconds;
  }
  if (first.length != 0 && seconds.length == 0) {
   return first;
  }
  final Class<T>[]
   firstClasses = Arrays
    .stream(first)
    .map(elem -> elem.getClass())
    .collect(Collectors.toList())
    .toArray(new Class[0]),
   secondClasses = Arrays
    .stream(seconds)
    .map(elem -> elem.getClass())
    .collect(Collectors.toList())
    .toArray(new Class[0]);
  final Class<? super T>
   firstCommonAncestor = ClassUtils.findCommonAncestor(firstClasses),
   secondsCommonAncestor = ClassUtils.findCommonAncestor(secondClasses),
   commonAncestor = ClassUtils.findCommonAncestor(firstCommonAncestor, secondsCommonAncestor);
  final T[] output = (T[])Array.newInstance(commonAncestor, first.length + seconds.length);
  for (int i = 0; i < first.length; i++) {
   output[i] = first[i];
  }
  for (int i = first.length; i < output.length; i++) {
   output[i] = seconds[(output.length - 1) - i];
  }
  return output;
 }

 //Returns a map of indices related to common elements in the first and second arrays, respectively
 public static <T> LinkedHashMap<Integer, Integer> findCommonElements(final T[] first, final Object... seconds) {
  final LinkedHashMap<Integer, Integer> lhm = new LinkedHashMap<>();
  for (int i = 0; i < first.length; i++) {
   for (int j = 0; j < seconds.length; j++) {
    if (Objects.deepEquals(first[i], seconds[j])) {
     lhm.put(i, j);
    }
   }
  }
  return lhm;
 }
}
