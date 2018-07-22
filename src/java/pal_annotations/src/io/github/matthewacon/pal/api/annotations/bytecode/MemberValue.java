package io.github.matthewacon.pal.api.annotations.bytecode;

import java.lang.annotation.*;
import java.lang.reflect.Modifier;

@PalAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@Repeatable(MemberValue.MemberValues.class)
public @interface MemberValue {
 Class<?> valueType();
 String valueName() default "value";
 int modifiers() default Modifier.PUBLIC;

 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.ANNOTATION_TYPE)
 @interface MemberValues {
  MemberValue[] value();
 }
}
