package io.github.matthewacon.pal.agent.bootstrap;

import sun.reflect.ConstantPool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

@SuppressWarnings({"unchecked", "sunapi"})
public final class AnnotationParserDispatch {
 private static final Method[] ANNOTATION_PARSER_METHODS;

 public static final Constructor
  ANNOTATION_TYPE_MISMATCH_EXCEPTION_PROXY,
  ENUM_CONSTANT_NOT_PRESENT_EXCEPTION_PROXY;

 static {
  ClassLoader foremostParent = ClassLoader.getSystemClassLoader();
  while (foremostParent.getParent() != null) foremostParent = foremostParent.getParent();
  try {
   final Class
    atmep = foremostParent.loadClass("sun.reflect.annotation.AnnotationTypeMismatchExceptionProxy"),
    ecnpep = foremostParent.loadClass("sun.reflect.annotation.EnumConstantNotPresentExceptionProxy");
   ANNOTATION_TYPE_MISMATCH_EXCEPTION_PROXY = atmep.getDeclaredConstructor(String.class);
   ENUM_CONSTANT_NOT_PRESENT_EXCEPTION_PROXY = ecnpep.getDeclaredConstructor(Class.class, String.class);
   ANNOTATION_PARSER_METHODS = foremostParent
    .loadClass("sun.reflect.annotation.AnnotationParser")
    .getDeclaredMethods();
   ANNOTATION_TYPE_MISMATCH_EXCEPTION_PROXY.setAccessible(true);
   ENUM_CONSTANT_NOT_PRESENT_EXCEPTION_PROXY.setAccessible(true);
  } catch(Throwable t) {
   throw new RuntimeException(t);
  }
 }

 public static Object newAnnotationTypeMismatchExceptionProxy(String str) {
  try {
   return ANNOTATION_TYPE_MISMATCH_EXCEPTION_PROXY.newInstance(str);
  } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
   throw new RuntimeException(e);
  }
 }

 public static Object newEnumConstantNotPresentExceptionProxy(Class clazz, String str) {
  try {
   return ENUM_CONSTANT_NOT_PRESENT_EXCEPTION_PROXY.newInstance(clazz, str);
  } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
   throw new RuntimeException(e);
  }
 }

 public static boolean primitiveMatch(Class first, Class second) {
  if (first.isPrimitive()) {
   return (first.getSimpleName().equals("int") && second.getSimpleName().equals("Integer"))
    || (first.getSimpleName().equals("float") && second.getSimpleName().equals("Float"))
    || (first.getSimpleName().equals("Long") && second.getSimpleName().equals("Long"))
    || (first.getSimpleName().equals("double") && second.getSimpleName().equals("Double"))
    || (first.getSimpleName().equals("short") && second.getSimpleName().equals("Short"))
    || (first.getSimpleName().equals("boolean") && second.getSimpleName().equals("Boolean"));
  }
  return false;
 }

 public static boolean typeCheck(Class first, Class second) {
  return first.equals(second)
    || first.isAssignableFrom(second)
    || second.isAssignableFrom(first)
    || primitiveMatch(first, second)
    || primitiveMatch(second, first);
 }

 //TODO support varargs
 public static Object invoke(final String name, Object... args) {
  final Class[] argTypes = new Class[args.length];
  for (int i = 0; i < args.length; i++) {
   argTypes[i] = args[i].getClass();
  }
  Method method = null;
  for (Method m : ANNOTATION_PARSER_METHODS) {
   if (m.getName().equals(name)) {
    final Class[] parameterTypes = m.getParameterTypes();
    boolean typeMatch = true;
    for (int i = 0; i < argTypes.length; i++) {
     typeMatch &= typeCheck(argTypes[i], parameterTypes[i]);
    }
    if (typeMatch && (argTypes.length == parameterTypes.length)) {
     method = m;
     break;
    }
   }
  }
  method.setAccessible(true);
  try {
   return method.invoke(null, args);
  } catch (IllegalAccessException | InvocationTargetException e) {
   throw new RuntimeException(e);
  }
 }

 public static final class ParseEnumValue {
  public static Object parseEnumValue(Class<? extends java.lang.Enum> var0, ByteBuffer var1, ConstantPool var2, Class<?> var3) {
   int var4 = var1.getShort() & '\uffff';
   String var5 = var2.getUTF8At(var4);
   int var6 = var1.getShort() & '\uffff';
   String var7 = var2.getUTF8At(var6);
   if (!var5.endsWith(";")) {
    if (!var0.getName().equals(var5)) {
     return newAnnotationTypeMismatchExceptionProxy(var5 + "." + var7);
    }
   } else {
    final Class<? extends java.lang.Enum> sig = (Class<? extends java.lang.Enum>)invoke("parseSig", var5, var3);
    if (!var0.isAssignableFrom(sig)) {
     return newAnnotationTypeMismatchExceptionProxy(var5 + "." + var7);
    } else {
     try {
      return java.lang.Enum.valueOf(sig, var7);
     } catch (IllegalArgumentException var9) {
      return newEnumConstantNotPresentExceptionProxy(sig, var7);
     }
    }
   }
   try {
    return java.lang.Enum.valueOf(var0, var7);
   } catch (IllegalArgumentException var9) {
    return newEnumConstantNotPresentExceptionProxy(var0, var7);
   }
  }
 }

 public static final class ParseArray {
  public static Object parseArray(Class<?> var0, ByteBuffer var1, ConstantPool var2, Class<?> var3) {
   int var4 = var1.getShort() & '\uffff';
   Class var5 = var0.getComponentType();
   if (var5 == Byte.TYPE) {
    return invoke("parseByteArray", var4, var1, var2);
   } else if (var5 == Character.TYPE) {
    return invoke("parseCharArray", var4, var1, var2);
   } else if (var5 == Double.TYPE) {
    return invoke("parseDoubleArray", var4, var1, var2);
   } else if (var5 == Float.TYPE) {
    return invoke("parseFloatArray", var4, var1, var2);
   } else if (var5 == Integer.TYPE) {
    return invoke("parseIntArray", var4, var1, var2);
   } else if (var5 == Long.TYPE) {
    return invoke("parseLongArray", var4, var1, var2);
   } else if (var5 == Short.TYPE) {
    return invoke("parseShortArray", var4, var1, var2);
   } else if (var5 == Boolean.TYPE) {
    return invoke("parseBooleanArray", var4, var1, var2);
   } else if (var5 == String.class) {
    return invoke("parseStringArray", var4, var1, var2);
   } else if (var5 == Class.class) {
    return invoke("parseClassArray", var4, var1, var2, var3);
    //TODO Narrow case
   } else if (var5.isEnum() || (var5.isInterface() && !var5.isAnnotation())) {
    return invoke("parseEnumArray", var4, var5, var1, var2, var3);
   } else {
    //TODO why was this assertion in the original sun method
//    assert var5.isAnnotation();
    return invoke("parseAnnotationArray", var4, var5, var1, var2, var3);
   }
  }
 }
}