package io.github.matthewacon.pal.util;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

 public static <T> List<T> constructList(final Set<T> set) {
  return constructList((T[]) set.toArray());
 }

 public static <T> List<T> constructList(final Collection<T> collection) {
  return constructList((T[]) collection.toArray());
 }

 public static <T> java.util.List toList(final List<T> list) {
  return new LinkedList<T>() {{
   addAll(list);
  }};
 }

 //JCTree utilities
 public static Vector<JCAnnotation> processAnnotations(final JCCompilationUnit compilationUnit) {
  final Vector<JCAnnotation> annotations = new Vector<>();
  List<? super JCTree> definitionLayer = nextDefinitionLayer(constructList(compilationUnit));
  boolean definitionRoundOver;
  do {
   definitionLayer = nextDefinitionLayer(constructList(definitionLayer));
   int annotationCount = definitionLayer
    .stream()
    .filter(JCAnnotation.class::isInstance)
    .collect(Collectors.toList())
    .size();
   definitionRoundOver = annotationCount == definitionLayer.size();
  } while(!definitionRoundOver);
  definitionLayer.forEach(annotation -> {
   try {
    annotations.add((JCAnnotation)annotation);
   } catch (ClassCastException e) {
    throw new RuntimeException("Non-JCAnnotation tree element passed through filter somehow!", e);
   }
  });
  return annotations;
 }

 private static List<? super JCTree> nextDefinitionLayer(final List<? super JCTree> trees) {
  final Vector<? super JCTree> definitions = new Vector<>();
  for (final Object tree : trees) {
   cswitch(tree,
    //JCAnnotation case - catch all annotations
    ccase(JCAnnotation.class, annotation -> definitions.add(annotation)),
    //LetExpr case
    ccase(LetExpr.class, expr -> {
     definitions.addAll(expr.defs);
     definitions.add(expr.expr);
    }),
    //JCErroneous case
    ccase(JCErroneous.class, error -> definitions.addAll(error.errs)),
    //JCAnnotatedType case
    ccase(JCAnnotatedType.class, annotatedType -> {
     definitions.addAll(annotatedType.annotations);
     definitions.add(annotatedType.underlyingType);
    }),
    //JCModifiers case
    ccase(JCModifiers.class, mod -> definitions.addAll(mod.annotations)),
    //JCWildcard case
    ccase(JCWildcard.class, wildCard -> definitions.add(wildCard.inner)),
    //JCTypeParameter case
    ccase(JCTypeParameter.class, typeParameter -> {
     definitions.addAll(typeParameter.bounds);
     definitions.addAll(typeParameter.annotations);
    }),
    //JCTypeIntersection case
    ccase(JCTypeIntersection.class, typeIntersection -> definitions.addAll(typeIntersection.bounds)),
    //JCTypeUnion case
    ccase(JCTypeUnion.class, typeUnion -> definitions.addAll(typeUnion.alternatives)),
    //JCTypeApply case
    ccase(JCTypeApply.class, typeApply -> {
     definitions.add(typeApply.clazz);
     definitions.addAll(typeApply.arguments);
    }),
    //JCArrayTypeTree case
    ccase(JCArrayTypeTree.class, arrayTypeTree -> definitions.add(arrayTypeTree.elemtype)),
    //JCMemberReference case
    ccase(JCMemberReference.class, memberReference -> {
     definitions.add(memberReference.expr);
     definitions.addAll(memberReference.typeargs);
    }),
    //JCFieldAccess case
    ccase(JCFieldAccess.class, fieldAccess -> definitions.add(fieldAccess.selected)),
    //JCArrayAccess case
    ccase(JCArrayAccess.class, arrayAccess -> {
     definitions.add(arrayAccess.indexed);
     definitions.add(arrayAccess.index);
    }),
    //JCInstanceOf case
    ccase(JCInstanceOf.class, instanceOf -> {
     definitions.add(instanceOf.expr);
     definitions.add(instanceOf.clazz);
    }),
    //JCTypeCast case
    ccase(JCTypeCast.class, typeCast -> {
     definitions.add(typeCast.clazz);
     definitions.add(typeCast.expr);
    }),
    //JCBinary case
    ccase(JCBinary.class, binary -> {
     definitions.add(binary.lhs);
     definitions.add(binary.rhs);
    }),
    //JCUnary case
    ccase(JCUnary.class, unary -> definitions.add(unary.arg)),
    //JCAssignOp case
    ccase(JCAssignOp.class, assignOp -> {
     definitions.add(assignOp.lhs);
     definitions.add(assignOp.rhs);
    }),
    //JCAssign case
    ccase(JCAssign.class, assign -> {
     definitions.add(assign.lhs);
     definitions.add(assign.rhs);
    }),
    //JCParens case
    ccase(JCParens.class, parens -> definitions.add(parens.expr)),
    //JCLambda case
    ccase(JCLambda.class, lambda -> {
     definitions.addAll(lambda.params);
     definitions.add(lambda.body);
    }),
    //JCNewArray case
    ccase(JCNewArray.class, newArray -> {
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
    ccase(JCNewClass.class, newClass -> {
     definitions.add(newClass.encl);
     definitions.addAll(newClass.typeargs);
     definitions.add(newClass.clazz);
     definitions.addAll(newClass.args);
     definitions.add(newClass.def);
    }),
    //JCMethodInvocation case
    ccase(JCMethodInvocation.class, methodInvocation -> {
     definitions.addAll(methodInvocation.typeargs);
     definitions.add(methodInvocation.meth);
     definitions.addAll(methodInvocation.args);
    }),
    //JCAssert case
    ccase(JCAssert.class, jcAssert -> {
     definitions.add(jcAssert.cond);
     definitions.add(jcAssert.detail);
    }),
    //JCThrow case
    ccase(JCThrow.class, jcThrow -> definitions.add(jcThrow.expr)),
    //JCReturn case
    ccase(JCReturn.class, jcReturn -> definitions.add(jcReturn.expr)),
    //JCContinue case
    ccase(JCContinue.class, jcContinue -> definitions.add(jcContinue.target)),
    //JCBreak case
    ccase(JCBreak.class, jcBreak -> definitions.add(jcBreak.target)),
    //JCExpressionStatement case
    ccase(JCExpressionStatement.class, expressionStatement -> definitions.add(expressionStatement.expr)),
    //JCIf case
    ccase(JCIf.class, jcIf -> {
     definitions.add(jcIf.cond);
     definitions.add(jcIf.thenpart);
     definitions.add(jcIf.elsepart);
    }),
    //JCConditional case
    ccase(JCConditional.class, conditional -> {
     definitions.add(conditional.cond);
     definitions.add(conditional.truepart);
     definitions.add(conditional.falsepart);
    }),
    //JCCatch case
    ccase(JCCatch.class, jcCatch -> {
     definitions.add(jcCatch.param);
     definitions.add(jcCatch.body);
    }),
    //JCTry case
    ccase(JCTry.class, jcTry -> {
     definitions.add(jcTry.body);
     definitions.addAll(jcTry.catchers);
     definitions.add(jcTry.finalizer);
     definitions.addAll(jcTry.resources);
    }),
    //JCSynchronized case
    ccase(JCSynchronized.class, jcSynchronized -> {
     definitions.add(jcSynchronized.lock);
     definitions.add(jcSynchronized.body);
    }),
    //JCCase case
    ccase(JCCase.class, jcCase -> {
     definitions.add(jcCase.pat);
     definitions.addAll(jcCase.stats);
    }),
    //JCSwitch case
    ccase(JCSwitch.class, jcSwitch -> {
     definitions.add(jcSwitch.selector);
     definitions.addAll(jcSwitch.cases);
    }),
    //JCLabeledStatement case
    ccase(JCLabeledStatement.class, labeledStatement -> definitions.add(labeledStatement.body)),
    //JCEnhancedForLoop case
    ccase(JCEnhancedForLoop.class, enhancedForLoop -> {
     definitions.add(enhancedForLoop.var);
     definitions.add(enhancedForLoop.expr);
     definitions.add(enhancedForLoop.body);
    }),
    //JCForLoop case
    ccase(JCForLoop.class, forLoop -> {
     definitions.addAll(forLoop.init);
     definitions.add(forLoop.cond);
     definitions.addAll(forLoop.step);
     definitions.add(forLoop.body);
    }),
    //JCWhileLoop case
    ccase(JCWhileLoop.class, whileLoop -> {
     definitions.add(whileLoop.cond);
     definitions.add(whileLoop.body);
    }),
    //JCDoWhileLoop case
    ccase(JCDoWhileLoop.class, doWhileLoop -> {
     definitions.add(doWhileLoop.body);
     definitions.add(doWhileLoop.cond);
    }),
    //JCBlock case
    ccase(JCBlock.class, block -> definitions.addAll(block.stats)),
    //JCVariableDecl case
    ccase(JCVariableDecl.class, variableDecl -> {
     definitions.add(variableDecl.mods);
     definitions.add(variableDecl.nameexpr);
     definitions.add(variableDecl.vartype);
     definitions.add(variableDecl.init);
    }),
    //JCMethodDecl case
    ccase(JCMethodDecl.class, methodDecl -> {
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
    ccase(JCClassDecl.class, classDecl -> {
     definitions.add(classDecl.mods);
     definitions.addAll(classDecl.typarams);
     definitions.add(classDecl.extending);
     definitions.addAll(classDecl.implementing);
     definitions.addAll(classDecl.defs);
    }),
    //JCImport case
    ccase(JCImport.class, jcImport -> definitions.add(jcImport.qualid)),
    //JCCompilationUnit case
    ccase(JCCompilationUnit.class, compilationUnit -> {
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

 private static final class Case<T> {
  public final Class<T> clazz;
  public final Consumer<? super T> consumer;

  public Case(Class<T> clazz, Consumer<? super T> consumer) {
   this.clazz = clazz;
   this.consumer = consumer;
  }

  public void accept(final Object obj) {
   this.consumer.accept((T)obj);
  }

  public boolean isInstance(final Object obj) {
   return clazz.isInstance(obj);
  }
 }

 private static <C> Case<C> ccase(Class<C> clazz, Consumer<? super C> ccase) {
  return new Case<>(clazz, ccase);
 }

 private static <T> void cswitch(T target, Case<?>... cases) {
  for (final Case<?> esac : cases) {
   if (esac.isInstance(target)) {
    esac.accept(target);
    break;
   }
  }
 }
}
