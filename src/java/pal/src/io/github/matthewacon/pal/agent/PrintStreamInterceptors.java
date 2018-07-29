package io.github.matthewacon.pal.agent;

import com.sun.tools.javac.jvm.ClassWriter;
import io.github.matthewacon.pal.NativeUtils;
import net.bytebuddy.asm.Advice;

import java.io.PrintStream;

public final class PrintStreamInterceptors {
 public static final class Print {
  @Advice.OnMethodEnter
  public static void intercept(@Advice.This final PrintStream ps) {
   if (System.err.equals(ps)) {
    final ClassWriter classWriterInst;
    try {
     classWriterInst = NativeUtils.getInstanceFromStack(ClassWriter.class);
    } catch (Throwable t) {
     throw new RuntimeException(t);
    }
    if (PalAgent.getExcludedClassWriters().contains(classWriterInst)) {
     System.out.println("ERROR ON PRINTSTREAM: " + ps);
//     return;
    }
   }
  }
 }
}
