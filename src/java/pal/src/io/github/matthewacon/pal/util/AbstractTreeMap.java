package io.github.matthewacon.pal.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

//TODO implement default Iterator methods
//TODO Should all trees be immutable?
public abstract class AbstractTreeMap<M extends AbstractTreeMap<M, C>, C extends Collection<M>> implements Cloneable {
//implements Cloneable, Iterable<AbstractTreeMap<T, C, M>> {
 public interface TreeTraversalFunction<M extends AbstractTreeMap<M, C>, C extends Collection<M>> {
  M process(final M parent, final M elem);
 }

 //E n u m s d o n ' t s u p p o r t g e n e r i c p a r a m e t e r s s o h e r e ' s a n i n t e r f a c e
 public interface TreeTraversalMethod<M extends AbstractTreeMap<M, C>, C extends Collection<M>> {
  M traverse(final M root, final TreeTraversalFunction<M, C>... ttf);

  default TreeTraversalFunction<M, C> remove(final M toRemove) {
   return (parent, elem) -> {
    if (toRemove == null) {
     return elem == null ? null : elem;
    } else {
     return elem.equals(toRemove) ? null : elem;
    }
   };
  }

  //Reflects the tree about the center of the vertical cross-section of the map
  TreeTraversalFunction<M, C> reflect();

  //Reflects the tree about the center of the horizontal cross-section of the map
  TreeTraversalFunction<M, C> invert();

  //Flattens the map down to 2 layers: the root and other nodes in the tree as root's children
  TreeTraversalFunction<M, C> flatten();

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
   return new TreeTraversalMethod<M, C>() {
    @Override
    public M traverse(M root, TreeTraversalFunction<M, C>... ttfs) {
     C childrenRef = root.getChildren();
     LinkedList<M>
      childrenClone = new LinkedList<>(childrenRef),
      currentIteration,
      nextIteration = new LinkedList<>();
     childrenRef.clear();
     nextIteration.add(root);
     M
      currentParent = root.getParent(),
      lastParent;
     while (nextIteration.size() > 0) {
      currentIteration = new LinkedList<>(nextIteration);
      nextIteration.clear();
      for (M branch : currentIteration) {
       lastParent = currentParent;
       currentParent = branch.getParent();
       for (final TreeTraversalFunction<M, C> ttf : ttfs) {
        branch = ttf.process(currentParent, branch);
       }
       if (branch != null) {
        if (currentParent != null) {
         if (!currentParent.equals(lastParent)) {
          childrenRef = currentParent.getChildren();
          childrenClone = new LinkedList<>(childrenRef);
          childrenRef.clear();
         }
         childrenRef.add(branch);
        }
        nextIteration.addAll(childrenClone);
       }
      }
     }
     return root;
    }

    @Override
    public TreeTraversalFunction<M, C> reflect() {
     return (parent, elem) -> {
      final LinkedList<M> children = new LinkedList<>(parent.getChildren());
      return children.get(children.size() - children.indexOf(elem) - 1);
     };
    }

    @Override
    public TreeTraversalFunction<M, C> invert() {
     //TODO implement
     return null;
    }

    @Override
    public TreeTraversalFunction<M, C> flatten() {
     //TODO implement
     return null;
    }
   };
  }

  static <M extends AbstractTreeMap<M, C>, C extends Collection<M>> TreeTraversalMethod<M, C> bottomUp() {
   //TODO implement
   return null;
  }
 }

// private final M parent;
//
// public AbstractTreeMap() {
//  this(null);
// }
//
// public AbstractTreeMap(final M parent) {
//  this.parent = parent;
// }

 public abstract M getParent();

 public abstract void setParent(final M parent);

 public abstract C getChildren();

 //TODO should the default implementations be final?
 public boolean addChild(final M child) {
  child.setParent((M)this);
  return getChildren().add(child);
 }

 public boolean addChildren(final C children) {
  children.forEach(child -> child.setParent((M)AbstractTreeMap.this));
  return getChildren().addAll(children);
 }

 public boolean removeChild(final M child) {
  child.setParent(null);
  return getChildren().remove(child);
 }

 public boolean removeChildren(final C children) {
  children.forEach(child -> child.setParent(null));
  return getChildren().removeAll(children);
 }

 //TODO Doc: shallow clones the subclass instance, disregarding any tree information
 @Override
 public abstract M clone();

 @Override
 public abstract boolean equals(final Object obj);

 public final M traverseTree(final TreeTraversalMethod<M, C> ttm, final Supplier<TreeTraversalFunction<M, C>>... ttf) {
  return traverseTree(
   ttm,
   (TreeTraversalFunction<M, C>[])Arrays
    .stream(ttf)
    .map(func -> func.get())
    .collect(Collectors.toList())
    .toArray()
  );
 }

 public final M traverseTree(final TreeTraversalMethod<M, C> ttm, final TreeTraversalFunction<M, C>... ttf) {
  if (ttf.length > 0) {
   return ttm.traverse((M)this, ttf);
  }
  return (M)this;
 }

 //Default tree traversal method -> topDown
 //TODO should this be included?
 public final M traverseTree(final TreeTraversalFunction<M, C>... ttf) {
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