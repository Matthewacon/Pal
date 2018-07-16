package io.github.matthewacon.pal.api;

import java.lang.annotation.Annotation;

public final class PalClass<T> {
 private final Class<T> clazz;
 private final Annotation[] annotations;
 private final byte[] bytecode;

 public PalClass() {
  this.clazz = null;
  this.annotations = null;
  this.bytecode = null;
 }
}
