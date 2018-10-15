package io.github.matthewacon.pal.agent;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.jvm.ClassWriter;
import com.sun.tools.javac.main.JavaCompiler;
import io.github.matthewacon.pal.CompilerHooks;
import io.github.matthewacon.pal.util.ExceptionUtils;
import io.github.matthewacon.pal.util.NativeUtils;
import net.bytebuddy.asm.Advice;

import java.util.Objects;

public final class ClassWriterInterceptors {
 public static final class WriterCall {
  public final ClassWriter writer;
  public final Object[] arguments;

  public WriterCall(final ClassWriter writer, final Object[] parameters) {
   this.writer = writer;
   this.arguments = parameters;
  }

  @Override
  public boolean equals(final Object obj) {
   if (obj instanceof WriterCall) {
    final WriterCall compare = (WriterCall)obj;
    return
     this.writer.equals(compare.writer) &&
      //Method definitions are constant, however, across multiple compiler versions, they may not be
//      this.arguments.length == compare.arguments.length &&
      Objects.deepEquals(this.arguments, compare.arguments);
   }
   return false;
  }
 }

////  private static final LinkedList<WriterCall> calls;
//  public static final PriorityQueue<WriterCall> calls;
//
//  static {
//   calls = new PriorityQueue<>();
//  }

 public static WriterCall lastCall;

 //Interceptor for the ClassWriter#writeClass method
 public static class WriteClass {
  @Advice.OnMethodExit
  public static void intercept(@Advice.This ClassWriter writer) {
   if (!PalAgent.getExcludedClassWriters().contains(writer)) {
//    System.out.println("Intercepted the writeClass method in writer: " + writer);
    if (lastCall.writer.equals(writer)) {
//     CompilerHooks.INSTANCE.onClassWrite(writer, (Symbol.ClassSymbol) lastCall.arguments[1]);
     final int depth = NativeUtils.firstDepthOfClassOnStack(JavaCompiler.class);
     try {
      CompilerHooks
       .forInstance(NativeUtils.getInstanceFromStack(depth))
       .onClassWrite(writer, (Symbol.ClassSymbol)lastCall.arguments[1]);
     } catch (Exception e) {
      throw ExceptionUtils.initFatal(e);
     }
    }
   } /*TODO remove debug branch*/ else {
    System.out.println("INFO::ClassWriter#writeClass Interceptor skipped on instance: " + writer.hashCode());
   }
  }
 }

 //Breeder interceptor, passes finalized data from the ClassWriter#writeClassFile method to the interceptor for the
 //ClassWriter#writeClass method
 public static class WriteClassFile {
  @Advice.OnMethodExit
  public static void intercept(@Advice.This ClassWriter writer, @Advice.AllArguments Object[] args) {
   if (!PalAgent.getExcludedClassWriters().contains(writer)) {
    lastCall = new WriterCall(writer, args);
//    calls.add(new WriterCall(writer, args));
   } /*TODO remove debug branch*/ else {
    System.out.println("INFO::ClassWriter#writeClassFile Interceptor skipped on instance: " + writer.hashCode());
   }
  }
 }
}
