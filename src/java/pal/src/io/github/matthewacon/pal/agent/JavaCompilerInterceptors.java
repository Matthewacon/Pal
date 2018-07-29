package io.github.matthewacon.pal.agent;

import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.List;
import io.github.matthewacon.pal.CompilerHooks;
import net.bytebuddy.asm.Advice;

import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;

public final class JavaCompilerInterceptors {
 public static final class Construct {
  //No exclude guard required, would be executed before instantiation returns an instance anyways
  @Advice.OnMethodExit
  public static void intercept(@Advice.This final JavaCompiler compiler) {
   new CompilerHooks(compiler);
  }
 }

 public static final class Close {
  @Advice.OnMethodEnter
  public static void intercept(@Advice.This final JavaCompiler compiler) {
   if (!PalAgent.getExcludedCompilers().contains(compiler)) {
    CompilerHooks
     .forInstance(compiler)
     .onCompilerClosed();
   } /*TODO remove debug branch*/ else {
    System.out.println("INFO::JavaCompiler#close(boolean) Interceptor skipped on instance: " + compiler.hashCode());
   }
  }
 }

 public static final class Compile {
  @Advice.OnMethodEnter
  public static void intercept(@Advice.This final JavaCompiler compiler, @Advice.AllArguments final Object[] args) {
   if (!PalAgent.getExcludedCompilers().contains(compiler)) {
    CompilerHooks
     .forInstance(compiler)
     .onCompileStarted((List<JavaFileObject>)args[0], (List<String>)args[1], (Iterable<? extends Processor>)args[2]);
   } /*TODO remove debug branch*/ else {
    System.out.println("INFO::JavaCompiler#compile Interceptor skipped on instance: " + compiler.hashCode());
   }
  }
 }
}
