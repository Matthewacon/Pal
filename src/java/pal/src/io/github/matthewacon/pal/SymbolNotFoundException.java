package io.github.matthewacon.pal;

public final class SymbolNotFoundException extends RuntimeException {
 public SymbolNotFoundException(String msg) {
  super(msg);
 }

 public SymbolNotFoundException(String msg, Throwable cause) {
  super(msg, cause);
 }
}
