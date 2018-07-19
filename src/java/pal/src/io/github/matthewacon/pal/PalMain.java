package io.github.matthewacon.pal;

import io.github.matthewacon.pal.api.IPalProcessor;
import org.jetbrains.annotations.NotNull;

import javax.tools.JavaFileManager;
import java.io.File;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public final class PalMain {
 public static final DisposableClassLoader PAL_CLASSLOADER;
 public static final File TEMP_DIR;

 private static final LinkedHashSet<? super JavaFileManager> FILE_MANAGERS;
 private static final LinkedHashMap<? extends Annotation, Class<? extends IPalProcessor<?>>> REGISTERED_PROCESSORS;
 private static LinkedHashSet<String> REGISTERED_PROCESSOR_PATHS;

 static {
  PAL_CLASSLOADER = new DisposableClassLoader(PalAgent.getInstrumentation());
  TEMP_DIR = new File(System.getProperty("java.io.tmpdir") + "/palResources");
  TEMP_DIR.deleteOnExit();

  FILE_MANAGERS = new LinkedHashSet<>();
  REGISTERED_PROCESSORS = new LinkedHashMap<>();
  REGISTERED_PROCESSOR_PATHS = new LinkedHashSet<>();
 }

 private PalMain() {}

 protected void onCompileFinished() {
  loadAllProcessors();

 }

 public static synchronized void registerProcessor(@NotNull final String flatProcessorPath) {
  REGISTERED_PROCESSOR_PATHS.add(flatProcessorPath);
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

 public static synchronized void registerProcessor(@NotNull final Class<? extends IPalProcessor<?>> processor) {
  processor.getClass().getGenericSuperclass();
 }

 public static synchronized void registerJavaFileManager(@NotNull final JavaFileManager manager) {
  if (FILE_MANAGERS.add(manager)) {
   new CompilerHooks(PAL_CLASSLOADER, manager);
  }
 }
}
