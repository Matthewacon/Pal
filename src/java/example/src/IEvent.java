import io.github.matthewacon.pal.api.annotations.bytecode.Enum;

@Enum
public interface IEvent<T extends java.lang.Enum<T> & IEvent<T>> {}