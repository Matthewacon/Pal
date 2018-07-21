package io.github.matthewacon.pal;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.jvm.ClassWriter;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Vector;

public final class CompilerHooks {
 private static final Field JavaCompiler_context;
 private static final LinkedHashSet<CompilerHooks> COMPILER_HOOKS;

 static {
  try {
   JavaCompiler_context = JavaCompiler.class.getDeclaredField("context");
   JavaCompiler_context.setAccessible(true);
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
 }

 //Define class in order of descending hierarchy (required to define inner classes)
 public void onCompilerClosed() {
//  openCompilers -= 1;
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
  final Class<?>[] definedClasses = PalMain.PAL_CLASSLOADER.defineClasses(orderedDefinitions);
  PalMain.onCompileFinished(definedClasses);
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
