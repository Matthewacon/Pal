package io.github.matthewacon.pal.api.annotations.bytecode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**Can be applied to abstract bodies to transform them into bytecode compatible enumerations.
 * WILL BE REPLACED WITH {@link Modifiers}
 */
@PalAnnotation
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Enum {}
