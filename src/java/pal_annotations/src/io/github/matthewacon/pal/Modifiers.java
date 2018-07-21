package io.github.matthewacon.pal;

import io.github.matthewacon.pal.api.bytecode.PalAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@PalAnnotation
@Target({
 ElementType.ANNOTATION_TYPE,
 ElementType.TYPE,
 ElementType.CONSTRUCTOR,
 ElementType.METHOD,
 ElementType.FIELD,
 ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Modifiers {
 int modifiers();
}
