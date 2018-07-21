package io.github.matthewacon.pal;

import io.github.matthewacon.pal.api.bytecode.PalAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**WILL BE REPLACED WITH {@link Modifiers}*/
@PalAnnotation
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Interface {}
