package io.github.matthewacon.pal.util;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ExceptionUtils {
 //TODO another time
// //TODO create a compiler plugin that generates disposable functional interfaces (function lambdas)
// public interface NonceBiFunction<T1, T2> {
//  void invoke(T1 t1, T2 t2);
// }
//
// //TODO create a compiler plugin that generates disposable Option types with handling cases for each type
//// public interface Optional {}
//
// //TODO Something like this but for vararg type arguments
//// public static abstract class OptionalImpl<T1, T2...> implements Optional {
////  abstract void accept(T1 t1);
////  abstract void accept(T2 t2);...
//// }
//
// public static class ExceptionHandler {
//  public interface ExceptionResponse {
//   NonceBiFunction<? extends Throwable, ? extends PrintStream> getAction();
//  }
//
//  public enum DefaultExceptionResponse implements ExceptionResponse {
//   IGNORE(),
//   WARNING(),
//   FATAL();
//  }
//
//  public enum ResponseType {
//   EXCEPTION, CALLBACK
//  }
//
//  private final ExceptionResponse fallthroughResponse;
//  private final PrintStream log;
//  //TODO Once option types and disposable functional interfaces are implemented
//  //private final
//  // LinkedHashMap<ExceptionResponse, Optional<Class<? extends Throwable>, Function<? extends Throwable>>> responseMap;
//  private final LinkedHashMap<Class<? extends Throwable>, ResponseType> responseTypeMap;
//  private final LinkedHashMap<Class<? extends Throwable>, ExceptionResponse> exceptionResponseMap;
//  private final LinkedHashMap<Class<? extends Throwable>, NonceBiFunction<? extends Throwable>> callbackResponseMap;
//
////  //Default exception responses
////  public final NonceBiFunction<? extends Throwable>
////   IGNORE = throwable -> {},
////   WARNING = throwable -> {},
////   FATAL = throwable -> {};
//
//  public ExceptionHandler(final NonceBiFunction<? extends Throwable> fallthroughResponse) {
//   this(
//    fallthroughResponse,
//    fallthroughResponse == IGNORE ? null : System.err
//   );
//  }
//
//  public ExceptionHandler(final ExceptionResponse fallthroughResponse, final PrintStream log) {
//   this.fallthroughResponse = fallthroughResponse;
//   this.log = log;
//   this.responseTypeMap = new LinkedHashMap<>();
//   this.exceptionResponseMap = new LinkedHashMap<>();
//   this.callbackResponseMap = new LinkedHashMap<>();
//  }
//
////  public ResponseType
//
//  public void registerSpecialCase(@NotNull final ExceptionResponse response,
//                                  @NotNull final Class<? extends Throwable>... exceptions) {
//
//  }
//
//  public <T extends Throwable> void registerSpecialCase(@NotNull final NonceBiFunction<? extends Throwable> callback,
//                                                        @NotNull final Class<? extends Throwable>... exceptions) {
//
//  }
//
//  public void expungeSpecialCase(@NotNull final ExceptionResponse response,
//                                 @NotNull final Class<? extends Throwable>... exceptions) {
//
//  }
//
//  public <T extends Throwable> void expungeSpecialCase(@NotNull final NonceBiFunction<? extends Throwable> callback,
//                                                       @NotNull final Class<? extends Throwable>... exceptions) {
//
//  }
//
//  private void internalHandle(final Throwable t) {
//
//  }
//
//  public void handle(final Throwable t) {
//   switch(responseTypeMap.get(t.getClass())) {
//    case EXCEPTION: {
//
//     break;
//    }
//    case CALLBACK: {
//
//     break;
//    }
//   }
//  }
// }

 //TODO another time
// public static StackTraceElement[] trimLeadingClasses(final StackTraceElement[] trace, final String... classRegexes) {
//
// }

 //TODO turn into an annotation
 @Deprecated
 public static RuntimeException initFatal(Throwable t) {
  return new RuntimeException(t) {
   //TODO
//   private void scrubTrace() {
//    StackTraceElement[] newTrace;
//
//   }
   @Override
   public void printStackTrace() {
    getCause().printStackTrace();
   }
   @Override
   public void printStackTrace(final PrintStream ps) {
    getCause().printStackTrace(ps);
   }
  };
 }

 public static final class ReflectionHandler<T> {
  private final T t;

  public ReflectionHandler(T t) {
   this.t = t;
  }

  public <R> R fatalIfErrorInvoke(final Method method, final Object... arguments) {
   try {
    return (R)method.invoke(t, arguments);
   } catch(Throwable t) {
    throw ExceptionUtils.initFatal(t);
   }
  }

  public <R> R fatalIfErrorGetField(final Field field) {
   try {
    return (R)field.get(t);
   } catch(Throwable t) {
    throw ExceptionUtils.initFatal(t);
   }
  }
 }
}
