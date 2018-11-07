package io.github.matthewacon.pal;

public final class MalformedExpressionException extends RuntimeException {
 public MalformedExpressionException(final String msg) {
  super(msg);
 }

 public MalformedExpressionException(final String msg, final Throwable cause) {
  super(msg, cause);
 }
}
