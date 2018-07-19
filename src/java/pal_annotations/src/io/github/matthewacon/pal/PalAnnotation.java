package io.github.matthewacon.pal;

import io.github.matthewacon.pal.api.IPalAnnotation;
import io.github.matthewacon.pal.api.IPalProcessor;
import io.github.matthewacon.pal.api.PalBytecodeProcessor;
import net.bytebuddy.dynamic.DynamicType;

import java.lang.annotation.*;
import java.util.HashMap;
import java.util.LinkedHashMap;

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
