package io.github.matthewacon.pal.util;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.github.matthewacon.pal.util.LambdaUtils.*;
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

 //JCTree utilities
 public static Vector<JCAnnotation> processAnnotations(final JCCompilationUnit compilationUnit) {
  final Vector<JCAnnotation> annotations = new Vector<>();
  List<? super JCTree> definitionLayer = nextDefinitionLayer(constructList(compilationUnit));
  boolean definitionRoundOver;
  do {
   definitionLayer = nextDefinitionLayer(constructList(definitionLayer), JCAnnotation.class);
   int annotationCount = definitionLayer
    .stream()
    .filter(JCAnnotation.class::isInstance)
    .collect(Collectors.toList())
    .size();
   definitionRoundOver = annotationCount == definitionLayer.size();
  } while(!definitionRoundOver);
  definitionLayer.forEach(annotation -> annotations.add((JCAnnotation)annotation));
  return annotations;
 }

 //TODO There has to be a cleaner way to do this, reflection maybe?
 //TODO Create static final inner class that acts
 private static List<? super JCTree> nextDefinitionLayer(
  final List<? super JCTree> trees,
  final Class<? extends JCTree>... preserve
 ) {
  final HashSet<Class<? extends JCTree>> preserved = new HashSet<>();
  for (final Class<? extends JCTree> toPreserve : preserve) {
   preserved.add(toPreserve);
  }
  final Vector<? super JCTree> definitions = new Vector<>();
  for (final Object tree : trees) {
   cswitch(tree,
    //JCAnnotation case - catch all annotations
    definitionCcase(
     definitions,
     preserved,
     JCAnnotation.class,
     annotation -> {
      definitions.add(annotation.annotationType);
      definitions.addAll(annotation.args);
     }
    ),
    //LetExpr case
    definitionCcase(
     definitions,
     preserved,
     LetExpr.class, expr -> {
      definitions.addAll(expr.defs);
      definitions.add(expr.expr);
    }),
    //JCErroneous case
    definitionCcase(
     definitions,
     preserved,
     JCErroneous.class,
     error -> definitions.addAll(error.errs)
    ),
    //JCAnnotatedType case
    definitionCcase(
     definitions,
     preserved,
     JCAnnotatedType.class,
     annotatedType -> {
      definitions.addAll(annotatedType.annotations);
      definitions.add(annotatedType.underlyingType);
    }),
    //JCModifiers case
    definitionCcase(
     definitions,
     preserved,
     JCModifiers.class,
     mod -> definitions.addAll(mod.annotations)
    ),
    //JCWildcard case
    definitionCcase(
     definitions,
     preserved,
     JCWildcard.class,
     wildCard -> definitions.add(wildCard.inner)
    ),
    //JCTypeParameter case
    definitionCcase(
     definitions,
     preserved,
     JCTypeParameter.class,
     typeParameter -> {
      definitions.addAll(typeParameter.bounds);
      definitions.addAll(typeParameter.annotations);
    }),
    //JCTypeIntersection case
    definitionCcase(
     definitions,
     preserved,
     JCTypeIntersection.class,
     typeIntersection -> definitions.addAll(typeIntersection.bounds)
    ),
    //JCTypeUnion case
    definitionCcase(
     definitions,
     preserved,
     JCTypeUnion.class,
     typeUnion -> definitions.addAll(typeUnion.alternatives)
    ),
    //JCTypeApply case
    definitionCcase(
     definitions,
     preserved,
     JCTypeApply.class,
     typeApply -> {
      definitions.add(typeApply.clazz);
      definitions.addAll(typeApply.arguments);
    }),
    //JCArrayTypeTree case
    definitionCcase(
     definitions,
     preserved,
     JCArrayTypeTree.class,
     arrayTypeTree -> definitions.add(arrayTypeTree.elemtype)
    ),
    //JCMemberReference case
    definitionCcase(
     definitions,
     preserved,
     JCMemberReference.class,
     memberReference -> {
      definitions.add(memberReference.expr);
      definitions.addAll(memberReference.typeargs);
    }),
    //JCFieldAccess case
    definitionCcase(
     definitions,
     preserved,
     JCFieldAccess.class,
     fieldAccess -> definitions.add(fieldAccess.selected)
    ),
    //JCArrayAccess case
    definitionCcase(
     definitions,
     preserved,
     JCArrayAccess.class,
     arrayAccess -> {
      definitions.add(arrayAccess.indexed);
      definitions.add(arrayAccess.index);
    }),
    //JCInstanceOf case
    definitionCcase(
     definitions,
     preserved,
     JCInstanceOf.class,
     instanceOf -> {
      definitions.add(instanceOf.expr);
      definitions.add(instanceOf.clazz);
    }),
    //JCTypeCast case
    definitionCcase(
     definitions,
     preserved,
     JCTypeCast.class,
     typeCast -> {
      definitions.add(typeCast.clazz);
      definitions.add(typeCast.expr);
    }),
    //JCBinary case
    definitionCcase(
     definitions,
     preserved,
     JCBinary.class,
     binary -> {
      definitions.add(binary.lhs);
      definitions.add(binary.rhs);
    }),
    //JCUnary case
    definitionCcase(
     definitions,
     preserved,
     JCUnary.class,
     unary -> definitions.add(unary.arg)
    ),
    //JCAssignOp case
    definitionCcase(
     definitions,
     preserved,
     JCAssignOp.class,
     assignOp -> {
      definitions.add(assignOp.lhs);
      definitions.add(assignOp.rhs);
    }),
    //JCAssign case
    definitionCcase(
     definitions,
     preserved,
     JCAssign.class,
     assign -> {
      definitions.add(assign.lhs);
      definitions.add(assign.rhs);
    }),
    //JCParens case
    definitionCcase(
     definitions,
     preserved,
     JCParens.class,
     parens -> definitions.add(parens.expr)
    ),
    //JCLambda case
    definitionCcase(
     definitions,
     preserved,
     JCLambda.class,
     lambda -> {
      definitions.addAll(lambda.params);
      definitions.add(lambda.body);
    }),
    //JCNewArray case
    definitionCcase(
     definitions,
     preserved,
     JCNewArray.class,
     newArray -> {
      definitions.add(newArray.elemtype);
      definitions.addAll(newArray.dims);
      definitions.addAll(newArray.annotations);
      newArray.dimAnnotations.forEach(
       dimAnnotations -> definitions.addAll(dimAnnotations)
      );
      definitions.addAll(newArray.elems);
    }),
    //JCNewClass case
    //TODO special case
    definitionCcase(
     definitions,
     preserved,
     JCNewClass.class,
     newClass -> {
      definitions.add(newClass.encl);
      definitions.addAll(newClass.typeargs);
      definitions.add(newClass.clazz);
      definitions.addAll(newClass.args);
      definitions.add(newClass.def);
    }),
    //JCMethodInvocation case
    definitionCcase(
     definitions,
     preserved,
     JCMethodInvocation.class,
     methodInvocation -> {
      definitions.addAll(methodInvocation.typeargs);
      definitions.add(methodInvocation.meth);
      definitions.addAll(methodInvocation.args);
    }),
    //JCAssert case
    definitionCcase(
     definitions,
     preserved,
     JCAssert.class,
     jcAssert -> {
      definitions.add(jcAssert.cond);
      definitions.add(jcAssert.detail);
    }),
    //JCThrow case
    definitionCcase(
     definitions,
     preserved,
     JCThrow.class,
     jcThrow -> definitions.add(jcThrow.expr)
    ),
    //JCReturn case
    definitionCcase(
     definitions,
     preserved,
     JCReturn.class,
     jcReturn -> definitions.add(jcReturn.expr)
    ),
    //JCContinue case
    definitionCcase(
     definitions,
     preserved,
     JCContinue.class,
     jcContinue -> definitions.add(jcContinue.target)
    ),
    //JCBreak case
    definitionCcase(
     definitions,
     preserved,
     JCBreak.class,
     jcBreak -> definitions.add(jcBreak.target)
    ),
    //JCExpressionStatement case
    definitionCcase(
     definitions,
     preserved,
     JCExpressionStatement.class,
     expressionStatement -> definitions.add(expressionStatement.expr)
    ),
    //JCIf case
    definitionCcase(
     definitions,
     preserved,
     JCIf.class,
     jcIf -> {
      definitions.add(jcIf.cond);
      definitions.add(jcIf.thenpart);
      definitions.add(jcIf.elsepart);
    }),
    //JCConditional case
    definitionCcase(
     definitions,
     preserved,
     JCConditional.class,
     conditional -> {
      definitions.add(conditional.cond);
      definitions.add(conditional.truepart);
      definitions.add(conditional.falsepart);
    }),
    //JCCatch case
    definitionCcase(
     definitions,
     preserved,
     JCCatch.class,
     jcCatch -> {
      definitions.add(jcCatch.param);
      definitions.add(jcCatch.body);
    }),
    //JCTry case
    definitionCcase(
     definitions,
     preserved,
     JCTry.class,
     jcTry -> {
      definitions.add(jcTry.body);
      definitions.addAll(jcTry.catchers);
      definitions.add(jcTry.finalizer);
      definitions.addAll(jcTry.resources);
    }),
    //JCSynchronized case
    definitionCcase(
     definitions,
     preserved,
     JCSynchronized.class,
     jcSynchronized -> {
      definitions.add(jcSynchronized.lock);
      definitions.add(jcSynchronized.body);
    }),
    //JCCase case
    definitionCcase(
     definitions,
     preserved,
     JCCase.class,
     jcCase -> {
      definitions.add(jcCase.pat);
      definitions.addAll(jcCase.stats);
    }),
    //JCSwitch case
    definitionCcase(
     definitions,
     preserved,
     JCSwitch.class,
     jcSwitch -> {
      definitions.add(jcSwitch.selector);
      definitions.addAll(jcSwitch.cases);
    }),
    //JCLabeledStatement case
    definitionCcase(
     definitions,
     preserved,
     JCLabeledStatement.class,
     labeledStatement -> definitions.add(labeledStatement.body)
    ),
    //JCEnhancedForLoop case
    definitionCcase(
     definitions,
     preserved,
     JCEnhancedForLoop.class,
     enhancedForLoop -> {
      definitions.add(enhancedForLoop.var);
      definitions.add(enhancedForLoop.expr);
      definitions.add(enhancedForLoop.body);
    }),
    //JCForLoop case
    definitionCcase(
     definitions,
     preserved,
     JCForLoop.class,
     forLoop -> {
      definitions.addAll(forLoop.init);
      definitions.add(forLoop.cond);
      definitions.addAll(forLoop.step);
      definitions.add(forLoop.body);
    }),
    //JCWhileLoop case
    definitionCcase(
     definitions,
     preserved,
     JCWhileLoop.class,
     whileLoop -> {
      definitions.add(whileLoop.cond);
      definitions.add(whileLoop.body);
    }),
    //JCDoWhileLoop case
    definitionCcase(
     definitions,
     preserved,
     JCDoWhileLoop.class,
     doWhileLoop -> {
      definitions.add(doWhileLoop.body);
      definitions.add(doWhileLoop.cond);
    }),
    //JCBlock case
    definitionCcase(
     definitions,
     preserved,
     JCBlock.class,
     block -> definitions.addAll(block.stats)
    ),
    //JCVariableDecl case
    definitionCcase(
     definitions,
     preserved,
     JCVariableDecl.class,
     variableDecl -> {
      definitions.add(variableDecl.mods);
      definitions.add(variableDecl.nameexpr);
      definitions.add(variableDecl.vartype);
      definitions.add(variableDecl.init);
    }),
    //JCMethodDecl case
    definitionCcase(
     definitions,
     preserved,
     JCMethodDecl.class,
     methodDecl -> {
      definitions.add(methodDecl.mods);
      definitions.add(methodDecl.restype);
      definitions.addAll(methodDecl.typarams);
      definitions.add(methodDecl.recvparam);
      definitions.addAll(methodDecl.params);
      definitions.addAll(methodDecl.thrown);
      definitions.add(methodDecl.body);
      definitions.add(methodDecl.defaultValue);
    }),
    //JCClassDecl case
    definitionCcase(
     definitions,
     preserved,
     JCClassDecl.class,
     classDecl -> {
      definitions.add(classDecl.mods);
      definitions.addAll(classDecl.typarams);
      definitions.add(classDecl.extending);
      definitions.addAll(classDecl.implementing);
      definitions.addAll(classDecl.defs);
    }),
    //JCImport case
    definitionCcase(
     definitions,
     preserved,
     JCImport.class,
     jcImport -> definitions.add(jcImport.qualid)
    ),
    //JCCompilationUnit case
    definitionCcase(
     definitions,
     preserved,
     JCCompilationUnit.class,
     compilationUnit -> {
      definitions.addAll(compilationUnit.packageAnnotations);
      definitions.add(compilationUnit.pid);
      definitions.addAll(compilationUnit.defs);
    })
   );
  }
  return constructList(
   //Filter out all null elements
   definitions
    .stream()
    .filter(Objects::nonNull)
    .collect(Collectors.toList())
  );
 }

 private static <C> Case<C> definitionCcase(
  final Vector<? super JCTree> definitions,
  final Set<Class<? extends JCTree>> toPreserve,
  final Class<C> clazz,
  final Consumer<? super C> ccase
 ) {
  return new Case<C>(clazz, ccase) {
   @Override
   public void accept(final Object obj) {
    if (toPreserve.contains(obj.getClass())) {
     definitions.add((JCTree)obj);
    } else {
     super.accept(obj);
    }
   }
  };
 }

 public interface TreeTraversalFunction {
//  <T extends JCTree> T process(
//   final T tree,
//   final JCTree elem,
//   final int layer
//  );
  JCTree process(
   final JCTree tree,
   final JCTree elem,
   final int layer
  );

  static <T extends JCTree> TreeTraversalFunction remove(final T toRemove) {
   return remove(toRemove.getClass());
  }

  static <T extends JCTree> TreeTraversalFunction remove(final Class<T> toRemove) {
   return (tree, elem, layer) -> {
    if (elem.getClass().equals(toRemove)) {
     return null;
    }
    return elem;
   };
  }
  //TODO include a broader range of tree traversal functions
 }

 public static <T extends JCTree> void traverseTree(final T tree, final TreeTraversalFunction ttf) {
  try {
   LinkedList<JCTree>
    variables = new LinkedList<>(),
    lastVariables = new LinkedList<>();
   variables.add(tree);
   int layer = -1;
   while (!variables.equals(lastVariables)) {
    lastVariables = new LinkedList<>(variables);
    variables = new LinkedList<>();
    layer++;
    for (final JCTree currentUnit : lastVariables) {
     final Class<? extends JCTree> type = currentUnit.getClass();
     for (final Field f : type.getFields()) {
      f.setAccessible(true);
      final Class<?> fieldType = f.getType();
      if (ClassUtils.findCommonAncestor(Collection.class, fieldType).equals(Collection.class)) {
       final Class<? extends JCTree> genericType = ClassUtils.getGenericParameter(fieldType);
       if (ClassUtils.findCommonAncestor(JCTree.class, genericType).equals(JCTree.class)) {
        final LinkedList<JCTree>
         originalElements = new LinkedList<>((List<? extends JCTree>)f.get(currentUnit)),
         transformedElements = new LinkedList<>();
        for (final JCTree element : originalElements) {
         transformedElements.add(ttf.process(tree, element, layer));
        }
        f.set(transformedElements, currentUnit);
       }
      } else if (ClassUtils.findCommonAncestor(JCTree.class, fieldType).equals(JCTree.class)) {
       f.set(ttf.process(currentUnit, (JCTree)f.get(currentUnit), layer), currentUnit);
      } else {
       throw new IllegalArgumentException(
        "Unexpected JCTree field type: '" +
        type.getCanonicalName() +
        "', in JCTree: '" +
        type.getCanonicalName() +
        "'!");
      }
     }
    }
   }
  } catch (Throwable t) {
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
