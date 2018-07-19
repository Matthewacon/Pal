package io.github.matthewacon.pal.processors;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import io.github.matthewacon.pal.PalMain;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileManager;

import java.util.HashSet;
import java.util.Set;

import com.sun.tools.javac.util.Context;

/**Bootstrapping processor for the {@link PalProcessor} annotation*/
public final class PalBootstrapAnnotationProcessor extends AbstractProcessor {
 private final HashSet<String> detectedProcessors;

 public PalBootstrapAnnotationProcessor() {
  this.detectedProcessors = new HashSet<>();
 }

 @Override
 public void init(final ProcessingEnvironment pe) {
  super.init(pe);
  final Context context = ((JavacProcessingEnvironment)pe).getContext();
  PalMain.registerJavaFileManager(context.get(JavaFileManager.class));
 }

 @Override
 public boolean process(final Set<? extends TypeElement> set, final RoundEnvironment re) {
  if (!re.processingOver()) {
   //The set is gaurenteed to only contain 1 element, since this implementation of AbstractProcessor only supports the
   //io.github.matthewacon.pal.bytecode.PalProcessor annotation.
   final Set<? extends Element> annotated = re.getElementsAnnotatedWith(set.iterator().next());
   System.out.println(annotated.size());
   for (final Element te : annotated) {
    if (te instanceof Symbol.ClassSymbol) {
//     ((Symbol.ClassSymbol)te).annotation
     PalMain.registerProcessor(((Symbol.ClassSymbol)te).flatname.toString());
    }
   }
  }
  return true;
 }

 @Override
 public Set<String> getSupportedAnnotationTypes() {
  return new HashSet<String>() {
   {
    add("io.github.matthewacon.pal.bytecode.PalProcessor");
   }
  };
 }

 @Override
 public SourceVersion getSupportedSourceVersion() {
  return SourceVersion.latestSupported();
 }
}
