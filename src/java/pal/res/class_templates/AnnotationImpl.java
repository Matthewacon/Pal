import java.lang.annotation.Annotation;

$IMPORT;

public static final class $NAME implements $INTERFACE {
 @Override
 public Class<? extends Annotation> annotationType() {
  return $INTERFACE.class;
 }

 $METHOD_STUB
}