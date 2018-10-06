package example;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Dummy(
 value = {
  {
   @Target({ElementType.ANNOTATION_TYPE}),
   new Target() {
    @Override
    public ElementType[] value() {
     return new ElementType[0];
    }
   }
  },
  {
   new Target() {
    {
     System.out.println("Anonymous instance initializer!");
    }

    @Override
    public ElementType[] value() {
     return new ElementType[] {ElementType.ANNOTATION_TYPE};
    }
   },
   @Target({ElementType.CONSTRUCTOR})
  }
 },
 clazz = Class.class
)
public @interface Dummy {
 Target[][] value();
 Class<?> clazz();
}
