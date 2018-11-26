package io.github.matthewacon.pal.util;

import java.util.LinkedList;

public abstract class ExampleLinkedTreeMap<T extends ExampleLinkedTreeMap<T>> implements AbstractTreeMap<T, LinkedList<T>> {
 private T parent;
 private final LinkedList<T> children;

 public ExampleLinkedTreeMap() {
  this(null);
 }

 public ExampleLinkedTreeMap(final T parent) {
  this.parent = parent;
  this.children = new LinkedList<>();
 }

 @Override
 public final T getParent() {
  return this.parent;
 }

 @Override
 public final void setParent(final T parent) {
  this.parent = parent;
 }

 @Override
 public final LinkedList<T> getChildren() {
  return children;
 }

// public void example() {
//  final ExampleLinkedTreeMap
//   root = new ExampleLinkedTreeMap(),
//   child1 = new ExampleLinkedTreeMap(root),
//   child2 = new ExampleLinkedTreeMap(child1);
//
//  TreeTraversalMethod ttm = TreeTraversalMethod.topDown();
//  root.traverseTree(ttm, ttm::flatten);
//  TreeTraversalMethod ttm1 = TreeTraversalMethod.preorder();
//  root.traverseTree(ttm1, ttm1.remove(child2));
//  TreeTraversalMethod ttm2 = TreeTraversalMethod.postorder();
//  root.traverseTree(ttm2, (base, elem) -> base);
//  root.traverseTree((base, elem) -> base.equals(elem) ? null : elem);
// }
}