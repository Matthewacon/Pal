package io.github.matthewacon.pal.javax.processors;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import io.github.matthewacon.pal.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;

public final class PalAnnotationProcessor extends AbstractProcessor {
 private static final Class<? extends JavacProcessingEnvironment> JavacProcessingEnvironment_Round;

 private static final Field
  JavacProcessingEnvironment_Round_topLevelClasses,
  JavacTrees_javacTaskImpl,
  JavacTaskImpl_compilerMain,
  JavaCompiler_todo,
  JavacElements_javaCompiler;

 static {
  try {
   JavacProcessingEnvironment_Round = (Class<? extends JavacProcessingEnvironment>)PalMain.PAL_CLASSLOADER.findClass(
     JavacProcessingEnvironment.class.getName() + "$Round"
   );
   JavacProcessingEnvironment_Round_topLevelClasses =
    JavacProcessingEnvironment_Round.getDeclaredField("topLevelClasses");
   JavacProcessingEnvironment_Round_topLevelClasses.setAccessible(true);
   JavacTrees_javacTaskImpl = JavacTrees.class.getDeclaredField("javacTaskImpl");
   JavacTrees_javacTaskImpl.setAccessible(true);
   JavacTaskImpl_compilerMain = JavacTaskImpl.class.getDeclaredField("compilerMain");
   JavacTaskImpl_compilerMain.setAccessible(true);
   JavaCompiler_todo = JavaCompiler.class.getDeclaredField("todo");
   JavaCompiler_todo.setAccessible(true);
   JavacElements_javaCompiler = JavacElements.class.getDeclaredField("javaCompiler");
   JavacElements_javaCompiler.setAccessible(true);
  } catch (Throwable t) {
   throw ExceptionUtils.initFatal(t);
  }
 }

 private JavacProcessingEnvironment pe;
// private JavaCompiler compiler;
 private Context context;
 private JavaFileManager javaFileManager;
 private Filer filer;
 private Trees trees;

// private final TreePathScanner<Object, CompilationUnitTree> scanner = new TreePathScanner<Object, CompilationUnitTree>() {
//  @Override
//  public Trees visitClass(final ClassTree ct, final CompilationUnitTree cut) {
//   if (cut instanceof JCCompilationUnit) {
//    final JCCompilationUnit compilationUnit = (JCCompilationUnit)cut;
//    System.out.println(compilationUnit.sourcefile.getKind());
//    System.out.println(compilationUnit);
////    try(final Reader reader = compilationUnit.getSourceFile().openReader(true)) {
//////     System.out.println(reader.getClass());
////    } catch(Throwable t) {
////     RuntimeException re = new RuntimeException();
////     re.initCause(t);
////     throw re;
////    }
////    try(Writer writer = compilationUnit.getSourceFile().openWriter()) {
////     writer.flush();
////    } catch (IOException e) {
////     e.printStackTrace();
////    }
////    if (compilationUnit.sourcefile.getKind() == Kind.CLASS) {}
//   }
//   return trees;
//  }
// };

// public PalAnnotationProcessor() {
//  super();
// }

 @Override
 public synchronized void init(ProcessingEnvironment pe) {
  try {
   this.pe = ((JavacProcessingEnvironment)pe);
   super.init(pe);
   this.context = this.pe.getContext();
//   this.compiler = JavaCompiler.instance(context);
   this.javaFileManager = context.get(JavaFileManager.class);
   this.filer = pe.getFiler();
   this.trees = Trees.instance(pe);
  } catch(Throwable t) {
   ExceptionUtils.initFatal(t);
  }
 }

 //TODO create temporary source file, copy contents into file, transform, compile, delete temporary file
 //TODO execute sourcecode processors
 @Override
 public boolean process(Set<? extends TypeElement> set, RoundEnvironment re) {
  try {
   final int roundStackDepth = NativeUtils.firstInstanceOfClassOnStack(JavacProcessingEnvironment_Round);
   final List<Symbol.ClassSymbol> topLevelClasses =
    (List<Symbol.ClassSymbol>)JavacProcessingEnvironment_Round_topLevelClasses.get(
     NativeUtils.getInstanceFromStack(roundStackDepth)
    );
   topLevelClasses.forEach(c -> System.out.println(c));
   //TODO remove debug
//  System.out.println("PAL ANNOTATION PROCESSOR INVOKED!");
   for (final Class<? extends Annotation> annotation : PalMain.getRegisteredAnnotations()) {
    for (final Element element : re.getElementsAnnotatedWith(annotation)) {
     final TreePath path = trees.getPath(element);
     final JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) path.getCompilationUnit();
     System.out.println("Element: " + element + " :: New code: ");
//    System.out.println(compilationUnit);
     final JavaFileObject originalFileObject = path.getCompilationUnit().getSourceFile();
     final String code;
     code = new String(Files.readAllBytes(new File(originalFileObject.toUri()).toPath()));
     Element encapsulator = element;
     do {
      if (!(encapsulator instanceof TypeElement)) {
       encapsulator = encapsulator.getEnclosingElement();
      }
     } while (!(encapsulator.getKind().isClass() || encapsulator.getKind().isInterface()));
     final JavaFileObject newFileObject;
     newFileObject = javaFileManager.getJavaFileForOutput(
      StandardLocation.SOURCE_OUTPUT,
      ((TypeElement) encapsulator).getQualifiedName().toString(),
      JavaFileObject.Kind.SOURCE,
      null
     );
     final OutputStream os = newFileObject.openOutputStream();
     os.write(new String().getBytes());
     os.close();
//     try (final OutputStream os = newFileObject.openOutputStream()) {
//      os.write(code.getBytes());
//     }
     final String newFile = new String(Files.readAllBytes(new File(newFileObject.toUri()).toPath()));
     System.out.println(newFile);
     System.out.println(newFileObject);
     compilationUnit.sourcefile = newFileObject;
     ((Symbol.ClassSymbol) element).sourcefile = newFileObject;
//    filer.createSourceFile()
//    try(final Writer writer = jfo.openWriter()) {
////     final CharBuffer cb = CharBuffer.wrap(new StringBuffer());
//     writer.write("");
//     writer.flush();
////     System.out.println(cb.toString() + "\n");
//    } catch (IOException e) {
//     throw ExceptionUtils.initFatal(e);
//    }
     System.out.println("Transformed element: ");
     System.out.println();
    }
   }
//  for (MetaAnnotation ma : MetaAnnotation.values()) {
//   for (Element te : re.getElementsAnnotatedWith(ma.metaAnnotation)) {
//    final PackageElement packageElement = (PackageElement)te.getEnclosingElement();
//    String pack = packageElement.getQualifiedName().toString();
//    pack = pack.isEmpty() ? "" : pack + ".";
////    try {
////     final TreePath path = trees.getPath(te);
////     scanner.scan(path, path.getCompilationUnit());
//////     JavaFileObject jfo = filer.createClassFile(pack + te.getSimpleName());
////////     Reader reader = jfo.openReader(true);
//////     Writer writer = jfo.openWriter();
////////     writer.write();
//////     writer.flush();
//////     writer.close();
////////     CharBuffer cb = CharBuffer.wrap(new StringBuffer());
////////     reader.read(cb);
////////     System.out.println(cb.toString());
////    } catch (Throwable t) {
////     throw ExceptionUtils.initFatal(t);
////    }
//   }
//  }
////  for (TypeElement element : set) {
////   System.out.println("Annotated with: " + element);
////   System.out.println("Element info: " + element.getClass());
////   re.getElementsAnnotatedWith(element).forEach(e -> System.out.println("\t" + e));
////   System.out.println();
////  }
  } catch (Throwable t) {
   throw ExceptionUtils.initFatal(t);
  }
  return true;
 }

 @Override
 public Set<String> getSupportedAnnotationTypes() {
//  System.out.println("PalAnnotationProcessor getSupportedAnnotationTypes");
  final HashSet<String> supportedAnnotations = new HashSet<>();
  PalMain
   .getRegisteredAnnotations()
   .forEach(clazz -> {
    supportedAnnotations.add(clazz.getName());
    //TODO remove debug statement
//    System.out.println(clazz);
   });
  System.out.println();
  return supportedAnnotations;
 }

 @Override
 public SourceVersion getSupportedSourceVersion() {
  return SourceVersion.latestSupported();
 }
}
