package io.github.matthewacon.pal.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

//TODO implement default Iterator methods
//TODO Should all trees be immutable?
public interface AbstractTreeMap<M extends AbstractTreeMap<M, C>, C extends Collection<M>> extends Cloneable {
//implements Cloneable, Iterable<AbstractTreeMap<T, C, M>> {
 interface TreeTraversalFunction<M extends AbstractTreeMap<M, C>, C extends Collection<M>> {
  M process(final M parent, final M elem);
 }

 //E n u m s d o n ' t s u p p o r t g e n e r i c p a r a m e t e r s s o h e r e ' s a n i n t e r f a c e
 interface TreeTraversalMethod<M extends AbstractTreeMap<M, C>, C extends Collection<M>> {
  M traverse(final M root, final TreeTraversalFunction<M, C>... ttf);

  default TreeTraversalFunction<M, C> remove(final M toRemove) {
   return (parent, elem) -> {
    if (elem == null) {
     return null;
    } else {
     return elem.equals(toRemove) ? null : elem;
    }
   };
  }

//  //Reflects the tree about the center of the vertical cross-section of the map
//  TreeTraversalFunction<M, C> reflect();
//
//  //Reflects the tree about the center of the horizontal cross-section of the map
//  TreeTraversalFunction<M, C> invert();
//
//  //Flattens the map down to 2 layers: the root and other nodes in the tree as root's children
//  TreeTraversalFunction<M, C> flatten();

  //TODO Generic parameters (and parameter sets) as first-order citizens
  /*Would simplify this entire data structure to something like the following
   //defined in AbstractTreeMap class
   static <> AbstractTreeMap_TYPE_PARAMETERS {
    def T
    def M extends AbstractTreeMap<AbstractTreeMap_TYPE_PARAMETERS>
    def C extends Collection<T>
   }

   //Example usage TreeTraversalMethod#preorder
   static <AbstractTreeMap_TYPE_PARAMETERS> preorder() {
    ...
   }
   */

  //Conventional traversal methods
  static <M extends AbstractTreeMap<M, C>, C extends Collection<M>> TreeTraversalMethod<M, C> preorder() {
   //TODO implement
   return null;
  }

  static <M extends AbstractTreeMap<M, C>, C extends Collection<M>> TreeTraversalMethod<M, C> postorder() {
   //TODO implement
   return null;
  }

  //Level order traversal methods (LTR only -- combine with 'TreeTraversalMethod::reflect' to iterate from RTL)
  static <M extends AbstractTreeMap<M, C>, C extends Collection<M>> TreeTraversalMethod<M, C> topDown() {
   //TODO severed branches must also have their parent references removed
   return (M root, final TreeTraversalFunction<M, C>... ttfs) -> {
    LinkedList<M>
     currentIteration = new LinkedList<>(),
     lastIteration;
    currentIteration.add(root);
    while (currentIteration.size() > 0) {
     lastIteration = new LinkedList<>(currentIteration);
     currentIteration.clear();
     C
      parentChildrenRef = null,
      lastParent = null;
     for (M branch : lastIteration) {
      final M parent = branch.getParent();
      final boolean hasParent = parent != null;
      final C branchChildren = branch.getChildren();
      if (hasParent) {
       lastParent = parentChildrenRef;
       parentChildrenRef = parent.getChildren();
       if (!parentChildrenRef.equals(lastParent)) {
        parentChildrenRef.clear();
       }
      }
      for (final TreeTraversalFunction<M, C> ttf : ttfs) {
       if (branch != null) {
        branch = ttf.process(parent, branch);
       } else {
        break;
       }
      }
      if (branch == null) {
       if (!hasParent) {
        //If the root element was removed from the tree then there is no more tree
        return null;
       }
      } else {
       if (hasParent) {
        parentChildrenRef.add(branch);
       }
       currentIteration.addAll(branchChildren);
      }
     }
    }
    return root;
   };
  }

  static <M extends AbstractTreeMap<M, C>, C extends Collection<M>> TreeTraversalMethod<M, C> bottomUp() {
   //TODO implement
   return null;
  }
 }

 M getParent();

 void setParent(final M parent);

 C getChildren();

 //TODO should the default implementations be final?
 default boolean addChild(final M child) {
  child.setParent((M)this);
  return getChildren().add(child);
 }

 default boolean addChildren(final C children) {
  children.forEach(child -> child.setParent((M)AbstractTreeMap.this));
  return getChildren().addAll(children);
 }

 default boolean removeChild(final M child) {
  child.setParent(null);
  return getChildren().remove(child);
 }

 default boolean removeChildren(final C children) {
  children.forEach(child -> child.setParent(null));
  return getChildren().removeAll(children);
 }

// default Object clone() {
//  return shallowClone();
// }

 //TODO Doc: shallow clones the subclass instance, disregarding any tree information
 M shallowClone();

 @Override
 public abstract boolean equals(final Object obj);

 default M traverseTree(final TreeTraversalMethod<M, C> ttm, final Supplier<TreeTraversalFunction<M, C>>... ttf) {
  return traverseTree(
   ttm,
   (TreeTraversalFunction<M, C>[])Arrays
    .stream(ttf)
    .map(func -> func.get())
    .collect(Collectors.toList())
    .toArray()
  );
 }

 default M traverseTree(final TreeTraversalMethod<M, C> ttm, final TreeTraversalFunction<M, C>... ttf) {
  if (ttf.length > 0) {
   return ttm.traverse((M)this, ttf);
  }
  return (M)this;
 }

 //Default tree traversal method -> topDown
 //TODO should this be included?
 default M traverseTree(final TreeTraversalFunction<M, C>... ttf) {
  if (ttf.length > 0) {
   return TreeTraversalMethod.<M, C>topDown().traverse((M)this, ttf);
  }
  return (M)this;
 }

// //TODO Doc: returns a default iterator that uses TreeTraversalMethod::topDown
// //Inefficient, flattens tree structure and then iterates through a second time
// @Override
// public final Iterator<AbstractTreeMap<T, C, M>> iterator() {
//  final AbstractTreeMap<T, C, M> flat = AbstractTreeMap.this.clone().traverseTree(TreeTraversalMethod::topDown, TreeTraversalFunction::flatten);
//  final LinkedList<AbstractTreeMap<T, C, M>> flattened = new LinkedList<>(flat.getChildren());
//  flattened.push(flat);
//  flattened.push(flat);
//  return flattened.iterator();
// }
}