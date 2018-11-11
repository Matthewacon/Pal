package io.github.matthewacon.pal;

import com.sun.tools.javac.code.Symbol;
import io.github.matthewacon.pal.util.ClassUtils;
import io.github.matthewacon.pal.util.ExceptionUtils;
import io.github.matthewacon.pal.util.LinkedTreeMap;
import sun.misc.Launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.JarURLConnection;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
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

 //TODO DOC, IMPL - Redefines a class without the restrictions imposed by Instrumentation-based class redefinition
 //Any properties in the class may be changed, added or removed, so long as the class bytecode is still valid
 public native Class<?> redefineClass(final byte[] bytes) throws ClassNotFoundException, ClassFormatError;

 //TODO DOC - Searches system classpath, bootstrap classpath and compiled classes
 //TODO Deal with embedded generic classes
 public Class<?> findClass(final String name) throws ClassNotFoundException {
  Class<?> clazz;
  try {
   clazz = super.findClass(name);
  } catch (ClassNotFoundException e) {
   try {
    //Search bootstrap classpath first
    if ((clazz = (Class<?>)ClassLoaderNativeAccessor.findBootstrapClassOrNull.invoke(this, name)) == null) {
     //If not found in bootstrap classpath, search injected classes
     for (final Class<?> injected : injectedClasses) {
      if (injected.getName().equals(name)) {
       clazz = injected;
       break;
      }
     }
     //Cut out any generic parameters and try to resolve the base classes (re-inject generic parameters after the fact)
     //TODO Once the ExampleLinkedTreeMap utilities are finished, convert over to use ClassUtils#lexGenericParameters and
     //TODO transform the resulting tree into classes. (isolation of utilities)
     if (name.contains("<")) {
      final String expr = name.replaceAll("\\s+", "");
      int
       index = 0,
       lastMatch = -1;
      char lastIdentifier = '\0';
      LinkedTreeMap<Class<?>> root = null;
      while (index < expr.length()) {
       final char c = expr.charAt(index);
       final String sub = expr.substring(lastMatch + 1, index);
       if (c == '<') {
        lastMatch = index;
        final LinkedTreeMap<Class<?>> newParent = new LinkedTreeMap<>(root, findClass(sub));
        if (root != null) {
         root.addChild(newParent);
        }
        root = newParent;
        lastIdentifier = c;
       } else if (c == '>') {
        lastMatch = index;
        if (lastIdentifier != '>') {
         root.addChild(
          new LinkedTreeMap<>(root, findClass(sub))
         );
        }
        root = root.getParent() == null ? root : root.getParent();
        lastIdentifier = c;
       } else if (c == ',') {
        lastMatch = index;
        if (lastIdentifier != '>') {
         root.addChild(
          new LinkedTreeMap<>(root, findClass(sub))
         );
        }
        lastIdentifier = c;
       }
       index++;
      }
      final Class<?> testClazz = ClassUtils.getGenericParameter(root.getClass());
      System.out.println();
     }
    }
    //Finally, if none of the searches have yielded any result, throw the original exception
    if (clazz == null) {
     throw e;
    }
   } catch (InvocationTargetException | IllegalAccessException e1) {
    throw ExceptionUtils.initFatal(e1);
   }
  }
  return clazz;
 }

 //TODO DOC - Returns a list of class candidates matching the given parameters
 public HashSet<Class<?>> findClass(
  //Not nullable
  final String baseName,
  //Not nullable (same as baseName if unknown)
  final String qualifiedName,
  //Nullable
  final String pckage,
  //Nullable
  final List<String> imports
 ) {
  final HashSet<Class<?>> classCandidates = new HashSet<>();
  if (baseName.equals(qualifiedName)) {
   if (pckage != null) {
    //Find based on package and base name
    try {
     classCandidates.add(findClass(pckage + "." + baseName));
    } catch (ClassNotFoundException e) {}
   }
   if (imports != null) {
    //Find based on imports (if not already found in the package scope)
    imports.stream()
     .filter(iport -> {
      final String lastQualifier = iport.substring(iport.lastIndexOf(".") + 1);
      return lastQualifier.equals(baseName);
     })
     .forEach(iport -> {
      try {
       classCandidates.add(findClass(iport));
      } catch (ClassNotFoundException e) {}
     });
   }
   //Find based on default java.lang.* imports
   try {
    classCandidates.add(findClass("java.lang." + baseName));
   } catch (ClassNotFoundException e) {}
  } else {
   //Find based on fully qualified name
   try {
    classCandidates.add(findClass(qualifiedName));
   } catch (ClassNotFoundException e) {}
  }
  return classCandidates;
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

 //TODO clean this up
 //TODO Without multi-threading, this will not scale well for large classpaths
 private void scanClassesInJar(final String jarPath, final String pckage, final String packageAsPath, final HashSet<String> discoveredFiles) throws FileNotFoundException {
  try {
   final JarURLConnection connection =
    (JarURLConnection) new URL("jar:file:///" + jarPath + "!/" + packageAsPath).openConnection();
   final JarFile file = connection.getJarFile();
   final Enumeration<JarEntry> entries = file.entries();
   while (entries.hasMoreElements()) {
    final JarEntry entry = entries.nextElement();
    final String name = entry.getName();
    if (name.contains(packageAsPath) && !entry.isDirectory()) {
     final String qualifiedPath =
      name
       .substring(name.indexOf(packageAsPath), name.length())
       .replace("/", ".")
       .replace(".class", "");
     discoveredFiles.add(qualifiedPath);
    }
   }
  } catch (FileNotFoundException e) {
   throw e;
  } catch (IOException e) {
   throw ExceptionUtils.initFatal(e);
  }
 }

 //TODO clean this up
 //TODO Without multi-threading, this will not scale well for large classpaths
 public Class<?>[] getClassesInPackage(final String pckage) {
  HashSet<Class<?>> classes;
  //Only attempt discovery if the package has not been scanned already
  if ((classes = discoveredClasses.get(pckage)) == null) {
   classes = new HashSet<>();
   final HashSet<String> discoveredFiles = new HashSet<>();
   final String packageAsPath = pckage.replace(".", "/");
   final Vector<String> paths = new Vector<>();
   //All files on the classpath
   for (final String path : System.getProperty("java.class.path").split(":")) {
    if (!path.isEmpty() && new File(path).exists()) {
     paths.add(path);
    }
   }
   //All files on the bootstrap classpath
   for (final URL url : Launcher.getBootstrapClassPath().getURLs()) {
    paths.add(url.getPath());
   }
   //Class discovery (recursively searches directories)
   for (final String path : paths) {
    if (path.endsWith(".jar")) {
     try {
      scanClassesInJar(path, pckage, packageAsPath, discoveredFiles);
     } catch (FileNotFoundException e) {/*Not fatal. Move onto next file.*/}
    } else if (path.endsWith(".class")) {
     if (path.contains(packageAsPath)) {
      final String qualifiedPath = path
       .substring(path.indexOf(packageAsPath), path.length())
       .replace("/", ".")
       .replace(".class", "");
      discoveredFiles.add(qualifiedPath);
     }
    } else {
     if (new File(path).isDirectory()) {
      try {
       Files
        .walk(new File(path).toPath())
        .filter(Files::isRegularFile)
        .forEach(p -> {
         final String fullPath = p.toString();
         if (fullPath.endsWith(".jar")) {
          try {
           scanClassesInJar(fullPath, pckage, packageAsPath, discoveredFiles);
          } catch (FileNotFoundException e) {/*Not fatal. Move onto next file*/}
         } else if (fullPath.endsWith(".class")) {
          if (fullPath.contains(packageAsPath)) {
           final String qualifiedPath = fullPath
            .substring(fullPath.indexOf(packageAsPath), fullPath.length())
            .replace("/", ".")
            .replace(".class", "");
           discoveredFiles.add(qualifiedPath);
          }
         }
        });
      } catch (Throwable t) {
       //TODO not necessarily fatal
       throw ExceptionUtils.initFatal(t);
      }
     }
    }
   }

   //Define all discovered classes
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
     "Error: Package '" + pckage + "' was not found on the classpath!"
    );
    throw ExceptionUtils.initFatal(fnfe);
   }
  }
  return classes.toArray(new Class<?>[0]);
 }
}