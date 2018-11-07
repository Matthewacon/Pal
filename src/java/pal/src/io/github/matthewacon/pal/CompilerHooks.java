package io.github.matthewacon.pal;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.jvm.ClassWriter;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import io.github.matthewacon.pal.agent.PalAgent;
import io.github.matthewacon.pal.util.CompilerUtils;
import io.github.matthewacon.pal.util.ExceptionUtils;
import io.github.matthewacon.pal.util.RuntimeAnnotationGenerator;

import javax.annotation.processing.Processor;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;

public final class CompilerHooks {
 public static final class LatentCompilationUnit {
  public final JCTree.JCCompilationUnit
   original,
   modified;

  public LatentCompilationUnit(final JCTree.JCCompilationUnit original, final JCTree.JCCompilationUnit modified) {
   this.original = original;
   this.modified = modified;
  }
 }

 private static final Field
  JavaCompiler_context,
  JavaCompiler_parserFactory;

 private static final LinkedHashSet<CompilerHooks> COMPILER_HOOKS;

 static {
  try {
   JavaCompiler_context = JavaCompiler.class.getDeclaredField("context");
   JavaCompiler_context.setAccessible(true);
   JavaCompiler_parserFactory = JavaCompiler.class.getDeclaredField("parserFactory");
   JavaCompiler_parserFactory.setAccessible(true);
  } catch (Throwable t) {
   throw ExceptionUtils.initFatal(t);
  }
  COMPILER_HOOKS = new LinkedHashSet<>();
 }

 private final JavaCompiler compiler;
 private final JavaFileManager fileManager;
 private final Vector<Symbol.ClassSymbol> compilerOutput;

 public CompilerHooks(final JavaCompiler compiler) {
  this.compiler = compiler;
  try {
   final Context context = (Context)JavaCompiler_context.get(compiler);
   this.fileManager = context.get(JavaFileManager.class);
  } catch (Throwable t) {
   throw ExceptionUtils.initFatal(t);
  }
  this.compilerOutput = new Vector<>();
  COMPILER_HOOKS.add(this);
  PalMain.onCompileStarted();
 }

 public void onCompileStarted(
  final List<JavaFileObject> inputFiles,
  final List<String> classNames,
  final Iterable<? extends Processor> processors) {
  //TODO remove debug
  System.out.println("COMPILE PROCESS STARTED");
  try {
   final Context context = new Context();
   final JavaCompiler disposableCompiler = JavaCompiler.instance(context);
   PalAgent.exclude(disposableCompiler, true, false);
   final ParserFactory parserFactory = (ParserFactory)JavaCompiler_parserFactory.get(disposableCompiler);
//   System.out.println("Parser Factory: " + parserFactory);
   final Vector<JCTree.JCCompilationUnit> compilationUnits = new Vector<>();
   Files
    //TODO move tests to separate module
    //TODO test resources are packaged in the test module artifact
    .walk(new File("/home/matthew/Git/pal/src/java/pal/res/tests").toPath())
    .filter(Files::isRegularFile)
    .forEach(file -> {
     try {
      final String code = new String(Files.readAllBytes(file.toAbsolutePath()));
      final JavacParser parser = parserFactory.newParser(code, true, true, true);
      compilationUnits.add(parser.parseCompilationUnit());
     } catch (Throwable t) {
      throw new RuntimeException("Encountered exception parsing test resources!", t);
     }
    });
   System.out.println("Compilation units to process: " + compilationUnits.size());
   long start = System.nanoTime();
   final LinkedHashMap<LatentCompilationUnit, LinkedList<JCTree.JCAnnotation>> postProcessing = new LinkedHashMap<>();
   compilationUnits.forEach(unit -> {
    System.out.println("Processing compilation unit: " + unit.hashCode());
//    try {
//     System.out.println(PalMain.PAL_CLASSLOADER.findClass(
//      "java.util.Map<java.util.List<java.lang.String>, java.util.Map<java.util.Map<java.lang.String, java.lang.Integer>, java.lang.Double>>"
//     ));
//    } catch (Throwable t) {
//     System.out.println();
//    }
    final Vector<JCTree.JCAnnotation> annotations = CompilerUtils.TreeTraversalFunction.aggregate(unit, JCTree.JCAnnotation.class);
    final RuntimeAnnotationGenerator.GeneratorContext gc = new RuntimeAnnotationGenerator.GeneratorContext(unit);
    annotations.forEach(ann -> {
     //Remove undefined annotations and retry after annotations have been compiled
     try {
      //If the type is resolvable, no exception will be thrown.
      gc.resolveType(ann);
     } catch (SymbolNotFoundException e) {
      //Clone JCCompilationUnit tree to preserve original state
      final JCTree.JCCompilationUnit original = (JCTree.JCCompilationUnit)unit.clone();
      //Remove the annotation from the compilation unit
      CompilerUtils.traverseTree(unit, CompilerUtils.TreeTraversalFunction.remove(ann.getClass()));
      final LatentCompilationUnit latentUnit = new LatentCompilationUnit(original, unit);
      LinkedList<JCTree.JCAnnotation> toProcess = postProcessing.get(unit);
      toProcess = toProcess == null ? new LinkedList<>() : toProcess;
      toProcess.add(ann);
      postProcessing.put(latentUnit, toProcess);
     }
    });
    //TODO strip pal annotations from compilation unit
//    annotations.forEach(ann -> PalMain.ANNOTATION_GENERATOR.generateAnnotation(unit, ann));
    System.out.println("Stripped " + annotations.size() + " annotations\n");
   });
   System.out.println("Annotation stripping took: " + (System.nanoTime()-start) + "ns");
  } catch (Throwable t) {
   throw ExceptionUtils.initFatal(t);
  }
  System.out.println();
 }

 //Define class in order of descending hierarchy (required to define inner classes)
 public void onCompilerClosed() {
  final LinkedList<Symbol.ClassSymbol> orderedHierarchy = new LinkedList<>();
  final LinkedHashMap<Symbol.ClassSymbol, byte[]> orderedDefinitions = new LinkedHashMap<>();
  int roundDepth = 0;
  while (!compilerOutput.isEmpty()) {
   final Vector<Symbol.ClassSymbol> toRemove = new Vector<>();
   for (final Symbol.ClassSymbol symbol : compilerOutput) {
    if (count(symbol.flatname.toString(), "$") == roundDepth) {
     toRemove.add(symbol);
    }
   }
   orderedHierarchy.addAll(toRemove);
   compilerOutput.removeAll(toRemove);
   roundDepth++;
  }
  for (final Symbol.ClassSymbol symbol : orderedHierarchy) {
   try {
    final FileObject outputFile = fileManager.getJavaFileForOutput(
     StandardLocation.CLASS_OUTPUT,
     symbol.flatname.toString(),
     JavaFileObject.Kind.CLASS,
     symbol.sourcefile
    );
    orderedDefinitions.put(
     symbol,
     Files.readAllBytes(new File(outputFile.toUri().getPath()).toPath())
    );
   } catch (IOException e) {
    ExceptionUtils.initFatal(e);
   }
  }
  //Define all classes
  final Class<?>[] compiledClasses = PalMain.PAL_CLASSLOADER.defineClasses(orderedDefinitions);
  //TODO remove debug statement
  if (compiledClasses.length > 0) {
   System.out.println("--------------------------------------------------");
   System.out.println("Defined classes from '" + compiler + "':");
   for (final Class<?> clazz : compiledClasses) {
    System.out.println(clazz);
   }
   System.out.println();
  }
  //Signal compilation finished
  PalMain.onCompileFinished();
 }

 public void onClassWrite(final ClassWriter writer, final Symbol.ClassSymbol symbol) {
  compilerOutput.add(symbol);
 }

 public JavaCompiler getCompiler() {
  return this.compiler;
 }

 public static CompilerHooks forInstance(final JavaCompiler compiler) {
  for (final CompilerHooks ch : COMPILER_HOOKS) {
   if (ch.compiler.equals(compiler)) {
    return ch;
   }
  }
  return null;
 }

 private static int count(final String src, final String txt) {
  return (src.length() - src.replace(txt, "").length())/txt.length();
 }
}
