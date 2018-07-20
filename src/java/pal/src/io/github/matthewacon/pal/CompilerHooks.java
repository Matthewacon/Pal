package io.github.matthewacon.pal;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.jvm.ClassWriter;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Vector;

public final class CompilerHooks {
 public static CompilerHooks INSTANCE;

 private final DisposableClassLoader dcl;
 private final JavaFileManager fileManager;
 private final Vector<Symbol.ClassSymbol> compilerOutput;

 private int openCompilers = 0;

 public CompilerHooks(final DisposableClassLoader dcl, final JavaFileManager fileManager) {
  if (INSTANCE == null) {
   INSTANCE = this;
  } else {
   throw new RuntimeException("The CompilerHooks class can only be instantiated once!");
  }
  this.dcl = dcl;
  this.fileManager = fileManager;
  this.compilerOutput = new Vector<>();
 }

 public void onCompilerConstructed() {
  openCompilers += 1;
 }

 //Define class in order of descending hierarchy (required to define inner classes)
 public void onCompilerClosed() {
  openCompilers -= 1;
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
  final Class<?>[] definedClasses = dcl.defineClasses(orderedDefinitions);
  //TODO remove debug statement
  for (final Class<?> clazz : definedClasses) {
   System.out.println("Defined: " + clazz);
  }
  if (openCompilers == 0) {
   PalMain.onCompileFinished(definedClasses);
  }
 }

 public void onClassWrite(final ClassWriter writer, final Symbol.ClassSymbol symbol) {
  compilerOutput.add(symbol);
 }

 private static int count(final String src, final String txt) {
  return (src.length() - src.replace(txt, "").length())/txt.length();
 }
}
