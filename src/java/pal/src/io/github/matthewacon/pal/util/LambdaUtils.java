package io.github.matthewacon.pal.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.function.Consumer;

//TODO document
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
   return clazz.isInstance(obj);
  }
 }

 public static <C> Case<C> ccase(Class<C> clazz, Consumer<? super C> ccase) {
  return new Case<>(clazz, ccase);
 }

 public static <T> void cswitch(T target, Case<?>... cases) {
  for (final Case<?> esac : cases) {
   if (esac.isInstance(target)) {
    esac.accept(target);
    break;
   }
  }
 }
}
