package io.github.matthewacon.pal.util;

import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.Context;
import io.github.matthewacon.pal.agent.PalAgent;
import io.github.matthewacon.pal.api.annotations.bytecode.DummyAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

public final class RuntimeAnnotationGenerator {
 private static final Field JavaCompiler_parserFactory;

 public static final String
  ANNOTATION_IMPL_STUB,
  ANNOTATION_METHOD_IMPL_STUB;

 static {
  try {
   JavaCompiler_parserFactory = JavaCompiler.class.getDeclaredField("parserFactory");
   JavaCompiler_parserFactory.setAccessible(true);
  } catch (Throwable t) {
   throw new RuntimeException(t);
  }
  //TODO fetch resources
  ANNOTATION_IMPL_STUB = ANNOTATION_METHOD_IMPL_STUB = null;
 }

 private final HashSet<String> registeredAnnotations;
 private final Context context;
 private final JavaCompiler compiler;
 private final ParserFactory parserFactory;

 public RuntimeAnnotationGenerator() {
  this.registeredAnnotations = new HashSet<>();
  this.context = new Context();
  this.compiler = JavaCompiler.instance(context);
  PalAgent.exclude(compiler, true, false);
  try {
   this.parserFactory = (ParserFactory)JavaCompiler_parserFactory.get(compiler);
  } catch (Throwable t) {
   throw new RuntimeException(t);
  }
 }

 public <T extends Annotation> Vector<T> generateAnnotations(final JCCompilationUnit baseUnit) {
  return null;
 }

 //TODO import correct annotation
 public <T extends Annotation> T generateAnnotation(final JCCompilationUnit baseUnit, final JCAnnotation annotation) {
  final String baseName = ((JCIdent)annotation.annotationType).name.toString();
  //Add required imports

  //Generate name and implement abstract method Annotation#annotationType
  String code = ANNOTATION_IMPL_STUB
   .replaceAll("[$]NAME", generateName(baseName))
   .replaceAll("[$]INTERFACE", baseName);
  //Generate methods
  for (final JCExpression arg : annotation.args) {
   code = code.replaceAll("[$]METHOD_STUB", generateMethod(arg));
  }
  //Remove remaining "$METHOD_STUB"
  code = code.replace("$METHOD_STUB", "");
  final JavacParser parser = parserFactory.newParser(code, true, true, true);
  final JCCompilationUnit compilationUnit = parser.parseCompilationUnit();
  return null;
 }

 //TODO
 private <T extends JCExpression> String generateMethod(final T arg) {
  if (!(arg instanceof JCAssign || arg instanceof JCLiteral)) {
   //TODO log through main compiler
   throw new IllegalArgumentException("Invalid annotation parameter: '" + arg + "'!");
  }
  return null;
 }

 //TODO check HashSet
 private String generateName(final String base) {
  String name;
  while (!registeredAnnotations.add((name = base + "_impl_" + new Random().nextLong())));
  return name;
 }
}
