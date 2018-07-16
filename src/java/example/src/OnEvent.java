import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Repeatable(OnEvents.class)
//@MemberValue(Class<? extends IEvent>.class)
public @interface OnEvent {
// Class<? extends IEvent> event();
}