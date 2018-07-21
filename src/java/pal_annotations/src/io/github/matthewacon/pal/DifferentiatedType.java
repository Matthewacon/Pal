package io.github.matthewacon.pal;

import io.github.matthewacon.pal.api.bytecode.PalAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@PalAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DifferentiatedType {
// Class<? extends IPalAnnotation>
}
