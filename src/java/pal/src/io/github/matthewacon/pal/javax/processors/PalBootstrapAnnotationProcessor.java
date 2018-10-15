package io.github.matthewacon.pal.javax.processors;

import com.sun.tools.javac.code.Symbol;
import io.github.matthewacon.pal.PalMain;
import io.github.matthewacon.pal.api.annotations.bytecode.PalAnnotation;
import io.github.matthewacon.pal.api.annotations.bytecode.PalProcessor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import java.util.HashSet;
import java.util.Set;

/**Bootstrapping processor for the {@link PalProcessor} and {@link PalAnnotation} annotations*/
public final class PalBootstrapAnnotationProcessor extends AbstractProcessor {
// private final HashSet<String> detectedProcessors;
//
// public PalBootstrapAnnotationProcessor() {
//  this.detectedProcessors = new HashSet<>();
// }

 @Override
 public void init(final ProcessingEnvironment pe) {
  super.init(pe);
 }

 @Override
 public boolean process(final Set<? extends TypeElement> set, final RoundEnvironment re) {
  if (!re.processingOver()) {
   //Detect source Pal processors
   for (final Element te : re.getElementsAnnotatedWith(PalProcessor.class)) {
    if (te instanceof Symbol.ClassSymbol) {
     PalMain.registerProcessor(((Symbol.ClassSymbol)te).flatname.toString());
    }
   }
   //Detect source Pal annotations
   for (final Element te : re.getElementsAnnotatedWith(PalAnnotation.class)) {
    if (te instanceof Symbol.ClassSymbol) {
     PalMain.registerAnnotation(((Symbol.ClassSymbol)te).flatname.toString());
    }
   }
  }
  return true;
 }

 @Override
 public Set<String> getSupportedAnnotationTypes() {
  return new HashSet<String>() {{
   add(PalProcessor.class.getName());
   add(PalAnnotation.class.getName());
  }};
 }

 @Override
 public SourceVersion getSupportedSourceVersion() {
  return SourceVersion.latestSupported();
 }
}
