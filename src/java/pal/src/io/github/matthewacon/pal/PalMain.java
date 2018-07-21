package io.github.matthewacon.pal;

import io.github.matthewacon.pal.api.IPalProcessor;
import io.github.matthewacon.pal.api.PalBytecodeProcessor;
import io.github.matthewacon.pal.api.PalSourceProcessor;
import io.github.matthewacon.pal.api.bytecode.PalProcessor;
import io.github.matthewacon.pal.processors.PalAnnotationProcessor;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.RoundEnvironment;
import javax.tools.JavaFileManager;
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

// private static final LinkedHashSet<? super JavaFileManager> FILE_MANAGERS;

 //2 states, initially NonBlockingHashMap<same erasure>, then LinkedHashMap<same erasure> after detection and ordering
 private static Map<Class<? extends Annotation>, ? super IPalProcessor<?>> REGISTERED_PROCESSORS;
 private static LinkedHashSet<String> REGISTERED_PROCESSOR_PATHS;

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

//  FILE_MANAGERS = new LinkedHashSet<>();
//  REGISTERED_PROCESSORS = new LinkedHashMap<>();
  REGISTERED_PROCESSORS = new NonBlockingHashMap<>();
  REGISTERED_PROCESSOR_PATHS = new LinkedHashSet<>();

  //TODO clean this mess up
  //Detect Pal annotation processors on the classpath
  try {
   final LinkedList<Thread> threads = new LinkedList<>();
   final Enumeration<URL> manifests = PAL_CLASSLOADER.getResources("META-INF/MANIFEST.MF");
   while (manifests.hasMoreElements()) {
    final URL manifestURL = manifests.nextElement();
    final Manifest manifest = new Manifest(manifestURL.openStream());
    //TODO add support for multi-package values (split on ':')
    manifest.getMainAttributes().forEach((final Object key, final Object value) -> {
     if (key instanceof Attributes.Name && value instanceof String) {
      if (key.toString().equals("Pal-Processors")) {
//       final Thread processorLoader = new Thread(() -> {
        try {
         for (final String pckage : ((String)value).split(":")) {
          for (final Class<?> clazz : PAL_CLASSLOADER.getClassesInPackage(pckage)) {
           if (clazz.getAnnotation(PalProcessor.class) != null) {
            if (IPalProcessor.class.isAssignableFrom(clazz)) {
//           boolean processorRegistered = false;
             //TODO Break off into separate method and reinforce logic (not flexible enough for future changes to the api)
             //Start generic parameter discovery
             final Type genericSuperclass = clazz.getGenericSuperclass();
             final Type[] genericInterfaces = clazz.getGenericInterfaces();
             final Class<? extends Annotation> targetAnnotation;
             GenericDiscovery:
             if (ParameterizedType.class.isAssignableFrom(genericSuperclass.getClass())) {
              final Type typeArgument = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
              targetAnnotation = (Class<? extends Annotation>) PAL_CLASSLOADER.findClass(typeArgument.getTypeName());
             } else {
              for (final Type type : genericInterfaces) {
               if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
                final Type typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
                targetAnnotation = (Class<? extends Annotation>) PAL_CLASSLOADER.findClass(typeArgument.getTypeName());
                break GenericDiscovery;
               }
              }
              targetAnnotation = null;
             }
             //End generic parameter discovery
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
//             } else if (constructors.length == 0) {
//              processor = (IPalProcessor<?>)clazz.newInstance();
              } else {
               final InvalidClassException ice = new InvalidClassException(
                "Pal processors must either define a public default constructor or none at all!"
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
             REGISTERED_PROCESSORS.put(targetAnnotation, processor);
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
//           if (clazz.isInstance(PalSourceProcessor.class)) {
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
//               PalSourceProcessor.class.getName()
//             )
//            );
//           }
            } else {
             throw ExceptionUtils.initFatal(
              new InvalidClassException(
               clazz.getName() +
                " must either implement " +
                PalBytecodeProcessor.class.getName() +
                " and/or " +
                PalSourceProcessor.class.getName()
              )
             );
            }
           }
          }
         }
        } catch (Throwable t) {
         System.err.print("ENCOUNTERED EXCEPTION IN THREAD " + Thread.currentThread().getName() + " :: ");
         t.printStackTrace();
        }
//        finally {
//         Thread.currentThread().interrupt();
//        }
//       });
//       processorLoader.setDefaultUncaughtExceptionHandler((final Thread thread, final Throwable throwable) -> {
//        Thread.currentThread().interrupt();
//        threads.forEach(t -> t.interrupt());
//        System.out.print("UNCAUGHT EXCEPTION IN THREAD! " + Thread.currentThread().getName() + " :: ");
//        throwable.printStackTrace();
//        throw ExceptionUtils.initFatal(throwable);
//       });
//       processorLoader.start();
//       threads.add(processorLoader);
      }
     }
    });
   }
   //Wait for discovery threads to finish
   boolean allFinished = true;
   do {
    ThreadLoop: for (final Thread thread : threads) {
     if (thread.getState() != Thread.State.TERMINATED) {
      allFinished = false;
      break ThreadLoop;
     }
    }
   } while(!allFinished);

  } catch (Throwable t) {
   final RuntimeException re = new RuntimeException("Error loading Pal annotation processors!");
   re.initCause(t);
   throw re;
  }
  //TODO conditional debug
  System.out.println(" ");
  REGISTERED_PROCESSORS.forEach((annotation, processor) ->
   System.out.println(annotation + " :: " + processor.getClass())
  );
  System.out.println();
 }

 private PalMain() {}

 //Apply
 protected static void onCompileStarted(final RoundEnvironment re) {

 }

 protected static synchronized void onCompileFinished(final Class<?>[] definedClasses) {
  //TODO conditional debug
  for (final Class<?> clazz : definedClasses) {
   System.out.println("Defined: " + clazz);
  }
//  loadAllProcessors();
 }

 private static void loadAllProcessors() {
  REGISTERED_PROCESSOR_PATHS.forEach((final String path) -> {
   try {
    final Class<? extends IPalProcessor<?>> processor =
     (Class<? extends IPalProcessor<?>>)PAL_CLASSLOADER.findClass(path);
    registerProcessor(processor);
   } catch (ClassCastException e) {
    final IllegalArgumentException iae = new IllegalArgumentException(
     "Annotated class '" + path + "' does not implement io.github.matthewacon.pal.api.IPalProcessor!"
    );
    iae.initCause(e);
    throw ExceptionUtils.initFatal(iae);
   } catch (ClassNotFoundException e) {
    final RuntimeException re = new RuntimeException("Invalid processor path: '" + path + "'!");
    re.initCause(e);
    throw re;
   }
  });
  REGISTERED_PROCESSOR_PATHS = null;
 }

 public static synchronized void registerProcessor(@NotNull final String flatProcessorPath) {
  REGISTERED_PROCESSOR_PATHS.add(flatProcessorPath);
 }

 public static synchronized void registerProcessor(@NotNull final Class<? extends IPalProcessor<?>> processor) {
  processor.getClass().getGenericSuperclass();
 }
}
