//package io.github.matthewacon.pal.util;
//
//import java.util.Collection;
//import java.util.Iterator;
//
////LinkedList impl that can store as many elements as the upper bound of N
////TODO For all inherited functions from Collection, add generic parameter checking
////TODO Once implemented, divert all type-checking-related operations to compiletime
//public abstract class LinkedList<T, N extends Number> implements Collection<T> {
// //TODO
// public enum PrimitiveBounds {
//
// }
//
// private final Class<T> genericParameter;
// private N size;
//
// public LinkedList(final Collection<T> collection) {
//  this.genericParameter = ClassUtils.getGenericParameter(this.getClass());
//
// }
//
// @Override
// public boolean equals(final Object o) {
//  if (o instanceof LinkedList) {
//   //TODO check generic parameters
//   if (((LinkedList<T, N>)o).containsAll(this)) {
//    return true;
//   }
//  }
//  return false;
// }
//
// //TODO Doc: use actualSize()
// @Override
// public int size() {
//  throw new UnsupportedOperationException();
// }
//
// public N actualSize() {
//  return size;
// }
//
// @Override
// public boolean isEmpty() {
//  return size.byteValue() == 0;
// }
//
// @Override
// public boolean contains(final Object o) {
//  if () {
//
//  }
//  return false;
// }
//
// @Override
// public Iterator<T> iterator() {
//  return null;
// }
//
// @Override
// public Object[] toArray() {
//  throw new UnsupportedOperationException();
// }
//
// @Override
// public <T1> T1[] toArray(T1[] t1s) {
//  throw new UnsupportedOperationException();
// }
//
// @Override
// public boolean add(T t) {
//  return false;
// }
//
// @Override
// public boolean remove(Object o) {
//  return false;
// }
//
// @Override
// public boolean containsAll(Collection<?> collection) {
//  return false;
// }
//
// @Override
// public boolean addAll(Collection<? extends T> collection) {
//  return false;
// }
//
// @Override
// public boolean removeAll(Collection<?> collection) {
//  return false;
// }
//
// @Override
// public boolean retainAll(Collection<?> collection) {
//  return false;
// }
//
// @Override
// public void clear() {
//
// }
//}
