package io.github.matthewacon.pal;

import io.github.matthewacon.pal.api.PalProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PalAnnotation {
 Class<? extends PalProcessor>[] processors() default {};
}
