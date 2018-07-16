//package io.github.matthewacon.pal;
//
//import net.bytebuddy.ByteBuddy;
//import net.bytebuddy.dynamic.DynamicType;
//import net.bytebuddy.implementation.MethodDelegation;
//import org.objectweb.asm.Opcodes;
//
//import java.io.File;
//import java.lang.reflect.Modifier;
//import java.nio.file.Files;
//
//import static net.bytebuddy.matcher.ElementMatchers.named;
//
////@interface OnEvent<T extends Enum<T> & IEvent<T>> {
//// T event();
////}
//
////interface TESTING {
//// <T extends Enum<T> & IEvent<T>> T test();
////}
//
////@interface TESTING {
//// Enum<? extends Enum & IEvent> value();
////}
//
//public class AnnotationTransformer {
//// private interface IEvent {}
// public static void main(String[] args) throws Exception {
//  //OnEvent metaAnnotation with {T extends Enum<T> & IEvent<T>} T value
////  DynamicType.Unloaded<OnEvent> type = new ByteBuddy()
////   .redefine(OnEvent.class)
////   .typeVariable("T",
////    TypeDescription.Generic.Builder.parameterizedType(
////     new TypeDescription.ForLoadedType(Enum.class),
////     TypeDescription.Generic.Builder.typeVariable("T").build()).build(),
////    TypeDescription.Generic.Builder.parameterizedType(
////     new TypeDescription.ForLoadedType(IEvent.class),
////     TypeDescription.Generic.Builder.typeVariable("T").build()).build()
////   )
////   .defineMethod("value",
////    TypeDescription.Generic.Builder.typeVariable("T").build(),
////    Modifier.PUBLIC | Modifier.ABSTRACT)
////   .withoutCode()
////   .make();
//
//  //OnEvent metaAnnotation with Enum<? extends IEvent<?>> value
////  DynamicType.Unloaded<OnEvent> type2 = new ByteBuddy()
////   .redefine(OnEvent.class)
////   .defineMethod(
////    "value",
////     TypeDescription.Generic.Builder.parameterizedType(
////      TypeDescription.ForLoadedType.of(Enum.class),
////      TypeDescription.Generic.Builder.parameterizedType(
////       TypeDescription.ForLoadedType.of(IEvent.class),
////       TypeDescription.Generic.Builder.unboundWildcard()
////      ).asWildcardUpperBound()
////     ).build(),
////       Modifier.PUBLIC | Modifier.ABSTRACT
////    ).withoutCode().make();
//
//  //OnEvent metaAnnotation with ServerEvent[] value
////  DynamicType.Unloaded<OnEvent> type = new ByteBuddy()
////      .redefine(OnEvent.class)
////      .defineMethod(
////          "value",
////          TypeDescription.ForLoadedType.of(ServerEvent[].class),
////          Modifier.PUBLIC | Modifier.ABSTRACT
////      ).withoutCode().make();
//
//  //Redefine IEvent to be an enum
//  DynamicType.Builder<?> test = new ByteBuddy()
//   .redefine(IEvent.class)
//   .modifiers(Opcodes.ACC_ENUM | Modifier.PUBLIC | Modifier.ABSTRACT | Modifier.INTERFACE);
//
////  DynamicType.Builder eventInterface = test
////   .modifiers(Opcodes.ACC_ENUM | Modifier.PUBLIC | Modifier.INTERFACE);
//
////  test = test.modifiers(Opcodes.ACC_ENUM | Opcodes.ACC_PUBLIC);
//
//  //OnEvent metaAnnotation with IEvent value
//  DynamicType.Unloaded<OnEvent> type2 = new ByteBuddy()
//   .redefine(OnEvent.class)
////   .modifiers(Modifier.PUBLIC)
//   .defineMethod(
//    "value",
////    TypeDescription.ForLoadedType.of(IEvent.class),
////    eventInterface.make().getTypeDescription(),
////    test.make().getTypeDescription(),
//    IEvent[].class,
//    Modifier.PUBLIC | Modifier.ABSTRACT
//   ).withoutCode().make();
//
////  DynamicType.Builder<?> testing = new ByteBuddy()
//////   .subclass(eventInterface.make().getTypeDescription())
////   .redefine(ExampleEventEnum.class)
////   .implement(test.make().getTypeDescription())
//////   .modifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_ENUM)
////   .name("ExampleEventEnum");
//
////  DynamicType.Unloaded<?> type = testing
////   .defineField("TEST_ENUM", testing.make().getTypeDescription(), Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL | Opcodes.ACC_ENUM)
////   .make();
//
//  //TestEvent
////  DynamicType.Builder typebuilder = new ByteBuddy()
////   .makeEnumeration("SERVER_BOUND")
////   .name("Gay");
////   .subclass(Enum.class)
////   .implement(TypeDescription.ForLoadedType.of(IEvent.class))
////   .name("TestEvent");
////  typebuilder = typebuilder.defineField("SERVER_BOUND", typebuilder.make().getTypeDescription(), Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
////  DynamicType.Unloaded type = typebuilder.make();
////  DynamicType.Unloaded<?> testing = new ByteBuddy()
////   .makeInterface()
////   .implement(type.getTypeDescription())
////   .make();
//
//  //Redefine sun's AnnotationParser (dynamic dispatch to custom impl)
//  DynamicType.Unloaded<AnnotationParser> ap = new ByteBuddy()
//   .redefine(AnnotationParser.class)
//   .method(named("parseEnumValue"))
//   .intercept(MethodDelegation.to(AnnotationParserDispatch.ParseEnumValue.class))
//   .method(named("parseArray"))
//   .intercept(MethodDelegation.to(AnnotationParserDispatch.ParseArray.class))
//   .make();
//
//  Files.write(new File("OnEvent.class").toPath(), type2.getBytes());
//  Files.write(new File("IEvent.class").toPath(), test.make().getBytes());
//  Files.write(new File("AnnotationParser.class").toPath(), ap.getBytes());
////  Files.write(new File("ExampleEventEnum.class").toPath(), type.getBytes());
////  Files.write(new File("ExampleEventEnum.class").toPath(), testing.make().getBytes());
// }
//}
