package io.github.matthewacon.pal;

import com.sun.tools.javac.code.Symbol;
import sun.misc.Launcher;

import java.io.FileNotFoundException;
import java.net.JarURLConnection;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class DisposableClassLoader extends ClassLoader {
 private static final class ClassLoaderNativeAccessor {
  public static final Method
   defineClass0,
   findBootstrapClassOrNull;
//   defineClass1,
//   defineClass2,
//   resolveClass0,
//   findBootstrapClass,
//   findLoadedClass,
//   findBuiltinLib,
//   retrieveDirectives;

  static {
   try {
    defineClass0 = ClassLoader.class.getDeclaredMethod("defineClass0", String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
    defineClass0.setAccessible(true);
    findBootstrapClassOrNull = ClassLoader.class.getDeclaredMethod("findBootstrapClassOrNull", String.class);
    findBootstrapClassOrNull.setAccessible(true);
//    defineClass1 = ClassLoader.class.getDeclaredMethod("defineClass1", String.class, byte[].class, int.class, int.class, ProtectionDomain.class, String.class);
//    defineClass1.setAccessible(true);
//    defineClass2 = ClassLoader.class.getDeclaredMethod("defineClass2", String.class, ByteBuffer.class, int.class, int.class, ProtectionDomain.class, String.class);
//    defineClass2.setAccessible(true);
//    resolveClass0 = ClassLoader.class.getDeclaredMethod("resolveClass0", Class.class);
//    resolveClass0.setAccessible(true);
//    findBootstrapClass = ClassLoader.class.getDeclaredMethod("findBootstrapClass", String.class);
//    findBootstrapClass.setAccessible(true);
//    findLoadedClass = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
//    findLoadedClass.setAccessible(true);
//    findBuiltinLib = ClassLoader.class.getDeclaredMethod("findBuiltinLib", String.class);
//    findBuiltinLib.setAccessible(true);
//    retrieveDirectives = ClassLoader.class.getDeclaredMethod("retrieveDirectives");
//    retrieveDirectives.setAccessible(true);
   } catch (NoSuchMethodException e) {
    throw ExceptionUtils.initFatal(e);
   }
  }
 }

 private final Instrumentation instrumentation;
 private final Vector<Class<?>> injectedClasses;
 private final LinkedHashMap<String, HashSet<Class<?>>> discoveredClasses;

 public DisposableClassLoader(final Instrumentation instrumentation) {
  this.instrumentation = instrumentation;
  this.injectedClasses = new Vector<>();
  this.discoveredClasses = new LinkedHashMap<>();
 }

 //TODO DOC - Searches system classpath, bootstrap classpath and compiled classes
 public Class<?> findClass(final String name) throws ClassNotFoundException {
  Class<?> clazz;
  try {
   clazz = super.findClass(name);
  } catch (ClassNotFoundException e) {
   try {
    if ((clazz = (Class<?>)ClassLoaderNativeAccessor.findBootstrapClassOrNull.invoke(this, name)) == null) {
     for (final Class<?> injected : injectedClasses) {
      if (injected.getName().equals(name)) {
       clazz = injected;
       break;
      }
     }
    }
    if (clazz == null) {
     throw e;
    }
   } catch (InvocationTargetException | IllegalAccessException e1) {
    throw ExceptionUtils.initFatal(e1);
   }
  }
  return clazz;
 }

 //TODO (once NativeUtils are done) define java.lang.Class impl where ALL forms of instantiation (static included) throw
 //TODO an  UnsupportedOperationException for any injected class, to prevent potential symbol resolution and unsatisfied
 //TODO link errors from arising on the compiler. These classes are only injected into the JVM instance so that they may
 //TODO be instrumented and NOT instantiated.
 public Class<?> defineClass(final String name, final byte[] classData) throws NoClassDefFoundError {
  Class<?> clazz;
  try {
   clazz = super.findClass(name);
  } catch(ClassNotFoundException e) {
   try {
    clazz = (Class<?>)ClassLoaderNativeAccessor
     .defineClass0
     .invoke(this, name, classData, 0, classData.length, null);
   } catch (IllegalAccessException | InvocationTargetException e1) {
    if (e1 instanceof InvocationTargetException) {
     final Throwable except = ((InvocationTargetException)e1).getTargetException();
     if (except instanceof NoClassDefFoundError) {
      throw (NoClassDefFoundError)except;
     }
    }
    throw ExceptionUtils.initFatal(e1);
   }
  }
  return clazz;
 }

 //TODO DOC - Accepts a list of class definitions and tries all registration orders (3-pass tolerance)
 public Class<?>[] defineClasses(final LinkedHashMap<Symbol.ClassSymbol, byte[]> orderedHierarchy) {
  LinkedList<Symbol.ClassSymbol> classQueue = new LinkedList<>(orderedHierarchy.keySet());
  final LinkedList<Symbol.ClassSymbol> failedDefinitions = new LinkedList<>();
  final Vector<Class<?>> successfulDefinitions = new Vector<>();
  int successiveDefinitionFailures = 0;
  for (int i = 0; i < classQueue.size(); i++) {
   final Symbol.ClassSymbol symbol = classQueue.get(i);
   final byte[] data = orderedHierarchy.get(symbol);
   Class<?> clazz;
   try {
    clazz = defineClass(symbol.flatname.toString(), data);
   } catch(NoClassDefFoundError e) {
    failedDefinitions.add(symbol);
    continue;
   }
   successfulDefinitions.add(clazz);
   injectedClasses.add(clazz);
   orderedHierarchy.remove(symbol);
  }
  while(classQueue.peek() != null) {
   classQueue = new LinkedList<>(orderedHierarchy.keySet());
   boolean successfullyDefined = false;
   if (!failedDefinitions.isEmpty()) {
    for (int i = 0; i < failedDefinitions.size(); i++) {
     final Symbol.ClassSymbol symbol = failedDefinitions.get(i);
     final byte[] data = orderedHierarchy.get(symbol);
     Class<?> clazz;
     try {
      clazz = defineClass(symbol.flatname.toString(), data);
     } catch(NoClassDefFoundError e) {
      if (successiveDefinitionFailures >= 3) {
       final RuntimeException re = new RuntimeException("Unable to define classes!");
       re.initCause(e);
       throw re;
      }
      continue;
     }
     successfullyDefined = true;
     successfulDefinitions.add(clazz);
     injectedClasses.add(clazz);
     orderedHierarchy.remove(symbol);
     failedDefinitions.remove(symbol);
     successiveDefinitionFailures = 0;
    }
   }
   //If failed definitions are the only classes left to define
   successiveDefinitionFailures += !successfullyDefined ? 1 : 0;
  }
  return successfulDefinitions.toArray(new Class<?>[0]);
 }

 //TODO multithread
 public Class<?>[] getClassesInPackage(final String pckage) {
  HashSet<Class<?>> classes;
  //Only attempt discovery if the package has not already been scanned
  if ((classes = discoveredClasses.get(pckage)) == null) {
   classes = new HashSet<>();
   final HashSet<String> discoveredFiles = new HashSet<>();
   final String packagePath = pckage.replace(".", "/");
   //TODO Recursively search files and directories on the classpath
//   final String classpath = System.getProperty("java.class.path");
//   final String[] paths = classpath.split(":");

   //Discover files on the bootstrap classpath
   for (final URL jar : Launcher.getBootstrapClassPath().getURLs()) {
    try {
     final String jarPath = jar.getPath();
     final JarURLConnection connection =
      (JarURLConnection) new URL("jar:file:///" + jarPath + "!/" + packagePath).openConnection();
     final JarFile file = connection.getJarFile();
     final Enumeration<JarEntry> entries = file.entries();
//     System.out.println(file);
     while (entries.hasMoreElements()) {
      final JarEntry entry = entries.nextElement();
//      System.out.println(entry);
      final String name = entry.getName();
      if (name.contains(packagePath) && !entry.isDirectory()) {
       final String qualifiedPath =
        name
         .substring(name.indexOf(packagePath), name.length())
         .replace("/", ".")
         .replace(".class", "");
       discoveredFiles.add(qualifiedPath);
      }
     }
    } catch (FileNotFoundException e) {
     continue;
    } catch (IOException e) {
     throw ExceptionUtils.initFatal(e);
    }
   }
   if (discoveredFiles.size() > 0) {
    try {
     for (final String file : discoveredFiles) {
      classes.add(findClass(file));
     }
    } catch (ClassNotFoundException e) {
     throw ExceptionUtils.initFatal(e);
    }
    //Append discovered classes to global map
    discoveredClasses.put(pckage, classes);
   } else {
    final FileNotFoundException fnfe = new FileNotFoundException(
     "Error: Package '" + pckage + "' was not found in jars or folders on the classpath!"
    );
    throw ExceptionUtils.initFatal(fnfe);
   }
  }
  return classes.toArray(new Class<?>[0]);
 }
}