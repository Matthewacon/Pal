package io.github.matthewacon.pal.api.annotations.bytecode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**Will be replaced by {@link MemberValue}. (Ambiguously names, clashes with {@link java.lang.annotation.Annotation}*/
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Annotation {}
