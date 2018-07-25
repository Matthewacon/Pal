//import net.bytebuddy.ByteBuddy;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.InputStream;
//import java.net.URL;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.LinkedList;
//import net.bytebuddy.ByteBuddy;
//import net.bytebuddy.agent.ByteBuddyAgent;
//import net.bytebuddy.dynamic.DynamicType;
//import net.bytebuddy.implementation.LoadedTypeInitializer;
//import net.bytebuddy.implementation.MethodDelegation;
//import net.bytebuddy.matcher.ElementMatchers;
//
//import java.io.File;
//import java.lang.instrument.ClassDefinition;
//import java.nio.file.Files;

import io.github.matthewacon.pal.api.annotations.bytecode.Enum;
import io.github.matthewacon.pal.api.annotations.sourcecode.Literal;

@Literal(target = "SC", replacement = "static class")
@Enum
public class Testing {
 public static class Test {
// public SC Test {
  public static void test() {
   System.out.println("Test test");
  }
 }

 public static void main(String[] args) throws Throwable {
//  LoadedTypeInitializer.Compound compound = new LoadedTypeInitializer.Compound();
//  compound.onLoad(Test.class);
//  System.out.println("Testing!");
//  ByteBuddyAgent.install();
//  DynamicType.Builder<Example> example = new ByteBuddy().rebase(Example.class);
//  DynamicType.Builder.MethodDefinition.ImplementationDefinition<Example> invokable = example.invokable(ElementMatchers.isTypeInitializer());
//  System.out.println(invokable);
//  invokable.intercept(MethodDelegation.to(Test.class));
//  ByteBuddyAgent
//   .getInstrumentation()
//   .redefineClasses(new ClassDefinition(Example.class, example.make().getBytes()));
//  Files.write(new File(System.getProperty("user.dir") + "/Example.class").toPath(), example.make().getBytes());
//  Example test = new Example();
 }
}
