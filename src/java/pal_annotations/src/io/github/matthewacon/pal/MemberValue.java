package io.github.matthewacon.pal;

import java.lang.annotation.*;
import java.lang.reflect.Modifier;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@Repeatable(MemberValue.MemberValues.class)
public @interface MemberValue {
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.ANNOTATION_TYPE)
 @interface MemberValues {
  MemberValue[] value();
 }

 Class<?> valueType();
 String valueName() default "value";
 int modifiers() default Modifier.PUBLIC;
}
