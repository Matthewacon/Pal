package io.github.matthewacon.pal;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.jvm.ClassWriter;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.*;

import com.sun.tools.javac.main.JavaCompiler;

import static net.bytebuddy.matcher.ElementMatchers.*;

public final class PalAgent {
 public static final class JavaCompilerInterceptors {
  public static final class Construct {
   @Advice.OnMethodExit
   public static void intercept(@Advice.This JavaCompiler compiler) {
    new CompilerHooks(compiler);
   }
  }

  public static final class Close {
   @Advice.OnMethodEnter
   public static void intercept(@Advice.This JavaCompiler compiler) {
    CompilerHooks
     .forInstance(compiler)
     .onCompilerClosed();
   }
  }
 }

 public static final class ClassWriterInterceptors {
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
//    System.out.println("Intercepted the writeClass method in writer: " + writer);
    if (lastCall.writer.equals(writer)) {
//     CompilerHooks.INSTANCE.onClassWrite(writer, (Symbol.ClassSymbol) lastCall.arguments[1]);
     final int depth = NativeUtils.firstInstanceOfClassOnStack(JavaCompiler.class);
     try {
      CompilerHooks
       .forInstance(NativeUtils.getInstanceFromStack(depth))
       .onClassWrite(writer, (Symbol.ClassSymbol)lastCall.arguments[1]);
     } catch (Exception e) {
      throw ExceptionUtils.initFatal(e);
     }
    }
   }
  }

  //Breeder interceptor, passes finalized data from the ClassWriter#writeClassFile method to the interceptor for the
  //ClassWriter#writeClass method
  public static class WriteClassFile {
   @Advice.OnMethodExit
   public static void intercept(@Advice.This ClassWriter writer, @Advice.AllArguments Object[] args) {
    lastCall = new WriterCall(writer, args);
//    calls.add(new WriterCall(writer, args));
   }
  }
 }

 private static Instrumentation instrumentation;

 public static void premain(final String args, final Instrumentation instrumentation) throws Exception {
  PalAgent.instrumentation = instrumentation;
  final AgentBuilder.Ignored base = new AgentBuilder.Default()
//   .with(new ByteBuddy().with(Implementation.Context.Default.Factory.INSTANCE))
//   .with(AgentBuilder.Listener.StreamWriting.toSystemOut())
   .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
   .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
   .with(AgentBuilder.TypeStrategy.Default.REBASE)
//   .enableBootstrapInjection(instrumentation, PalResources.TEMP_DIR)
   .ignore(none());

  //Transform JavaCompiler class
  base
   .type(named(JavaCompiler.class.getName()))
   //Intercepting the close method
   .transform((builder, typeDescription, classLoader, module) ->
     builder.visit(
      Advice.to(JavaCompilerInterceptors.Close.class).on(named("close").and(takesArguments(boolean.class)))
     )
   )
   //Intercepting compiler instantiation
   .transform((builder, typeDescription, classLoader, module) ->
    builder.visit(
     Advice.to(JavaCompilerInterceptors.Construct.class).on(isConstructor())
    )
   )
   .installOn(instrumentation);

  //Transform ClassWriter#writeClassFile
  base
   .type(named(ClassWriter.class.getName()))
   .transform((builder, typeDescription, classLoader, module) ->
    builder.visit(
     Advice.to(ClassWriterInterceptors.WriteClassFile.class).on(named("writeClassFile"))
    )
   )
   .installOn(instrumentation);

  //Transform ClassWriter#writeClass
  base
   .type(named(ClassWriter.class.getName()))
   .transform((builder, typeDescription, classLoader, module) ->
    builder.visit(
     Advice.to(ClassWriterInterceptors.WriteClass.class).on(named("writeClass"))
    )
   )
   .installOn(instrumentation);

 }

 public static Instrumentation getInstrumentation() {
  final Vector<Field> agentsToSet = new Vector<>();
  if (PalAgent.instrumentation == null) {
   ClassLoader parent = PalAgent.class.getClassLoader();
//   System.out.println("PalAgent classloader: " + parent);
   do {
    final Class<PalAgent> clazz;
    final Field f_instrumentation;
    try {
     try {
      clazz = (Class<PalAgent>) parent.loadClass(PalAgent.class.getName());
     } catch (ClassNotFoundException e) {
      parent = parent.getParent();
      continue;
     }
     f_instrumentation = clazz.getDeclaredField("instrumentation");
     f_instrumentation.setAccessible(true);
     final Instrumentation instrumentation = (Instrumentation)f_instrumentation.get(null);
     if (instrumentation == null) {
      agentsToSet.add(f_instrumentation);
      parent = parent.getParent();
//      continue;
     } else {
      //No break here - iteration should continue to search for class instances of PalAgent that do not contain a
      //configured instrumentation. This branch should, however, never be executed twice for different instances of
      //java.lang.instrument.Instrumentation. If that is the case, then the premain has been executed twice, from
      //different classloaders and with different Instrumentation instances.
      if (PalAgent.instrumentation != null &&
       instrumentation != null &&
       !PalAgent.instrumentation.equals(instrumentation)) {
       throw new LinkageError(
        "The PalAgent premain has been run multiple times! The compiler need only be instrumented once. Make sure" +
         "you've properly configured your JVM compiler arguments!"
       );
      }
      //No need to reassign the instrumentation if they are identical references
      if (!PalAgent.instrumentation.equals(instrumentation)) {
       PalAgent.instrumentation = instrumentation;
      }
     }
    } catch (Throwable t) {
     ExceptionUtils.initFatal(t);
    }
   } while(parent != null && parent.getParent() != null);
  }
  //If no classloader contains a class instance of PalAgent where instrumentation is set
  if (PalAgent.instrumentation == null) {
   throw new LinkageError(
    "The PalAgent premain was never invoked! Are you sure you configured your compiler's JVM arguments correctly?"
   );
  }
  agentsToSet.forEach(field -> {
   try {
    field.set(null, PalAgent.instrumentation);
    field.setAccessible(false);
   } catch (IllegalAccessException e) {
    ExceptionUtils.initFatal(e);
   }
  });
  return PalAgent.instrumentation;
 }
}
