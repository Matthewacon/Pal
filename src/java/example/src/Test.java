//import net.bytebuddy.agent.ByteBuddyAgent;
//import sun.reflect.metaAnnotation.AnnotationParser;
//
//import java.io.File;
//import java.lang.metaAnnotation.Annotation;
//import java.lang.instrument.ClassDefinition;
//import java.lang.instrument.Instrumentation;
//import java.lang.reflect.Field;
//import java.nio.file.Files;
//import java.util.jar.JarFile;
//
//@OnEvent({ExampleEventEnum.EVENT_ONE, ExampleEventEnum.EVENT_TWO})
//@OnEvent({ExampleEventEnum.EVENT_ONE, ExampleEventEnum.EVENT_FIVE, ExampleEventEnum.EVENT_THREE})
//public class Test {
// static {
//  long start = System.nanoTime();
//  //Set custom classloader as the highest in the parent hierarchy
//  final PalClassLoader tcl = new PalClassLoader(null);
//  try {
//   final Field parentCL = ClassLoader.class.getDeclaredField("parent");
//   parentCL.setAccessible(true);
//   ClassLoader parentLoader = ClassLoader.getSystemClassLoader();
//   while(parentLoader.getParent() != null) parentLoader = parentLoader.getParent();
//   parentCL.set(parentLoader, tcl);
//  } catch (Throwable t) {
//   RuntimeException re = new RuntimeException();
//   re.initCause(t);
//   throw re;
//  }
//
//  //Transform the AnnotationParser class to recognize inheritance
//  ByteBuddyAgent.install();
//  try {
//   Instrumentation instrumentation = ByteBuddyAgent.getInstrumentation();
//   instrumentation.appendToBootstrapClassLoaderSearch(
//     new JarFile(System.getProperty("user.dir")+"/TestJar.jar")
//   );
//   final byte[] newAnnotationParser = Files.readAllBytes(new File(System.getProperty("user.dir") + "/AnnotationParser.class").toPath());
//   instrumentation.redefineClasses(new ClassDefinition(AnnotationParser.class, newAnnotationParser));
//  } catch (Throwable t) {
//   RuntimeException re = new RuntimeException();
//   re.initCause(t);
//   throw re;
//  }
////  DynamicType.Loaded<AnnotationParser> ap = new ByteBuddy()
////   .redefine(AnnotationParser.class)
////   .method(named("parseEnumValue"))
////   .intercept(MethodDelegation.to(AnnotationParserDispatch.ParseEnumValue.class))
////   .method(named("parseArray"))
////   .intercept(MethodDelegation.to(AnnotationParserDispatch.ParseArray.class))
////   .make()
////   .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
//  System.out.println("<sinit> took: " + (System.nanoTime()-start));
// }
//
// public static void main(String[] args) {
//  long start = System.nanoTime();
//  for (Annotation ann : Test.class.getAnnotations()) {
//   if (ann instanceof OnEvents) {
//    for (OnEvent ann2 : ((OnEvents)ann).value()) {
//     for (IEvent event : ann2.value()) {
//      System.out.println(event);
//     }
//    }
//   }
//   System.out.println(ann);
//  }
//  System.out.println("Duration: " + (System.nanoTime()-start));
// }
//}