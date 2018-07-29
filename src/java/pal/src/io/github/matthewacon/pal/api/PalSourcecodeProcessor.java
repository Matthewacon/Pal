package io.github.matthewacon.pal.api;

import java.lang.annotation.Annotation;
import java.util.function.BiFunction;

import javax.lang.model.element.Element;

public abstract class PalSourcecodeProcessor<T extends Annotation> implements IPalProcessor<T> {
 private final BiFunction<T, String, String>[] scrubbers;

 public PalSourcecodeProcessor(final BiFunction<T, String, String>... scrubbers) {
  this.scrubbers = scrubbers;
 }

 public abstract String process(final T annotation, final Element element, final String code);

 //TODO predefined scrubber to remove annotations from source code
 public static <A extends Annotation> String scrubAnnotation(final A annotation, final String code) {
  return null;
 }
}
