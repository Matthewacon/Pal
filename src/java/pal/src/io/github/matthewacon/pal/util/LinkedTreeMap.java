package io.github.matthewacon.pal.util;

import java.util.Collection;
import java.util.LinkedList;

//TODO standardize to be compatible with jdk (see TreeMap)
public final class LinkedTreeMap<T> {
 public interface TreeTraversalFunction<E, T extends LinkedTreeMap<E>> {
  T process(final T root, final T elem);

//  static <E> TreeTraversalFunction<E, LinkedTreeMap<E>> remove(final LinkedTreeMap<E> toRemove) {
//   return (root, elem) -> {
//
//   };
//  }
 }

 private final LinkedTreeMap<T> parent;
 private final T value;
 private final LinkedList<LinkedTreeMap<T>> children;

 public LinkedTreeMap(final LinkedTreeMap<T> parent, final T value) {
  this.parent = parent;
  this.value = value;
  this.children = new LinkedList<>();
 }

 public LinkedTreeMap(final T value) {
  this(null, value);
 }

 //TODO implement
// @Override
// public boolean equals(final Object object) {
//
// }

 public boolean isLeaf() {
  return children.size() == 0;
 }

 public LinkedTreeMap<T> getParent() {
  return parent;
 }

 public T getValue() {
  return value;
 }

 public LinkedList<LinkedTreeMap<T>> getChildren() {
  return new LinkedList<>(children);
 }

 public void addChild(final LinkedTreeMap<T> child) {
  children.add(child);
 }

 public void addChildren(final Collection<LinkedTreeMap<T>> children) {
  this.children.addAll(children);
 }

 //TODO convert to use traverseTree
 public int totalElements() {
  LinkedList<LinkedTreeMap<T>>
   round = null,
   newRound = new LinkedList<>();
  newRound.add(this);
  int count = 1;
  while (!newRound.equals(round)) {
   round = newRound;
   newRound = new LinkedList<>();
   for (final LinkedTreeMap<T> child : round) {
    count++;
    newRound.add(child);
   }
  }
  return count;
 }

 //TODO convert to use traverseTree
 public int numNodesAt(final int level) {
  int index = 0;
  LinkedList<LinkedTreeMap<T>>
   round,
   newRound = new LinkedList<>();
  while (index < level) {
   round = newRound;
   newRound = new LinkedList<>();
   for (final LinkedTreeMap<T> child : round) {
    newRound.addAll(child.children);
   }
   index++;
  }
  return newRound.size();
 }

 //TODO convert to use traverseTree
 public LinkedList<LinkedTreeMap<T>> nodesAt(final int level) {
  int index = 0;
  LinkedList<LinkedTreeMap<T>>
   round,
   newRound = new LinkedList<>();
  newRound.add(this);
  while (index < level) {
   round = newRound;
   newRound = new LinkedList<>();
   for (final LinkedTreeMap<T> child : round) {
    newRound.addAll(child.children);
   }
   if (newRound.size() == 0) {
    throw new IndexOutOfBoundsException("" + level);
   }
   index++;
  }
  return newRound;
 }

 public LinkedTreeMap<T> nodeAt(final int level, final int depth) {
  final LinkedTreeMap<T> node;
  try {
   node = nodesAt(level).get(depth);
  } catch (IndexOutOfBoundsException e) {
   return null;
  }
  return node;
 }

// //TODO implement
// public void traverseTree(final TreeTraversalFunction<T, LinkedTreeMap<T>> ttf) {
//
// }
}