package io.github.matthewacon.pal.api;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;

public interface PalSourcecodeProcessor<T extends Annotation> extends IPalProcessor<T> {
 String process(final T annotation, final Element element, final String code);
}
