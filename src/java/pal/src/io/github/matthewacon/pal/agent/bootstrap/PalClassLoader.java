package io.github.matthewacon.pal.agent.bootstrap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PalClassLoader extends ClassLoader {
 public PalClassLoader(final ClassLoader parent) {
  super(parent);
 }

 private Class<?> loadInstrumentedClass(String name) {
  String[] parsed = name.split("\\.");
  if (parsed.length >= 1) name = parsed[parsed.length-1];
  try {
   byte[] eventData = Files.readAllBytes(new File(System.getProperty("user.dir") + "/ORIGINAL_CLASSES/" + name + ".class").toPath());
   return super.defineClass(name, eventData, 0, eventData.length);
  } catch (IOException e) {
   RuntimeException re = new RuntimeException();
   re.initCause(e);
   throw re;
  }
 }

 public Class<?> loadClass(String name) throws ClassNotFoundException {
//  if (name.equals("IEvent") || name.contains("AnnotationParser")) {
  if (name.equals("IEvent")) {
   return loadInstrumentedClass(name);
  } else {
   return super.loadClass(name);
  }
 }

 public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//  if (name.equals("IEvent") || name.contains("AnnotationParser")) {
  if (name.equals("IEvent")) {
   return loadInstrumentedClass(name);
  } else {
   return super.loadClass(name, resolve);
  }
 }

 public Class<?> findClass(String name) throws ClassNotFoundException {
  if (name.contains("AnnotationParserDispatch")) {
   try {
    final byte[] classData = Files.readAllBytes(new File(System.getProperty("user.dir")+"/target/classes/AnnotationParserDispatch.class").toPath());
    return defineClass("AnnotationParserDispatch", classData, 0, classData.length);
   } catch (Throwable t) {
    RuntimeException re = new RuntimeException();
    re.initCause(t);
    throw re;
   }
  }
  return super.findClass(name);
 }
}
