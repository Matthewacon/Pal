import java.lang.annotation.Annotation;
$IMPORTS
public final class $NAME implements $INTERFACE {
 @Override
 public Class<? extends Annotation> annotationType() {
  return $INTERFACE.class;
 }

 $METHOD_STUB
}