package io.github.matthewacon.pal.javax.processors;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.file.BaseFileObject;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.BaseFileManager;
import com.sun.tools.javac.util.Context;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.util.*;

import io.github.matthewacon.pal.*;
import io.github.matthewacon.pal.api.IPalProcessor;
import io.github.matthewacon.pal.api.PalSourcecodeProcessor;

public final class PalAnnotationProcessor extends AbstractProcessor {
 //TODO clean all reflection related code and implement a security manager
 private static final Class<? extends JavacProcessingEnvironment> JavacProcessingEnvironment_Round;
 private static final Class<?> BaseFileManager_ContentCacheEntry;

 private static final Constructor<?>
  BaseFileManager_ContentCacheEntry_constructor,
  javac_util_List;

 private static final Field
  JavacProcessingEnvironment_Round_roots,
  BaseFileManager_contentCache,
  JavaCompiler_parserFactory;

 static {
  try {
   JavacProcessingEnvironment_Round = (Class<? extends JavacProcessingEnvironment>)PalMain.PAL_CLASSLOADER.findClass(
     JavacProcessingEnvironment.class.getName() + "$Round"
   );
   BaseFileManager_ContentCacheEntry = PalMain.PAL_CLASSLOADER.findClass(
    BaseFileManager.class.getName() + "$ContentCacheEntry"
   );
   BaseFileManager_ContentCacheEntry_constructor = BaseFileManager_ContentCacheEntry.getDeclaredConstructors()[0];
   BaseFileManager_ContentCacheEntry_constructor.setAccessible(true);
   javac_util_List = com.sun.tools.javac.util.List.class.getDeclaredConstructors()[0];
   javac_util_List.setAccessible(true);
   JavacProcessingEnvironment_Round_roots = JavacProcessingEnvironment_Round.getDeclaredField("roots");
   JavacProcessingEnvironment_Round_roots.setAccessible(true);
   BaseFileManager_contentCache = BaseFileManager.class.getDeclaredField("contentCache");
   BaseFileManager_contentCache.setAccessible(true);
   JavaCompiler_parserFactory = JavaCompiler.class.getDeclaredField("parserFactory");
   JavaCompiler_parserFactory.setAccessible(true);
  } catch (Throwable t) {
   throw ExceptionUtils.initFatal(t);
  }
 }

 private JavacProcessingEnvironment pe;
 private Context context;
 private JavacFileManager javaFileManager;
 private Trees trees;

 @Override
 public synchronized void init(ProcessingEnvironment pe) {
  try {
   this.pe = ((JavacProcessingEnvironment)pe);
   super.init(pe);
   this.context = this.pe.getContext();
   this.javaFileManager = (JavacFileManager)context.get(JavaFileManager.class);
   this.trees = Trees.instance(pe);
  } catch(Throwable t) {
   ExceptionUtils.initFatal(t);
  }
 }

 @Override
 public boolean process(Set<? extends TypeElement> set, RoundEnvironment re) {
  try {
   final Map<JavaFileObject, Object> contentCache = new HashMap<>();
   contentCache.putAll((Map<JavaFileObject, Object>)BaseFileManager_contentCache.get(javaFileManager));
   final int
    compilerStackDepth = NativeUtils.firstDepthOfClassOnStack(JavaCompiler.class),
    roundStackDepth = NativeUtils.firstDepthOfClassOnStack(JavacProcessingEnvironment_Round);
   final JavaCompiler compiler = NativeUtils.getInstanceFromStack(compilerStackDepth);
   final ParserFactory parserFactory = (ParserFactory)JavaCompiler_parserFactory.get(compiler);
   final Object round = NativeUtils.getInstanceFromStack(roundStackDepth);
   final List<JCTree.JCCompilationUnit> roots = new LinkedList<>();
   roots.addAll((List<JCTree.JCCompilationUnit>)JavacProcessingEnvironment_Round_roots.get(round));
//   final String someCode = "@Literal(target=\"PFC\", replacement=\"public final class\")\nPFC AnotherTest {}";
//   final JavacParser firstParser = parserFactory.newParser(someCode, true, true, true);
//   JCTree.JCCompilationUnit someUnit = firstParser.parseCompilationUnit();
   //TODO remove debug
//  System.out.println("PAL ANNOTATION PROCESSOR INVOKED!");
   for (final Class<? extends Annotation> annotation : PalMain.getRegisteredAnnotations()) {
    for (final Element element : re.getElementsAnnotatedWith(annotation)) {
     final TreePath path = trees.getPath(element);
     //TODO remove debug
//     System.out.println("Element: " + element);
     //Get original code
     final JavaFileObject originalFileObject = path.getCompilationUnit().getSourceFile();
     String code = new String(Files.readAllBytes(new File(originalFileObject.toUri()).toPath()));
     //Continue until the foremost class for the element is reached
     Element encapsulator = element;
     do {
      if (!(encapsulator instanceof TypeElement)) {
       encapsulator = encapsulator.getEnclosingElement();
      }
     } while (!(encapsulator.getKind().isClass() || encapsulator.getKind().isInterface()));
     //Create new source file (does not interfere with the original sources)
     final JavaFileObject newFileObject = javaFileManager.getJavaFileForOutput(
      StandardLocation.SOURCE_OUTPUT,
      ((TypeElement)encapsulator).getQualifiedName().toString(),
      JavaFileObject.Kind.SOURCE,
      null
     );
     //TODO Run source transformations
     //TODO Processor ordering and parallel execution
     final Set<? super IPalProcessor<?>> processors = PalMain
      .getRegisteredAnnotationProcessors()
      .get(annotation);
     if (processors != null) {
      for (final Object processor : processors) {
       if (processor instanceof PalSourcecodeProcessor) {
        code = ((PalSourcecodeProcessor)processor).process(null, element, code);
       }
      }
     }
     //Write new code to source output
     try (final OutputStream os = newFileObject.openOutputStream()) {
      os.write(code.getBytes());
     }
     //Parse new file
     final JavacParser parser = parserFactory.newParser(code, true, true, true);
     final JCTree.JCCompilationUnit newCompilationUnit = parser.parseCompilationUnit();
     //Set sourcefile for compilation unit
     newCompilationUnit.sourcefile = newFileObject;
     //Update compilation units
     JCTree.JCCompilationUnit oldUnit = null;
     for (final JCTree.JCCompilationUnit compilationUnit : roots) {
      if (sameName(compilationUnit.sourcefile, newFileObject)) {
       oldUnit = compilationUnit;
       break;
      }
     }
     if (oldUnit == null) {
      throw new ConcurrentModificationException(
       "Source file '" +
       newFileObject.getName() +
       "' compilation unit task disappeared during source annotation processing!"
      );
     } else {
      final int index = roots.indexOf(oldUnit);
      roots.remove(oldUnit);
      roots.add(index, newCompilationUnit);
     }
     //Convert LinkedList<JCCompilationUnit> to com.sun.tools.javac.util.List<JCCompilationUnit>
     com.sun.tools.javac.util.List<JCTree.JCCompilationUnit> newRoots =
      (com.sun.tools.javac.util.List<JCTree.JCCompilationUnit>)javac_util_List.newInstance(
      null,
      null
     );
     for (int index = roots.size()-1; index > -1; index--) {
      newRoots = (com.sun.tools.javac.util.List<JCTree.JCCompilationUnit>)javac_util_List.newInstance(
        roots.get(index),
        newRoots
       );
     }
     //Set the new source rounds
     JavacProcessingEnvironment_Round_roots.set(round, newRoots);
     //Update symbol
     ((Symbol.ClassSymbol)element).sourcefile = newFileObject;
     //Update file manager contentCache
     JavaFileObject entryToRemove = null;
     for (final JavaFileObject keyFile : contentCache.keySet()) {
      if (sameName(keyFile, newFileObject)) {
       entryToRemove = keyFile;
       break;
      }
     }
     if (entryToRemove == null) {
      throw new ConcurrentModificationException(
       "Source file '" +
       newFileObject.getName() +
       "' disappeared during source annotation processing!"
      );
     } else {
      contentCache.remove(entryToRemove);
     }
     contentCache.put(
      newFileObject,
      BaseFileManager_ContentCacheEntry_constructor.newInstance(
       newFileObject,
       CharBuffer.wrap(code)
      )
     );
    }
   }
   final Map contentCacheMap = (Map)BaseFileManager_contentCache.get(javaFileManager);
   contentCacheMap.clear();
   contentCacheMap.putAll(contentCache);
  } catch (Throwable t) {
   throw ExceptionUtils.initFatal(t);
  }
  return true;
 }

 @Override
 public Set<String> getSupportedAnnotationTypes() {
  final HashSet<String> supportedAnnotations = new HashSet<>();
  PalMain
   .getRegisteredAnnotations()
   .forEach(clazz -> supportedAnnotations.add(clazz.getName()));
  return supportedAnnotations;
 }

 @Override
 public SourceVersion getSupportedSourceVersion() {
  return SourceVersion.latestSupported();
 }

 private static boolean sameName(final FileObject f1, final FileObject f2) {
  if (f1 instanceof BaseFileObject && f2 instanceof BaseFileObject) {
   return ((BaseFileObject)f1).getShortName().equals(((BaseFileObject)f2).getShortName());
  }
  return false;
 }
}
