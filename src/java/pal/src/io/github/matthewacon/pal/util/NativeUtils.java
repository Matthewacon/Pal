package io.github.matthewacon.pal.util;

import io.github.matthewacon.pal.util.ExceptionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Vector;

//TODO Documentation
//TODO Convert to ReflectionUtils
public final class NativeUtils {
 //java.lang.ClassLoader.NativeLibrary related constants
 public static final Class ClassLoader_NativeLibrary;

 //Vector<ClassLoader.NativeLibrary> fields in java.lang.ClassLoader
 public static final Field
  //Instance-bound properties
  //Instance-bound properties
  ClassLoader_loadedLibraryNames,
  ClassLoader_nativeLibraries,
  //Static properties
  ClassLoader_systemNativeLibraries,
  ClassLoader_nativeLibraryContext,
  ClassLoader_scl;

 //TODO make an annotation for creating mirror accessor classes
 //NOTE - mirrored objects are references and thus can be modified without reflectively resetting them in the original
 //object. However, primitive types are not objects and thus must be reflectively changed in the original object to
 //match the mirror object.
 //Mirror of java.lang.ClassLoader.NativeLibrary
 public static final class NativeLibrary {
  //Instance-bound fields in java.lang.ClassLoader.NativeLibrary
  public static final Field
   NativeLibrary_handle,
   NativeLibrary_jniVersion,
   NativeLibrary_fromClass,
   NativeLibrary_name,
   NativeLibrary_isBuiltin,
   NativeLibrary_loaded;

  //Instance-bound methods in java.lang.ClassLoader.NativeLibrary
  public static final Method
   NativeLibrary_load,
   NativeLibrary_find,
   NativeLibrary_unload,
   NativeLibrary_finalize,
   NativeLibrary_getFromClass;

  static {
   try {
    //Fields
    NativeLibrary_handle = ClassLoader_NativeLibrary.getDeclaredField("handle");
    NativeLibrary_handle.setAccessible(true);
    NativeLibrary_jniVersion = ClassLoader_NativeLibrary.getDeclaredField("jniVersion");
    NativeLibrary_jniVersion.setAccessible(true);
    NativeLibrary_fromClass = ClassLoader_NativeLibrary.getDeclaredField("fromClass");
    NativeLibrary_fromClass.setAccessible(true);
    NativeLibrary_name = ClassLoader_NativeLibrary.getDeclaredField("name");
    NativeLibrary_name.setAccessible(true);
    NativeLibrary_isBuiltin = ClassLoader_NativeLibrary.getDeclaredField("isBuiltin");
    NativeLibrary_isBuiltin.setAccessible(true);
    NativeLibrary_loaded = ClassLoader_NativeLibrary.getDeclaredField("loaded");
    NativeLibrary_loaded.setAccessible(true);

    //Methods
    NativeLibrary_load = ClassLoader_NativeLibrary.getDeclaredMethod("load", String.class, boolean.class);
    NativeLibrary_load.setAccessible(true);
    NativeLibrary_find = ClassLoader_NativeLibrary.getDeclaredMethod("find", String.class);
    NativeLibrary_find.setAccessible(true);
    NativeLibrary_unload = ClassLoader_NativeLibrary.getDeclaredMethod("unload", String.class, boolean.class);
    NativeLibrary_unload.setAccessible(true);
    NativeLibrary_finalize = ClassLoader_NativeLibrary.getDeclaredMethod("finalize");
    NativeLibrary_finalize.setAccessible(true);
    NativeLibrary_getFromClass = ClassLoader_NativeLibrary.getDeclaredMethod("getFromClass");
    NativeLibrary_getFromClass.setAccessible(true);
   } catch(Throwable t) {
    throw ExceptionUtils.initFatal(t);
   }
  }

//  private final Object nativeLibraryInstance;
  private final ExceptionUtils.ReflectionHandler rf;

  protected NativeLibrary(Object nativeLibraryInstance) {
   if (!nativeLibraryInstance.getClass().isAssignableFrom(ClassLoader_NativeLibrary)) {
    //TODO error message
    throw new IllegalArgumentException();
   }
//   this.nativeLibraryInstance = nativeLibraryInstance;
   this.rf = new ExceptionUtils.ReflectionHandler(nativeLibraryInstance);
  }

  //Mirror of instance-bound methods defined in java.lang.ClassLoader.NativeLibrary
  public void load(String name, boolean var2) {
   rf.fatalIfErrorInvoke(NativeLibrary_load, name, var2);
  }

  public long find(String name) {
   return (long)rf.fatalIfErrorInvoke(NativeLibrary_find, name);
  }

  public void unload(String name, boolean var2) {
   rf.fatalIfErrorInvoke(NativeLibrary_unload, name, var2);
  }

  //TODO probably not a good idea, we'll see
//  protected void finalize() {
//   fatalIfErrorInvoke(NativeLibrary_finalize);
//  }

  public Class<?> getFromClass() {
   return (Class<?>)rf.fatalIfErrorInvoke(NativeLibrary_getFromClass);
  }

  //Getters for all instance-bound properties of java.lang.ClassLoader.NativeLibrary
  public long handle() {
   return (long)rf.fatalIfErrorGetField(NativeLibrary_handle);
  }

  public int jniVersion() {
   return (int)rf.fatalIfErrorGetField(NativeLibrary_jniVersion);
  }

  public Class<?> fromClass() {
   return (Class<?>)rf.fatalIfErrorGetField(NativeLibrary_fromClass);
  }

  public String name() {
   return (String)rf.fatalIfErrorGetField(NativeLibrary_name);
  }

  public boolean isBuiltin() {
   return (boolean)rf.fatalIfErrorGetField(NativeLibrary_isBuiltin);
  }

  public boolean loaded() {
   return (boolean)rf.fatalIfErrorGetField(NativeLibrary_loaded);
  }
 }

 static {
  try {
   //ClassLoader.NativeLibrary related constants
   ClassLoader_NativeLibrary = Class.forName("java.lang.ClassLoader$NativeLibrary");

   //ClassLoader fields
   ClassLoader_loadedLibraryNames = ClassLoader.class.getDeclaredField("loadedLibraryNames");
   ClassLoader_loadedLibraryNames.setAccessible(true);
   ClassLoader_systemNativeLibraries = ClassLoader.class.getDeclaredField("systemNativeLibraries");
   ClassLoader_systemNativeLibraries.setAccessible(true);
   ClassLoader_nativeLibraries = ClassLoader.class.getDeclaredField("nativeLibraries");
   ClassLoader_nativeLibraries.setAccessible(true);
   ClassLoader_nativeLibraryContext = ClassLoader.class.getDeclaredField("nativeLibraryContext");
   ClassLoader_nativeLibraryContext.setAccessible(true);
   ClassLoader_scl = ClassLoader.class.getDeclaredField("scl");
   ClassLoader_scl.setAccessible(true);
  } catch (Throwable t) {
   throw ExceptionUtils.initFatal(t);
  }
 }

 public static native <T> T getInstanceFromStack(final int depth) throws Exception;

 public static <T> int firstDepthOfClassOnStack(final Class<T> clazz) {
  int frame = 0;
  final StackTraceElement[] trace = Thread.currentThread().getStackTrace();
  for (int i = 0; i < trace.length; i++) {
   String name = trace[i].getClassName();
   //Subclasses push themselves to slot 0, above the target class
//   if (name.lastIndexOf("$") == -1 && name.equals(clazz.getName())) {
   if (name.equals(clazz.getName())) {
    frame = i;
    break;
   }
  }
  return frame - 1;
 }

 public static <T> T getInstanceFromStack(final Class<T> clazz) throws Exception {
  return getInstanceFromStack(firstDepthOfClassOnStack(clazz));
 }

 public static Vector<String> getLoadedLibraryNames(final ClassLoader loader) {
  try {
   return (Vector<String>)ClassLoader_nativeLibraries.get(loader);
  } catch(Throwable t) {
   RuntimeException re = new RuntimeException();
   re.initCause(t);
   throw re;
  }
 }

 public static boolean isLibraryLoaded(final String name, final ClassLoader... classLoaders) {
  boolean loaded = false;
  if (classLoaders == null || classLoaders.length == 0) {
   ClassLoader parentLoader = ClassLoader.getSystemClassLoader();
   while (parentLoader.getParent() != null && !loaded) {
    loaded |= isLibraryLoaded(name, parentLoader);
    parentLoader = parentLoader.getParent();
   }
  } else {
   for (final ClassLoader loader : classLoaders) {
    final Vector<String> nativeLibraries;
    try {
     nativeLibraries = (Vector<String>)ClassLoader_loadedLibraryNames.get(loader);
    } catch (Throwable t) {
     RuntimeException re = new RuntimeException();
     re.initCause(t);
     throw re;
    }
    if (nativeLibraries.contains(name)) {
     loaded = true;
     break;
    }
   }
  }
  return loaded;
 }

 public static boolean unloadLibrary(final String name, final ClassLoader... classLoaders) {
  boolean libraryRemoved = false;
  if (classLoaders == null || classLoaders.length == 0) {
   libraryRemoved |= unloadLibrary(name, ClassLoader.getSystemClassLoader());
  } else if (classLoaders != null && classLoaders.length > 0) {
   for (final ClassLoader loader : classLoaders) {
    ClassLoader parent = loader;
    while (parent.getParent() != null) {
     libraryRemoved |= removeNativeLibrary(name, parent);
     parent = parent.getParent();
    }
    try {
     libraryRemoved |= removeNativeLibrary(name, (ClassLoader)ClassLoader_scl.get(null));
    } catch (IllegalAccessException e) {
     RuntimeException re = new RuntimeException();
     re.initCause(e);
     throw re;
    }
   }
  }
  return libraryRemoved;
 }

 private static boolean removeNativeLibrary(final String name, final ClassLoader classLoader) {
  boolean libraryLoaded = false;
  final Vector<String> loadedLibraryNames;
  final Vector<Object>
   clSystemNativeLibraries,
   clNativeLibraries,
   clNativeLibraryContext;
  try {
   loadedLibraryNames = (Vector<String>)ClassLoader_loadedLibraryNames.get(classLoader);
   clSystemNativeLibraries = (Vector<Object>)ClassLoader_systemNativeLibraries.get(null);
   clNativeLibraries = (Vector<Object>)ClassLoader_nativeLibraries.get(classLoader);
   clNativeLibraryContext = (Vector<Object>)ClassLoader_nativeLibraryContext.get(null);
  } catch (Throwable t) {
   RuntimeException re = new RuntimeException();
   re.initCause(t);
   throw re;
  }
  final LinkedHashMap<Vector<Object>, Object> toRemove = new LinkedHashMap<>();
  for (final Vector<Object> nativeLibs : new Vector[] {clSystemNativeLibraries, clNativeLibraries, clNativeLibraryContext}) {
   Inner: for (final Object nativeLib : nativeLibs) {
    final NativeLibrary refLib = new NativeLibrary(nativeLib);
    if (refLib.name().equals(name) && refLib.loaded()) {
     libraryLoaded = true;
     //TODO What is the flag for?
     refLib.unload(name, true);
     toRemove.put(nativeLibs, nativeLib);
     break Inner;
    }
   }
  }
  libraryLoaded |= loadedLibraryNames.remove(name);
  for (final Vector<Object> nativeLibVector : toRemove.keySet()) {
   libraryLoaded |= nativeLibVector.remove(toRemove.get(nativeLibVector));
  }
  return libraryLoaded;
 }
}
