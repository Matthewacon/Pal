package io.github.matthewacon.pal.api.bytecode;

import java.lang.annotation.*;

/**Defines a Pal library annotation. Anything annotated with this will be subject to any processors defined in
 *
 */
@PalAnnotation
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PalAnnotation {
// Class<? extends IPalProcessor>[] processors() default {};
// Class<? extends IPalAnnotation>[] superClasses() default {};
 boolean runtimeAccessible() default false;

// final class PalAnnotationProcessor implements PalBytecodeProcessor {
//  @Override
//  public HashMap<Class<?>, DynamicType.Builder<?>> process(final LinkedHashMap<Class<?>, Annotation[]> map) {
//   return new HashMap<Class<?>, DynamicType.Builder<?>>() {
//    {
//     for (final Class<?> key : map.keySet()) {
//      //Some processing
//     }
//    }
//   };
//  }
// }
}
