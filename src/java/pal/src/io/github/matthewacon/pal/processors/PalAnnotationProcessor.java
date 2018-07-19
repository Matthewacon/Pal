package io.github.matthewacon.pal.processors;

import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import io.github.matthewacon.pal.*;
import io.github.matthewacon.pal.api.IPalProcessor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileManager;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

@SupportedAnnotationTypes("org.github.matthewacon.pal.meta.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class PalAnnotationProcessor extends AbstractProcessor {
 private static final Field
  JAVACTREES_javacTaskImpl,
  JAVACTASKIMPL_compilerMain,
  JAVACOMPILER_todo,
  JAVACELEMENTS_javaCompiler;

 static {
  final String tempDir = System.getProperty("java.io.tmpdir");
  final File nativeLib = new File(tempDir + "/libpal.so");
  nativeLib.deleteOnExit();
  //TODO multiplatform (use gradle string replacement)
  try(final InputStream nativeLibIS = PalAnnotationProcessor.class.getResourceAsStream("/libpal.so")) {
   if (!nativeLib.exists()) nativeLib.createNewFile();
   Files.copy(nativeLibIS, nativeLib.toPath(), StandardCopyOption.REPLACE_EXISTING);
   //Unload the native lib if it's already loaded (found that out the hard way, working with gradle...)
   if (NativeUtils.isLibraryLoaded(nativeLib.getAbsolutePath())) {
    NativeUtils.unloadLibrary(nativeLib.getAbsolutePath());
   }
   //Load pal native library
   System.load(nativeLib.getAbsolutePath());
   JAVACTREES_javacTaskImpl = JavacTrees.class.getDeclaredField("javacTaskImpl");
   JAVACTREES_javacTaskImpl.setAccessible(true);
   JAVACTASKIMPL_compilerMain = JavacTaskImpl.class.getDeclaredField("compilerMain");
   JAVACTASKIMPL_compilerMain.setAccessible(true);
   JAVACOMPILER_todo = JavaCompiler.class.getDeclaredField("todo");
   JAVACOMPILER_todo.setAccessible(true);
   JAVACELEMENTS_javaCompiler = JavacElements.class.getDeclaredField("javaCompiler");
   JAVACELEMENTS_javaCompiler.setAccessible(true);
  } catch (Throwable t) {
   if (nativeLib.exists()) nativeLib.delete();
   throw ExceptionUtils.initFatal(t);
  }
 }

 private JavacProcessingEnvironment pe;
 private JavaCompiler compiler;
 private Context context;
 private JavaFileManager javacFileManager;
// private Todo todo;
// private Filer filer;
 private Trees trees;
// private Main main;
// private ClassWriter classWriter;

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

 @Override
 public synchronized void init(ProcessingEnvironment pe) {
  try {
   this.pe = ((JavacProcessingEnvironment)pe);
   super.init(pe);
   this.context = this.pe.getContext();
   this.compiler = JavaCompiler.instance(context);
   this.javacFileManager = context.get(JavaFileManager.class);
//   this.todo = (Todo)JAVACOMPILER_todo.get(compiler);
//   this.filer = pe.getFiler();
   this.trees = Trees.instance(pe);
//   this.main = (Main)JAVACTASKIMPL_compilerMain.get(JAVACTREES_javacTaskImpl.get(trees));
//   this.classWriter = ClassWriter.instance(context);
//   new CompilerHooks(PalMain.PAL_CLASSLOADER, javacFileManager);
  } catch(Throwable t) {
   ExceptionUtils.initFatal(t);
  }
 }

 @Override
 public boolean process(Set<? extends TypeElement> set, RoundEnvironment re) {
  System.out.println("PAL ANNOTATION PROCESSOR INVOKED!");
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
  return true;
 }
}
