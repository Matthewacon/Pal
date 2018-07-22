package io.github.matthewacon.pal.api.annotations.bytecode;

import io.github.matthewacon.pal.api.IPalAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@PalAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DifferentiatedType {
// IPalAnnotation[] value();
}
