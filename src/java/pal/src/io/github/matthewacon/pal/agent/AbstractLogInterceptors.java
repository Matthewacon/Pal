package io.github.matthewacon.pal.agent;

import com.sun.tools.javac.util.AbstractLog;
import com.sun.tools.javac.util.DiagnosticSource;
import io.github.matthewacon.pal.util.ExceptionUtils;
import net.bytebuddy.asm.Advice;

import java.lang.reflect.Field;

public final class AbstractLogInterceptors {
 public static final class Error {
  public static final Field AbstractLog_source;

  static {
   try {
    AbstractLog_source = AbstractLog.class.getDeclaredField("source");
    AbstractLog_source.setAccessible(true);
   } catch (Throwable t) {
    throw ExceptionUtils.initFatal(t);
   }
  }

  //TODO Mute disposable compiler output
  @Advice.OnMethodEnter
  public static void intercept(@Advice.This final AbstractLog log) {
   if (!PalAgent.getExcludedAbstractLogs().contains(log)) {
    try {
     if (AbstractLog_source.get(log) == null) {
      AbstractLog_source.set(log, DiagnosticSource.NO_SOURCE);
     }
    } catch (Throwable t) {
     throw ExceptionUtils.initFatal(t);
    }
   } /*TODO remove debug branch*/ else {
    System.out.println("INFO::AbstractLog Interceptor skipped on instance: " + log.hashCode());
   }
  }
 }
}
