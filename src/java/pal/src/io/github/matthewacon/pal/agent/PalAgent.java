package io.github.matthewacon.pal.agent;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.jvm.ClassWriter;
import com.sun.tools.javac.util.AbstractLog;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.List;
import io.github.matthewacon.pal.ExceptionUtils;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import com.sun.tools.javac.main.JavaCompiler;
import org.jetbrains.annotations.NotNull;

import static net.bytebuddy.matcher.ElementMatchers.*;

public final class PalAgent {
 private static final Vector<JavaCompiler> compilerExclude;
 private static final Vector<ClassWriter> classWriterExclude;
 private static final Vector<AbstractLog> abstractLogExclude;

 private static final Method
  JavaCompiler_close,
  JavaCompiler_compile,
  ClassWriter_writeClass,
  ClassWriter_writeClassFile,
  AbstractLog_error,
  PrintStream_print;

 private static final Field
  JavaCompiler_writer,
  JavaCompiler_log;

 static {
  try {
   //Initialize exclusion vectors
   compilerExclude = new Vector<>();
   classWriterExclude = new Vector<>();
   abstractLogExclude = new Vector<>();
   //Initialize all reflectively accessed methods
   JavaCompiler_close = JavaCompiler.class.getDeclaredMethod("close", boolean.class);
   JavaCompiler_close.setAccessible(true);
   JavaCompiler_compile = JavaCompiler.class.getDeclaredMethod("compile", List.class, List.class, Iterable.class);
   JavaCompiler_compile.setAccessible(true);
   ClassWriter_writeClass = ClassWriter.class.getDeclaredMethod("writeClass", Symbol.ClassSymbol.class);
   ClassWriter_writeClass.setAccessible(true);
   ClassWriter_writeClassFile = ClassWriter.class.getDeclaredMethod(
    "writeClassFile",
    OutputStream.class,
    Symbol.ClassSymbol.class
   );
   ClassWriter_writeClassFile.setAccessible(true);
   AbstractLog_error = AbstractLog.class.getDeclaredMethod(
    "error",
    JCDiagnostic.DiagnosticFlag.class,
    JCDiagnostic.DiagnosticPosition.class,
    String.class,
    Object[].class
   );
   AbstractLog_error.setAccessible(true);
   PrintStream_print = PrintStream.class.getDeclaredMethod("print", String.class);
   PrintStream_print.setAccessible(true);
   //Initialize all reflectively accessed fields
   JavaCompiler_writer = JavaCompiler.class.getDeclaredField("writer");
   JavaCompiler_writer.setAccessible(true);
   JavaCompiler_log = JavaCompiler.class.getDeclaredField("log");
   JavaCompiler_log.setAccessible(true);
  } catch (Throwable t) {
   throw ExceptionUtils.initFatal(t);
  }
 }

 private static Instrumentation instrumentation;

 public static void premain(final String args, final Instrumentation instrumentation) {
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
   //Intercept JavaCompiler#close(boolean)
   .transform((builder, typeDescription, classLoader, module) ->
     builder.visit(
//      Advice.to(JavaCompilerInterceptors.Close.class).on(named("close").and(takesArguments(boolean.class)))
      Advice.to(JavaCompilerInterceptors.Close.class).on(is(JavaCompiler_close))
     )
   )
   //Intercept compiler instantiation
   .transform((builder, typeDescription, classLoader, module) ->
    builder.visit(
     Advice.to(JavaCompilerInterceptors.Construct.class).on(isConstructor())
    )
   )
   //Intercept JavaCompiler#compile(List<JavaFileObject>, List<String>, Iterable<? extends Processor>)
   .transform((builder, typeDescription, classLoader, module) ->
    builder.visit(
     Advice.to(JavaCompilerInterceptors.Compile.class).on(is(JavaCompiler_compile))
    )
   )
   .installOn(instrumentation);

  //Transform ClassWriter class
  base
   .type(named(ClassWriter.class.getName()))
   //Intercept ClassWriter#writeClassFile
   .transform((builder, typeDescription, classLoader, module) ->
    builder.visit(
//     Advice.to(ClassWriterInterceptors.WriteClassFile.class).on(named("writeClassFile"))
     Advice.to(ClassWriterInterceptors.WriteClassFile.class).on(is(ClassWriter_writeClassFile))
    )
   )
   //Intercept ClassWriter#writeClass
   .transform((builder, typeDescription, classLoader, module) ->
    builder.visit(
//     Advice.to(ClassWriterInterceptors.WriteClass.class).on(named("writeClass"))
     Advice.to(ClassWriterInterceptors.WriteClass.class).on(is(ClassWriter_writeClass))
    )
   )
   .installOn(instrumentation);

  //Transform AbstractLog class
  base
   .type(named(AbstractLog.class.getName()))
   //Intercept error
   .transform((builder, typeDescription, classLoader, module) ->
    builder.visit(
     Advice.to(AbstractLogInterceptors.Error.class).on(is(AbstractLog_error))
    )
   )
   .installOn(instrumentation);

  //TODO Mute disposable compiler output
//  //Transform PrintStream#print(String)
//  base
//   .type(named(PrintStream.class.getName()))
//   .transform((builder, typeDescription, classLoader, module) ->
//    builder.visit(
//     Advice.to(PrintStreamInterceptors.Print.class).on(is(PrintStream_print))
//    )
//   )
//   .installOn(instrumentation);
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
    throw ExceptionUtils.initFatal(e);
   }
  });
  return PalAgent.instrumentation;
 }

 //TODO doc - Excludes a compiler instance from interception
 public static void exclude(@NotNull final JavaCompiler compiler, boolean excludeClassWriter, boolean excludeLog) {
  @NotNull final ClassWriter writer;
  @NotNull final AbstractLog log;
  try {
   compilerExclude.add(compiler);
   if (excludeClassWriter) {
    writer = (ClassWriter)JavaCompiler_writer.get(compiler);
    classWriterExclude.add(writer);
   }
   if (excludeLog) {
    log = (AbstractLog)JavaCompiler_log.get(compiler);
    abstractLogExclude.add(log);
   }
  } catch(Throwable t) {
   throw new RuntimeException(t);
  }
 }

 public static Vector<JavaCompiler> getExcludedCompilers() {
  return new Vector<>(compilerExclude);
 }

 public static Vector<ClassWriter> getExcludedClassWriters() {
  return new Vector<>(classWriterExclude);
 }

 public static Vector<AbstractLog> getExcludedAbstractLogs() {
  return new Vector<>(abstractLogExclude);
 }
}
