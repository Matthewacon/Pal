package io.github.matthewacon.pal.util;

import java.util.function.Consumer;

import io.github.matthewacon.pal.util.LambdaUtils.RCase.CaseFunction;

//TODO document
//TODO exception handling, trim internal LambdaUtils functions from the stack
public final class LambdaUtils {
 private LambdaUtils() {}

 public static class Case<T> {
  public final Class<T> clazz;

  public Case(final Class<T> clazz) {
   this.clazz = clazz;
  }
 }

 public static class VCase<T> extends Case<T> {
  public final Consumer<? super T> consumer;

  public VCase(final Class<T> clazz, final Consumer<? super T> consumer) {
   super(clazz);
   this.consumer = consumer;
  }

  public void accept(final Object o) {
   this.consumer.accept((T)o);
  }

  public boolean isInstance(final Object o) {
   if (o == null || clazz == null) {
    return o == clazz;
   }
   return clazz.isInstance(o);
  }
 }

 public static class RCase<T, R> extends Case<T> {
  public interface CaseFunction<T, R> {
   R accept(final T t);
  }

  public final RCase.CaseFunction<? super T, R> caseFunction;

  public RCase(Class<T> clazz, RCase.CaseFunction<? super T, R> caseFunction) {
   super(clazz);
   this.caseFunction = caseFunction;
  }

  public R accept(final Object o) {
   return this.caseFunction.accept((T)o);
  }

  public boolean isInstance(final Object o) {
   if (o == null || clazz == null) {
    return o == clazz;
   }
   return clazz.isInstance(o);
  }
 }

 public static <C> VCase<C> vcase(Class<C> clazz, Consumer<? super C> ccase) {
  return new VCase<>(clazz, ccase);
 }

 public static <T> void vswitch(T target, VCase<?>... cases) {
  VCase<?> defaultCase = vcase(null, inst -> {});
  for (final VCase<?> esac : cases) {
   if (esac.clazz == null) {
    defaultCase = esac;
    break;
   }
  }
  for (int i = 0; i < cases.length; i++) {
   final VCase<?> esac = cases[i];
   if (esac.isInstance(target)) {
    esac.accept(target);
    break;
   } else if (i == (cases.length-1)) {
    defaultCase.accept(target);
    break;
   }
  }
 }

 public static <C, R> RCase<C, R> rcase(Class<C> clazz, CaseFunction<? super C, R> ccase) {
  return new RCase<>(clazz, ccase);
 }

 public static <T, R> R rswitch(T target, RCase<?, R>... rcases) {
  RCase<?, R> defaultRCase = rcase(null, inst -> null);
  for (final RCase<?, R> esac : rcases) {
   if (esac.clazz == null) {
    defaultRCase = esac;
    break;
   }
  }
  R result = null;
  for (int i = 0; i < rcases.length; i++) {
   final RCase<?, R> esac = rcases[i];
   if (esac.isInstance(target)) {
    result = esac.accept(target);
    break;
   }
  }
  if (result == null) {
   result = defaultRCase.accept(target);
  }
  return result;
 }

 //TODO remove
 @Deprecated
 public static final class Wrapper<T> {
  private T t;
  private final boolean mutable;

  public Wrapper(final T t, boolean mutable) {
   this.t = t;
   this.mutable = mutable;
  }

  public T unwrap() {
   return this.t;
  }

  public void wrap(final T t) {
   if (mutable) {
    this.t = t;
   } else {
    throw new IllegalArgumentException("Wrapper is immutable!");
   }
  }
 }
}