package io.github.matthewacon.pal;

import io.github.matthewacon.pal.api.IPalAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DifferentiatedType {
// Class<? extends IPalAnnotation>
}
