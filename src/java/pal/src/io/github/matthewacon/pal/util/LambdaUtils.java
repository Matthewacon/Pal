package io.github.matthewacon.pal.util;

import java.util.function.Consumer;

//TODO document
//TODO exception handling, trim internal LambdaUtils functions from the stack
public final class LambdaUtils {
 public static class Case<T> {
  public final Class<T> clazz;
  public final Consumer<? super T> consumer;

  public Case(Class<T> clazz, Consumer<? super T> consumer) {
   this.clazz = clazz;
   this.consumer = consumer;
  }

  public void accept(final Object obj) {
   this.consumer.accept((T)obj);
  }

  public boolean isInstance(final Object obj) {
   if (obj == null || clazz == null) {
    return obj == clazz;
   }
   return clazz.isInstance(obj);
  }
 }

 public static <C> Case<C> ccase(Class<C> clazz, Consumer<? super C> ccase) {
  return new Case<>(clazz, ccase);
 }

 public static <T> void cswitch(T target, Case<?>... cases) {
  Case<?> defaultCase = ccase(null, inst -> {});
  for (final Case<?> esac : cases) {
   if (esac.clazz == null) {
    defaultCase = esac;
    break;
   }
  }
  for (int i = 0; i < cases.length; i++) {
   final Case<?> esac = cases[i];
   if (esac.isInstance(target)) {
    esac.accept(target);
    break;
   } else if (i == (cases.length-1)) {
    defaultCase.accept(target);
    break;
   }
  }
 }

 //Simple object wrapper
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
