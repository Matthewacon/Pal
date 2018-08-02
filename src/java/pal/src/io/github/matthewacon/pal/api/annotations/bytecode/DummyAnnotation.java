package io.github.matthewacon.pal.api.annotations.bytecode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({
 ElementType.ANNOTATION_TYPE,
 ElementType.CONSTRUCTOR,
 ElementType.FIELD,
 ElementType.LOCAL_VARIABLE,
 ElementType.METHOD,
 ElementType.PACKAGE,
 ElementType.PARAMETER,
 ElementType.TYPE,
 ElementType.TYPE_PARAMETER,
 ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface DummyAnnotation {
 String value() default "";
}
