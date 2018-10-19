package io.github.matthewacon.pal.util;

//TODO Variadic type arguments
//A simple utility type which can only hold an instance of 1 of the 2 type parameters
//Ideally, a union would have variadic type arguments, however, java does not support this
//public final class Union<T...>
//public final class Union<T1, T2> {
// private final Object held;
// private final boolean containsFirst;
//
// public Union(final T1 t1) {
//  this.held = t1;
//  this.containsFirst = true;
// }
//
// public Union(final T2 t2) {
//  this.held = t2;
//  this.containsFirst = false;
// }
//
// public boolean containsT1() {
//  return containsFirst;
// }
//
// public boolean containsT2() {
//  return !containsFirst;
// }
//}
