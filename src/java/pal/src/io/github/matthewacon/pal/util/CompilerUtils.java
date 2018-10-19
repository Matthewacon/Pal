package io.github.matthewacon.pal.util;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import io.github.matthewacon.pal.PalMain;

import static io.github.matthewacon.pal.util.LambdaUtils.*;

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
   head = (List<T>) javac_util_List.newInstance(null, null);
   for (int i = elements.length - 1; i > -1; i--) {
    head = (List<T>) javac_util_List.newInstance(elements[i], head);
   }
  } catch (Throwable t) {
   throw new RuntimeException(t);
  }
  return head;
 }

 public static <T> List<T> constructList(final Collection<T> collection) {
  return constructList((T[]) collection.toArray());
 }

 //TODO include a broader range of tree traversal functions
 public interface TreeTraversalFunction {
  //TODO generic lambda methods
//  <T extends JCTree> T process(
//   final T tree,
//   final JCTree elem,
//   final int layer
//  );
  //TODO interface prototypes without named arguments (unnecessary without an accompanying body)
  JCTree process(
   final JCTree root,
   final JCTree elem,
   final int layer
  );

  static <T extends JCTree> TreeTraversalFunction remove(final Class<T> toRemove) {
   return (tree, elem, layer) -> toRemove.isInstance(elem) ? null : elem;
  }

  static <T extends JCTree, C extends JCTree> Vector<C> aggregate(final T root, final Class<C> toAggregate) {
   final Vector<C> aggregates = new Vector<>();
   traverseTree(
    root,
    (tree, elem, layer) -> {
     if (elem.getClass().equals(toAggregate)) {
      aggregates.add((C)elem);
     }
     return elem;
    }
   );
   return aggregates;
  }
 }

 //TODO break apart into modifyTree and traverseTree (performance improvement for lambdas that do not modify the tree)
 //TODO further generalize modifyTree to accept a modification function
 public static <T extends JCTree> void traverseTree(final T tree, final TreeTraversalFunction ttf) {
  try {
   final LinkedList<JCTree> toProcess = new LinkedList<>();
   toProcess.add(tree);
   final int[] layer = { -1 };
   while (!toProcess.isEmpty()) {
    layer[0] += 1;
    final JCTree currentTree;
    try {
     currentTree = toProcess.pop();
    } catch (NoSuchElementException e) {
     break;
    }
    final Class<?> treeType = currentTree.getClass();
    for (final Field f : treeType.getFields()) {
     f.setAccessible(true);
     final Class<?> fieldType = f.getType();
     if (ClassUtils.hasCommonAncestor(JCTree.class, fieldType)) {
      final JCTree child = (JCTree)f.get(currentTree);
      if (child != null) {
       final JCTree newChild = ttf.process(currentTree, child, layer[0]);
       if (newChild != null) {
        toProcess.add(newChild);
       }
       f.set(currentTree, newChild);
      }
     } else if (ClassUtils.hasCommonAncestor(List.class, fieldType)) {
      final Class<? extends JCTree> genericType = (Class<? extends JCTree>)PalMain
       .PAL_CLASSLOADER
       .findClass(((ParameterizedType)f.getGenericType())
        .getActualTypeArguments()[0]
        .getTypeName()
       );
      if (ClassUtils.hasCommonAncestor(JCTree.class, genericType)) {
       final List<JCTree> children = (List<JCTree>)f.get(currentTree);
       if (children != null) {
        final LinkedList<JCTree> newChildren = new LinkedList<>();
        children
         .stream()
         .filter(child -> child != null)
         .forEach(child -> {
          if ((child = ttf.process(currentTree, child, layer[0])) != null) {
           newChildren.add(child);
          }
         });
        toProcess.addAll(newChildren);
        f.set(currentTree, constructList(newChildren));
       }
      }
     } else {
      continue;
     }
    }
   }
  } catch (Throwable t) {
   //TODO Error message
   throw new RuntimeException(t);
  }
 }

 public static String getFullyQuantifiedAnnotationName(final JCAnnotation annotation) {
  final StringBuilder sb = new StringBuilder();
  cswitch(annotation.annotationType,
   ccase(
    JCIdent.class,
    ident -> sb.append(ident.name.toString())
   ),
   ccase(
    JCFieldAccess.class,
    field -> sb.append(field.toString())
   ),
   ccase(
    null,
    cdefault -> {throw new IllegalArgumentException("Invalid annotation name type: '" + cdefault.getClass() + "'!");}
   )
  );
  return sb.toString();
 }

 public static String getBaseName(final JCAnnotation annotation) {
  final StringBuilder sb = new StringBuilder();
  cswitch(annotation.annotationType,
   ccase(
    JCIdent.class,
    ident -> sb.append(ident.name.toString())
   ),
   ccase(
    JCFieldAccess.class,
    field -> sb.append(field.name.toString())
   ),
   ccase(
    null,
    cdefault -> {throw new IllegalArgumentException("Invalid annotation name type: '" + cdefault.getClass() + "'!");}
   )
  );
  return sb.toString();
 }
}
