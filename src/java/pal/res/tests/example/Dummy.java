package example;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Dummy(
 value = {
  @Target({ElementType.ANNOTATION_TYPE}),
  new Target() {
   @Override
   public ElementType[] value() {
    return new ElementType[0];
   }
  }
 },
 clazz = Class.class
)
public @interface Dummy {
 Target[] value();
 Class<?> clazz();
}
