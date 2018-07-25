package io.github.matthewacon.pal;

import io.github.matthewacon.pal.api.IPalProcessor;
import io.github.matthewacon.pal.api.PalBytecodeProcessor;
import io.github.matthewacon.pal.api.PalSourcecodeProcessor;
import io.github.matthewacon.pal.api.annotations.bytecode.PalProcessor;
import io.github.matthewacon.pal.javax.processors.PalAnnotationProcessor;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import org.cliffc.high_scale_lib.NonBlockingHashSet;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public final class PalMain {
 public static final DisposableClassLoader PAL_CLASSLOADER;
 public static final File TEMP_DIR;

 private static final HashSet<Class<? extends Annotation>> REGISTERED_ANNOTATIONS;
 private static final NonBlockingHashMap<Class<? extends Annotation>, NonBlockingHashSet<? super IPalProcessor<?>>>
  REGISTERED_PROCESSORS;

 private static LinkedHashSet<String> REGISTERED_ANNOTATION_PATHS;
 private static LinkedHashSet<String> REGISTERED_PROCESSOR_PATHS;
 private static int openCompilers = 0;

 static {
  //Load Pal native library
  final String tempDir = System.getProperty("java.io.tmpdir");
  final File nativeLib = new File(tempDir + "/libpal.so");
  nativeLib.deleteOnExit();
  //TODO multiplatform (use gradle string replacement)
  try(final InputStream nativeLibIS = PalAnnotationProcessor.class.getResourceAsStream("/libpal.so")) {
   if (!nativeLib.exists()) nativeLib.createNewFile();
   Files.copy(nativeLibIS, nativeLib.toPath(), StandardCopyOption.REPLACE_EXISTING);
   //Unload the native lib if it's already loaded (found that out the hard way, working with gradle...)
   if (NativeUtils.isLibraryLoaded(nativeLib.getAbsolutePath())) {
    System.out.println("UNLOADED LIBRARY!");
    NativeUtils.unloadLibrary(nativeLib.getAbsolutePath());
   }
   //Load pal native library
   System.load(nativeLib.getAbsolutePath());
  } catch (IOException e) {
   if (nativeLib.exists()) nativeLib.delete();
   throw ExceptionUtils.initFatal(e);
  }

  //Initialize constants
  PAL_CLASSLOADER = new DisposableClassLoader(PalAgent.getInstrumentation());
  TEMP_DIR = new File(System.getProperty("java.io.tmpdir") + "/palResources");
  TEMP_DIR.deleteOnExit();
  REGISTERED_ANNOTATIONS = new HashSet<>();
  REGISTERED_PROCESSORS = new NonBlockingHashMap<>();

  REGISTERED_ANNOTATION_PATHS = new LinkedHashSet<>();
  REGISTERED_PROCESSOR_PATHS = new LinkedHashSet<>();

  //Detect Pal annotations and annotation processors on the classpath
  try {
   final Enumeration<URL> manifests = PAL_CLASSLOADER.getResources("META-INF/MANIFEST.MF");
   while (manifests.hasMoreElements()) {
    final URL manifestURL = manifests.nextElement();
    final Manifest manifest = new Manifest(manifestURL.openStream());
    manifest.getMainAttributes().forEach((final Object key, final Object value) -> {
     if (key instanceof Attributes.Name && value instanceof String) {
      final String attributeName = key.toString();
      for (final String pckage : ((String)value).split(":")) {
       if (!pckage.isEmpty() && (attributeName.equals("Pal-Processors") || attributeName.equals("Pal-Annotations"))) {
        for (final Class<?> clazz : PAL_CLASSLOADER.getClassesInPackage(pckage)) {
         switch (attributeName) {
          case "Pal-Processors": {
           registerProcessor(clazz);
           break;
          }
          case "Pal-Annotations": {
           registerAnnotation(clazz);
           break;
          }
         }
        }
       }
      }
     }
    });
   }
  } catch (Throwable t) {
   throw new RuntimeException("Error loading Pal annotation processors!", t);
  }
 }

 //Not instantiable. All class functions and properties are static
 private PalMain() {}

 public static Set<Class<? extends Annotation>> getRegisteredAnnotations() {
  return REGISTERED_ANNOTATIONS;
 }

 public static Map<Class<? extends Annotation>, NonBlockingHashSet<? super IPalProcessor<?>>> getRegisteredAnnotationProcessors() {
  return REGISTERED_PROCESSORS;
 }

 protected static void onCompileStarted() {
  openCompilers++;
 }

 protected static synchronized void onCompileFinished() {
  openCompilers--;
  if (openCompilers == 0) {
   loadAllAnnotations();
   loadAllProcessors();
   //TODO conditional debug
   System.out.println("Discovered Pal Processors: ");
   REGISTERED_PROCESSORS.forEach((annotation, processor) ->
    System.out.println(annotation + " :: " + processor.getClass())
   );
   System.out.println("\nDiscovered Pal Annotations: ");
   REGISTERED_ANNOTATIONS.forEach(annotation -> System.out.println(annotation));
   //TODO execute bytecode processors (support repeatable annotations)
//           if (clazz.isInstance(PalBytecodeProcessor.class)) {
//            processorRegistered = true;
//            final PalBytecodeProcessor<?> processor;
//            try {
//             processor = (PalBytecodeProcessor<?>) clazz.newInstance();
//            } catch (InstantiationException | IllegalAccessException e) {
//             throw ExceptionUtils.initFatal(e);
//            }
//            REGISTERED_PROCESSORS.put(targetAnnotation, processor);
//           }
//           if (clazz.isInstance(PalSourcecodeProcessor.class)) {
//            processorRegistered = true;
//           }
//           if (!processorRegistered) {
//            throw ExceptionUtils.initFatal(
//             new InvalidClassException(
//              "Pal processors cannot directly implement " +
//               IPalProcessor.class.getName() +
//               ". " +
//               clazz.getName() +
//               " must either implement " +
//               PalBytecodeProcessor.class.getName() +
//               " and/or " +
//               PalSourcecodeProcessor.class.getName()
//             )
//            );
//           }
  }
 }

 //Loads processors that were just compiled
 private static void loadAllProcessors() {
  REGISTERED_PROCESSOR_PATHS.forEach((final String path) -> {
   try {
    final Class<? extends IPalProcessor<?>> processor =
     (Class<? extends IPalProcessor<?>>)PAL_CLASSLOADER.findClass(path);
    registerProcessor(processor);
   } catch (ClassCastException e) {
    final IllegalArgumentException iae = new IllegalArgumentException(
     "Annotated class '" + path + "' does not implement io.github.matthewacon.pal.api.IPalProcessor!",
     e
    );
    throw ExceptionUtils.initFatal(iae);
   } catch (ClassNotFoundException e) {
    throw new RuntimeException("Invalid processor path: '" + path + "'!", e);
   }
  });
  REGISTERED_PROCESSOR_PATHS = null;
 }

 //Loads annotations that were just compiled
 private static void loadAllAnnotations() {
  REGISTERED_ANNOTATION_PATHS.forEach((final String path) -> {
   try {
    final Class<? extends Annotation> annotation = (Class<? extends Annotation>)PAL_CLASSLOADER.findClass(path);
    registerAnnotation(annotation);
   } catch (ClassCastException e) {
    final IllegalArgumentException iae = new IllegalArgumentException(
     "Annotated class '" + path + "' is not an annotation!",
     e
    );
    throw ExceptionUtils.initFatal(iae);
   } catch (ClassNotFoundException e) {
    throw new RuntimeException("Invalid annotation path: '" + path + "'!", e);
   }
  });
  REGISTERED_ANNOTATION_PATHS = null;
 }

 public static void registerAnnotation(@NotNull final String flatAnnotationPath) {
  REGISTERED_ANNOTATION_PATHS.add(flatAnnotationPath);
 }

 public static void registerAnnotation(@NotNull final Class<?> clazz) {
  if (Annotation.class.isAssignableFrom(clazz)) {
   REGISTERED_ANNOTATIONS.add((Class<? extends Annotation>)clazz);
  }
 }

 public static void registerProcessor(@NotNull final String flatProcessorPath) {
  REGISTERED_PROCESSOR_PATHS.add(flatProcessorPath);
 }

 public static void registerProcessor(@NotNull final Class<?> clazz) {
  if (clazz.getAnnotation(PalProcessor.class) != null) {
   if (IPalProcessor.class.isAssignableFrom(clazz)) {
    final Class<? extends Annotation> targetAnnotation = getGenericParameter(clazz);
    if (targetAnnotation == null) {
     throw new IllegalArgumentException(
      "Pal processors must specify which annotation they target as a generic parameter!"
     );
    }
    final IPalProcessor<?> processor;
    try {
     final Constructor[] constructors = clazz.getConstructors();
     Constructor defaultConstructor = null;
     for (final Constructor constructor : constructors) {
      if (constructor.getParameterCount() == 0 && (constructor.getModifiers() | Modifier.PUBLIC) == constructor.getModifiers()) {
       defaultConstructor = constructor;
       break;
      }
     }
     if (defaultConstructor != null) {
      defaultConstructor.setAccessible(true);
      processor = (IPalProcessor<?>) defaultConstructor.newInstance();
     } else {
      final InvalidClassException ice = new InvalidClassException(
       "Pal processors must either define a public default constructor!"
      );
      throw ExceptionUtils.initFatal(ice);
     }
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
     final InstantiationException ie = new InstantiationException(
      "Exception thrown while constructing Pal processor '" + clazz.getName() + "'!"
     );
     ie.initCause(e);
     throw ExceptionUtils.initFatal(ie);
    }
    REGISTERED_ANNOTATIONS.add(targetAnnotation);
    final NonBlockingHashSet<? super IPalProcessor<?>> processorSet;
    if (REGISTERED_PROCESSORS.get(targetAnnotation) != null) {
     processorSet = REGISTERED_PROCESSORS.get(targetAnnotation);
    } else {
     processorSet = new NonBlockingHashSet<>();
    }
    processorSet.add(processor);
    REGISTERED_PROCESSORS.put(targetAnnotation, processorSet);
   } else {
    throw ExceptionUtils.initFatal(
     new InvalidClassException(
      clazz.getName() +
       " must either implement " +
       PalBytecodeProcessor.class.getName() +
       " and/or " +
       PalSourcecodeProcessor.class.getName()
     )
    );
   }
  }
 }

 private static <T> Class<T> getGenericParameter(final Class<?> clazz) {
  final Type genericSuperclass = clazz.getGenericSuperclass();
  final Type[] genericInterfaces = clazz.getGenericInterfaces();
  final Class<T> targetParameter;
  GenericDiscovery:
  try {
   if (ParameterizedType.class.isAssignableFrom(genericSuperclass.getClass())) {
    final Type typeArgument = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
    targetParameter = (Class<T>) PAL_CLASSLOADER.findClass(typeArgument.getTypeName());
   } else {
    for (final Type type : genericInterfaces) {
     if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
      final Type typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
      targetParameter = (Class<T>) PAL_CLASSLOADER.findClass(typeArgument.getTypeName());
      break GenericDiscovery;
     }
    }
    targetParameter = null;
   }
  } catch (ClassNotFoundException e) {
   throw ExceptionUtils.initFatal(e);
  }
  return targetParameter;
 }
}
