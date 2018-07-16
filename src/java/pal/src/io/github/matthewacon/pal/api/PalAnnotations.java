package io.github.matthewacon.pal.api;

import io.github.matthewacon.pal.ExceptionUtils;
import io.github.matthewacon.pal.PalAnnotationProcessor;
import io.github.matthewacon.pal.PalClassLoader;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

//TODO Reconfigure gradle modules such that the pal_annotations sources are accessible from the pal module
public enum PalAnnotations implements PalProcessor {
 //TODO FINISH
 PAL_TARGET(forName("io.github.matthewacon.pal.PalTarget")) {
  @Override
  public PalProcessor[] getProcessors() {
   return new PalBytecodeProcessor[] {
    (final LinkedHashMap<Class<?>, Annotation[]> map) -> new HashMap<Class<?>, DynamicType.Builder<?>>() {
     {
      for (final Class<?> key : map.keySet()) {
       for (final Annotation annotation : map.get(key)) {
//        if (annotation instanceof PalTarget) {
        if (PAL_TARGET.supportedAnnotations[0].getClass().isAssignableFrom(annotation.getClass())) {

         //TODO FINISH PALCLASSLOADER
         final DynamicType.Builder transformedClass = new ByteBuddy()
          .rebase(key)
          .visit(Advice.to(PalClassLoader.class).on(ElementMatchers.isTypeInitializer()));
         put(key, transformedClass);
        }
       }
      }
     }
    }
   };
  }
 },
 PAL_ANNOTATION(forName("io.github.matthewacon.pal.PalAnnotation")) {
  @Override
  public PalProcessor[] getProcessors() {
   return new PalBytecodeProcessor[] {
    (final LinkedHashMap<Class<?>, Annotation[]> map) -> new HashMap<Class<?>, DynamicType.Builder<?>>() {
     {
//      for () {
//
//      }
     }
    }
   };
  }
 },
 @Deprecated
 ENUM(forName("io.github.matthewacon.pal.Enum")) {
  @Override
  public PalProcessor[] getProcessors() {
   return new PalBytecodeProcessor[] {
    (final LinkedHashMap<Class<?>, Annotation[]> map) -> new HashMap<Class<?>, DynamicType.Builder<?>>() {
     {
      for (final Class<?> key : map.keySet()) {
       for (final Annotation annotation : map.get(key)) {
//        if (annotation instanceof Enum) {
        if (ENUM.supportedAnnotations[0].getClass().isAssignableFrom(annotation.getClass())) {
         final DynamicType.Builder transformedClass = new ByteBuddy()
          .redefine(key)
          .modifiers(key.getModifiers() | Opcodes.ACC_ENUM);
         put(key, transformedClass);
        }
       }
      }
     }
    }
   };
  }
 },
 @Deprecated
 INTERFACE(forName("io.github.matthewacon.pal.Interface")) {
  @Override
  public PalProcessor[] getProcessors() {
   return new PalBytecodeProcessor[] {
    (final LinkedHashMap<Class<?>, Annotation[]> map) -> new HashMap<Class<?>, DynamicType.Builder<?>>() {
     {
      for (final Class<?> key : map.keySet()) {
       for (final Annotation annotation : map.get(key)) {
        //if (annotation instanceof Interface) {
        if (INTERFACE.supportedAnnotations[0].getClass().isAssignableFrom(annotation.getClass())) {
         final DynamicType.Builder transformedClass = new ByteBuddy()
          .redefine(key)
          .modifiers(key.getModifiers() | Modifier.INTERFACE);
         put(key, transformedClass);
        }
       }
      }
     }
    }
   };
  }
 },
 //TODO Implement
// MODIFIERS(forName("io.github.matthewacon.pal.Modifiers")) {
//
// },
 MEMBER_VALUE(forName("io.github.matthewacon.pal.MemberValue")) {
  @Override
  public PalProcessor[] getProcessors() {
   return new PalBytecodeProcessor[] {
    (final LinkedHashMap<Class<?>, Annotation[]> map) -> new HashMap<Class<?>, DynamicType.Builder<?>>() {
     {
//      for (final Class<?> key : map.keySet()) {
//       for (final Annotation annotation : ) {
//        if () {
//         final DynamicType.Builder transformedClass = new ByteBuddy()
//          .redefine(key)
////        .defineMethod()
//          ;
//         put(key, transformedClass);
//        }
//       }
//      }
     }
    }
   };
  }
 }
 ;

 private final Class<? extends Annotation>[] supportedAnnotations;

 PalAnnotations(final Class<? extends Annotation>... annotations) {
  supportedAnnotations = annotations;
 }

 public abstract PalProcessor[] getProcessors();

 public Class<? extends Annotation>[] getSupportedAnnotations() {
  return this.supportedAnnotations;
 }

 public static PalAnnotations[] getAnnotationsSupporting(final Class<? extends Annotation>... annotations) {
  final Vector<PalAnnotations> supporting = new Vector<>();
  for (final Class<? extends Annotation> annotation : annotations) {
   for (final PalAnnotations supported : getAnnotationsSupporting(annotation.getName())) {
    supporting.add(supported);
   }
  }
//  for (final Class<? extends Annotation> annotation : annotations) {
//   ValueLoop: for (final PalAnnotations palAnnotation : values()) {
//    for (final Class<? extends Annotation> supportedAnnotation : palAnnotation.supportedAnnotations) {
//     if (annotation.getName().equals(supportedAnnotation.getName())) {
//      supporting.add(palAnnotation);
//      continue ValueLoop;
//     }
//    }
//   }
//  }
  return supporting.toArray(new PalAnnotations[0]);
 }

 public static PalAnnotations[] getAnnotationsSupporting(final String... annotations) {
  final Vector<PalAnnotations> supporting = new Vector<>();
  for (final String annotation : annotations) {
   ValueLoop: for (final PalAnnotations palAnnotation : values()) {
    for (final Class<? extends Annotation> supportedAnnotation : palAnnotation.supportedAnnotations) {
     if (annotation.equals(supportedAnnotation.getName())) {
      supporting.add(palAnnotation);
      continue ValueLoop;
     }
    }
   }
  }
  return supporting.toArray(new PalAnnotations[0]);
 }

 private static Class<? extends Annotation> forName(final String name) {
  Class<?> clazz;
  try {
   clazz = Class.forName(name);
  } catch (ClassNotFoundException e) {
   try {
    clazz = Class.forName(name, true, PalAnnotationProcessor.dcl);
   } catch (ClassNotFoundException e1) {
    throw ExceptionUtils.initFatal(e1);
   }
  }
  if (Annotation.class.isAssignableFrom(clazz)) {
   return (Class<? extends Annotation>)clazz;
  } else {
   throw new RuntimeException("Class '" + clazz + "' is not an annotation!");
  }
 }

}
