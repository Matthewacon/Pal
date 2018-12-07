package io.github.matthewacon.pal.util;

import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.Context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.github.matthewacon.pal.PalMain;
import io.github.matthewacon.pal.SymbolNotFoundException;
import io.github.matthewacon.pal.agent.PalAgent;

import static io.github.matthewacon.pal.util.LambdaUtils.*;
import static io.github.matthewacon.pal.util.ArrayUtils.*;

//For self-annotated annotation definitions, process all but the target annotation, compile the target annotation
//and then process the annotations matching the target type (no need to recompile the previous annotations)
public final class RuntimeAnnotationGenerator {
 private static final Field JavaCompiler_parserFactory;

 private static final String
  ANNOTATION_IMPL_URL = "META-INF/class_templates/AnnotationImpl.java",
  ANONYMOUS_ANNOTATION_IMPL_URL = "META-INF/class_templates/AnonymousAnnotationImpl.java",
  ANNOTATION_METHOD_IMPL_URL = "META-INF/class_templates/AnnotationMethodImpl.java",
  IMPORT_IMPL_URL = "META-INF/class_templates/ImportImpl.java";

 public static final String
  ANNOTATION_IMPL_STUB,
  ANONYMOUS_ANNOTATION_IMPL_STUB,
  ANNOTATION_METHOD_IMPL_STUB,
  IMPORT_IMPL_STUB;

 static {
  try {
   JavaCompiler_parserFactory = JavaCompiler.class.getDeclaredField("parserFactory");
   JavaCompiler_parserFactory.setAccessible(true);
   ANNOTATION_IMPL_STUB = new String(IOUtils.readResource(ANNOTATION_IMPL_URL));
   ANONYMOUS_ANNOTATION_IMPL_STUB = new String(IOUtils.readResource(ANONYMOUS_ANNOTATION_IMPL_URL));
   ANNOTATION_METHOD_IMPL_STUB = new String(IOUtils.readResource(ANNOTATION_METHOD_IMPL_URL));
   IMPORT_IMPL_STUB = new String(IOUtils.readResource(IMPORT_IMPL_URL));
  } catch (Throwable t) {
   throw new RuntimeException(t);
  }
 }

 public static final class AnnotationInfo<T extends Annotation> {
  public static final class AnnotationProperty {
   public final Class<?> propertyType;
   public final String propertyName;
   public final Object defaultValue;

   public AnnotationProperty(final Class<?> propertyType, final String propertyName, final Object defaultValue) {
    this.propertyType = propertyType;
    this.propertyName = propertyName;
    this.defaultValue = defaultValue;
   }

   public boolean hasDefaultValue() {
    return defaultValue != null;
   }
  }

  public final Class<T> annotationType;
  private final AnnotationProperty[] properties;

  public AnnotationInfo(final Class<T> annotation) {
   this.annotationType = annotation;
   try {
    final Method[] declaredMethods = annotation.getDeclaredMethods();
    properties = new AnnotationProperty[declaredMethods.length];
    for (int i = 0; i < declaredMethods.length; i++) {
     final Method method = declaredMethods[i];
     properties[i] = new AnnotationProperty(
      method.getReturnType(),
      method.getName(),
      method.getDefaultValue()
     );
    }
   } catch(Throwable t) {
    //TODO exception handling
    throw new RuntimeException(t);
   }
  }

  public AnnotationProperty[] getProperties() {
   return Arrays.copyOf(properties, properties.length);
  }

  public boolean containsProperty(final String name) {
   for (final AnnotationProperty property : properties) {
    if (property.propertyName.equals(name)) {
     return true;
    }
   }
   return true;
  }

  public boolean containsProperty(final Class<?> clazz) {
   for (final AnnotationProperty property : properties) {
    if (property.propertyType.equals(clazz)) {
     return true;
    }
   }
   return false;
  }


  //TODO clean up
  public boolean matches(final JCAnnotation annotation, final GeneratorContext context) {
   final List<JCExpression> args = annotation.args;
   final AnnotationInfo selfRef = this;
   AnnotationProperty value = null;
   final LinkedList<AnnotationProperty> nonDefault = new LinkedList<>();
   //Find value property
   for (int i = 0; i < properties.length; i++) {
    if (properties[i].propertyName.equals("value")) {
     value = properties[i];
     break;
    }
   }
   //Populate nonDefault list
   for (final AnnotationProperty property : properties) {
    if (property.defaultValue == null) {
     nonDefault.add(property);
    }
   }
   //Check for the presence of named arguments
   boolean containsNamedArguments = false;
   for (final JCExpression arg : args) {
    if (arg instanceof JCAssign) {
     containsNamedArguments = true;
     break;
    }
   }
   //If there are no named arguments then there can only be 1 unnamed argument, 'value'
   if (containsNamedArguments) {
    //Make sure that all non-default properties of the annotation are satisfied
    if ((properties.length - nonDefault.size()) <= args.size()) {
     final Wrapper<Boolean> matchWrapper = new Wrapper<>(true, true);
     for (final JCExpression arg : args) {
      vswitch(arg,
       vcase(JCAssign.class,
        jcAssign -> {
         if (!selfRef.containsProperty(jcAssign.lhs.toString())) {
          if (!selfRef.containsProperty(context.resolveType(jcAssign.rhs))) {
           matchWrapper.wrap(false);
          }
         }
        }
       ),
//       vcase(JCLiteral.class, unnamedArgumentCase(matchWrapper)),
//       vcase(JCNewArray.class, unnamedArgumentCase(matchWrapper)),
//       vcase(JCNewClass.class, unnamedArgumentCase(matchWrapper)),
//       vcase(JCAnnotation.class, unnamedArgumentCase(matchWrapper)),
       //TODO if the annotation values contain a named argument, the code is invalid (maybe throw an exception)
       vcase(null, cDefault -> matchWrapper.wrap(false))
      );
      //Break out of loop on first confirmed negative match
      if (!matchWrapper.unwrap()) {
       break;
      }
     }
     if (matchWrapper.unwrap()) {
      return true;
     }
    }
   } else {
    //Check for 'value' property in annotation
    if (value != null) {
     //Check whether or not the property has a default value
     //If there is a default value then the annotation may contain 0 or 1 argument(s) (default and override,
     //respectively).
     //If there is no default value for the 'value' property, then an argument must be provided. In this case the
     //argument and property types must be equivalent for the code to be valid
     if (value.defaultValue == null || args.size() > 0) {
      //Ensure that the argument type matches the annotation property type
      if (value.propertyType.equals(context.resolveType(args.get(0)))) {
       return true;
      }
     } else {
      //If no arguments were provided and the annotation contains a default value for the 'value' field, then the
      //annotation is valid, as the type of the default value will always match the property
      return true;
     }
    }
   }
   return false;
  }

  public <E extends JCExpression> int argumentIndex(final E arg, final GeneratorContext context) {
   for (int i = 0; i < properties.length; i++) {
    if (properties[i].equals(context.resolveType(arg))) {
     return i;
    }
   }
   return -1;
  }
 }

 //Utility class for a given compilation unit
 public static final class GeneratorContext {
  private final JCCompilationUnit unit;

  public GeneratorContext(final JCCompilationUnit unit) {
   this.unit = unit;
  }

  public List<String> generateImports() {
   return unit.defs
    .stream()
    .filter(JCImport.class::isInstance)
    .map(tree -> (JCImport)tree)
    .map(jcImport -> jcImport.qualid.toString())
    .collect(Collectors.toList());
  }

  public <E extends JCExpression> String expressionToString(final E arg, final E... lastArg) {
   final StringBuilder sb = new StringBuilder();
   vswitch(arg,
   //JCAnnotation case
   vcase(
    JCAnnotation.class,
    jcAnnotation -> {
     //lastArg[0] should always be the root annotation
     final AnnotationInfo annotationInfo = new AnnotationInfo(resolveType(lastArg[0]));
     final int argumentIndex = annotationInfo.argumentIndex(jcAnnotation, GeneratorContext.this);
     final String propertyName = annotationInfo.getProperties()[argumentIndex].propertyName;
     //Generate name and anonymous implementation of interface
     String code = ANONYMOUS_ANNOTATION_IMPL_STUB.replace(
       "$INTERFACE",
       CompilerUtils.getFullyQuantifiedAnnotationName(jcAnnotation)
     );
     //Generate methods
     for (final JCExpression argument : jcAnnotation.args) {
      code = code.replace(
       "$METHOD_STUB",
       expressionToString(argument, safeConcat(lastArg, jcAnnotation))
      );
     }
     //Remove trailing $METHOD_STUB stub
     code = code.replace("$METHOD_STUB", "");
     sb.append(ANNOTATION_METHOD_IMPL_STUB
      .replace("$VALUE", code)
      .replace("$TYPE", CompilerUtils.getFullyQuantifiedAnnotationName(jcAnnotation))
      .replace("$NAME", propertyName)
     );
    }
   ),
    //JCAssign case
    vcase(JCAssign.class,
     jcAssign -> {
      sb.append(ANNOTATION_METHOD_IMPL_STUB
       .replace("$TYPE", resolveType(jcAssign).getCanonicalName())
       .replace("$NAME", expressionToString(jcAssign.lhs, safeConcat(lastArg, jcAssign)))
       .replace("$VALUE", expressionToString(jcAssign.rhs, safeConcat(lastArg, jcAssign)))
      );
     }
    ),
    //JCNewArray case
    vcase(JCNewArray.class,
     jcNewArray -> {
      final StringBuffer localBuffer = new StringBuffer();
      Class<?> type = resolveType(jcNewArray);
      final LinkedList<? extends JCExpression> elems = new LinkedList<>(jcNewArray.elems);
      localBuffer.append("new " + type.getCanonicalName() + " {");
      for (int i = 0; i < elems.size(); i++) {
       final JCExpression elem = elems.get(i);
       localBuffer.append(
        expressionToString(elem, safeConcat(lastArg, jcNewArray, elem)) +
        (i == elems.size() - 1 ? "" : ",")
       );
      }
      localBuffer.append("}");
      //If the array was passed as the default argument
      if (lastArg.length == 0) {
       sb.append(ANNOTATION_METHOD_IMPL_STUB
        .replace("$TYPE", type.getCanonicalName())
        .replace("$NAME", "value")
        .replace("$VALUE", localBuffer.toString())
       );
      } else {
       sb.append(localBuffer);
      }
      System.out.println();
     }
    ),
    //JCNewClass case
    vcase(JCNewClass.class,
     //TODO consider the default argument case
     jcNewClass -> sb.append(jcNewClass.toString())
    ),
    //JCIdent case
    //TODO consider the default argument case
    vcase(JCIdent.class, jcIdent -> sb.append(jcIdent.name)),
    //JCFieldAccess case
//   vcase(JCFieldAccess.class, jcFieldAccess -> sb.append(jcFieldAccess.toString())),
    //TODO consider the default argument case
    vcase(
     JCFieldAccess.class,
     jcFieldAccess -> {
      final Class<?> type = resolveType(jcFieldAccess.selected);
      sb.append(type.getCanonicalName() + "." + jcFieldAccess.name.toString());
     }),
    //JCLiteral case
    //TODO consider the default argument case
    vcase(JCLiteral.class, jcLiteral -> sb.append(jcLiteral.toString())),
    //Default case
    vcase(null,
     cDefault -> {
     throw new IllegalArgumentException("Invalid annotation parameter: '" + arg + "', type: '" + arg.getClass() + "'!");
//      System.err.println("Invalid annotation parameter: '" + arg + "', type: '" + arg.getClass() + "'!");
     }
    )
   );
   return sb.toString();
  }

  //TODO Cache map
  //Find parent annotation interface
  private <A extends Annotation> AnnotationInfo<A> getParentAnnotation(final JCAnnotation annotation) {
   final String
    baseName = CompilerUtils.getBaseName(annotation),
    qualifiedName = CompilerUtils.getFullyQuantifiedAnnotationName(annotation),
    pckage = unit.pid == null ? null : unit.pid.toString();
   final List<String> imports = generateImports();
   final HashSet<Class<?>> classCandidates = PalMain
    .PAL_CLASSLOADER
    .findClass(baseName, qualifiedName, pckage, imports);
   //Filter through the classCandidates to find the matching class candidate
   if (classCandidates.size() > 0) {
    Vector<Class<?>> copy;
    do {
     copy = new Vector<>(classCandidates);
     for (final Class<?> clazz : copy) {
      //Ensure that class is actually an annotation
      if (clazz.isAnnotation()) {
       final AnnotationInfo<A> annotationInfo = new AnnotationInfo<>((Class<A>)clazz);
       //Check arguments to see if they match annotation properties
       if (annotationInfo.matches(annotation, this)) {
        continue;
       }
      }
      classCandidates.remove(clazz);
     }
    } while (!classCandidates.containsAll(copy));
   }
   //If, after filtering, the number of candidates has not been reduced to 1, throw error
   if (classCandidates.size() == 0) {
    throw new SymbolNotFoundException("No candidate classes found for annotation '" + annotation + "'!");
   } else if (classCandidates.size() > 1) {
    //TODO Proper exception handling
    throw new SymbolNotFoundException("Multiple class candidates match the annotation: " + annotation);
   } else {
    return new AnnotationInfo<>((Class<A>)classCandidates.iterator().next());
   }
  }

  public <E extends JCExpression> Class<?> resolveType(final E expr) {
   final Class<?>[] clazz = new Class<?>[] {null};
   final String pckage = unit.pid != null ? unit.pid.toString() : null;
   final List<String> imports = generateImports();
   vswitch(expr,
    vcase(JCAssign.class, jcAssign -> clazz[0] = resolveType(jcAssign.rhs)),
    //TODO write test cases for JCLiteral
    vcase(
     JCLiteral.class,
     jcLiteal -> {
      final String sArg = jcLiteal.toString();
      if (sArg.indexOf("\"") != sArg.lastIndexOf("\"")) {
       clazz[0] = String.class;
      } else {
       throw new IllegalArgumentException("Unknown type of JCLiteral expression: '" + sArg + "'!");
      }
     }
    ),
    vcase(
     JCFieldAccess.class,
     jcFieldAccess -> {
      final String name = jcFieldAccess.selected.toString();
//       extension = jcFieldAccess.name.toString();
      clazz[0] = narrowCandidates(name, name, pckage, imports, jcFieldAccess);
//      if (extension.equals("class")) {
//       //TODO why are the imports null?
////       clazz[0] = narrowCandidates(name, name, null, null, jcFieldAccess);
//       clazz[0] = narrowCandidates(name, name, pckage, imports, jcFieldAccess);
//      } else {
//       throw new SymbolNotFoundException(
//        "Unknown field extension type: '" +
//         extension +
//         "' in JCFieldAccess: '" +
//         jcFieldAccess.toString() +
//         "'"
//       );
//      }
     }
    ),
    vcase(JCNewClass.class, jcNewClass -> {
     final String name = jcNewClass.clazz.toString();
     clazz[0] = narrowCandidates(name, name, null, imports, jcNewClass);
    }),
    vcase(JCAnnotation.class, jcAnnotation -> clazz[0] = getParentAnnotation(jcAnnotation).annotationType),
    vcase(JCNewArray.class, jcNewArray -> {
     final Class<?>[] candidates = new Class<?>[jcNewArray.elems.size()];
     for (int i = 0; i < jcNewArray.elems.size(); i++) {
      final JCExpression xpr = jcNewArray.elems.get(i);
      candidates[i] = resolveType(xpr);
     }
     //Ensure that all elements are of the same type
     for (int i = 0; i < candidates.length; i++) {
      for (int j = 0; j < candidates.length; j++) {
       if (i == j) {
        continue;
       }
       if (!candidates[i].equals(candidates[j])) {
        throw new SymbolNotFoundException(
         "Array element types are not congruent in expression: '" +
          jcNewArray.toString() +
          "'!"
        );
       }
      }
     }
     //If all classes match, simply take the class candidate at index 0
     try {
      if (candidates[0].isArray()) {
       //TODO switch over to {@link ClassUtils#countDims(Class)}
       final Pattern pattern = Pattern.compile("\\[\\]");
       final Matcher matcher = pattern.matcher(candidates[0].getCanonicalName());
       String dims = "[";
       while (matcher.find()) {
        dims += "[";
       }
       Class<?> baseComponentType = candidates[0];
       while ((baseComponentType = baseComponentType.getComponentType()).isArray());
       clazz[0] = Class.forName(dims + "L" + baseComponentType.getCanonicalName() + ";");
      } else {
       clazz[0] = Class.forName("[L" + candidates[0].getCanonicalName() + ";");
      }
     } catch (ClassNotFoundException e) {
      //This should never happen, but just in case
      //TODO appropriate error message
      throw new SymbolNotFoundException("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
     }
    }),
    vcase(
     JCIdent.class,
     jcIdent -> {
      final String name = jcIdent.toString();
      clazz[0] = narrowCandidates(name, name, pckage, imports, jcIdent);
     }
    ),
    vcase(
     null,
     cDefault -> {
      throw new IllegalArgumentException("Unexpected JCExpression: '" + expr + "', type: '" + expr.getClass() + "'");
     }
    )
   );
   return clazz[0];
  }

  private static <T extends JCExpression> Class<?> narrowCandidates(
   //Not nullable
   final String baseName,
   //Not nullable (same as baseName if unknown)
   final String qualifiedName,
   //Nullable
   final String pckage,
   //Nullable
   final List<String> imports,
   //Not nullable
   final T expr
  ) {
   final HashSet<Class<?>> candidates = PalMain.PAL_CLASSLOADER.findClass(baseName, qualifiedName, pckage, imports);
   if (candidates.size() == 0) {
    throw new SymbolNotFoundException(
     "No candidate classes found for JCExpression: '" +
      expr.toString() +
      "', of type: '" +
      expr.getClass().getCanonicalName() +
      "'!"
    );
   } else if (candidates.size() > 1) {
    throw new SymbolNotFoundException(
     "Multiple class candidates found for JCExpression: '" +
      expr.toString() +
      "', of type: '" +
      expr.getClass().getCanonicalName() +
      "'!"
    );
   } else {
    return candidates.iterator().next();
   }
  }
 }

 private final HashSet<String> registeredAnnotations;
 private final Context context;
 private final JavaCompiler compiler;
 private final ParserFactory parserFactory;

 public RuntimeAnnotationGenerator() {
  this.registeredAnnotations = new HashSet<>();
  this.context = new Context();
  this.compiler = JavaCompiler.instance(context);
  PalAgent.exclude(compiler, true, false);
  try {
   this.parserFactory = (ParserFactory)JavaCompiler_parserFactory.get(compiler);
  } catch (Throwable t) {
   throw new RuntimeException(t);
  }
 }

 //TODO implement
// public <T extends Annotation> Vector<T> generateAnnotations(final JCCompilationUnit unit) {
//  final GeneratorContext generatorContext = new GeneratorContext(unit);
//  final Vector<JCAnnotation> annotations = CompilerUtils.processAnnotations(unit);
//  return null;
// }

 /**Literal reference checking:
  * The using class must make reference, in one way or another, to the class literal that it is using. The references
  * may take take the form of:
  * 1. A direct import
  * 2. The fully quantified package path upon type use
  * 3. A package scope reference (named or unnamed), requiring only that the referenced class literal and the using
  *    class be in the same package
  */
 //TODO if the compilation unit produces an undefined reference error, then throw an appropriate exception
 public <T extends Annotation> T generateAnnotation(final JCCompilationUnit unit, final JCAnnotation annotation) {
  final GeneratorContext gc = new GeneratorContext(unit);
//  try {
//   final AnnotationInfo<? extends Annotation> annotationInfo = gc.getParentAnnotation(annotation);
//  } catch (SymbolNotFoundException e) {}
  final String baseName = CompilerUtils.getBaseName(annotation);
  //Add required imports
  String code = ANNOTATION_IMPL_STUB;
  for (final String iport : gc.generateImports()) {
   final String gen = IMPORT_IMPL_STUB.replaceAll("\\bimport [$]IMPORT\\b", "import " + iport);
   code = code.replace("$IMPORTS", gen);
  }
  //Remove trailing '$IMPORTS' stub
  code = code.replace("$IMPORTS", "");
  //Generate name and implement abstract method Annotation#annotationType
  code = code
   .replaceAll("[$]NAME", generateName(baseName))
   .replaceAll("[$]INTERFACE", CompilerUtils.getFullyQuantifiedAnnotationName(annotation));
  //Generate methods
  for (final JCExpression arg : annotation.args) {
   code = code.replace("$METHOD_STUB", gc.expressionToString(arg, annotation));
  }
  //Remove remaining "$METHOD_STUB"
  code = code.replace("$METHOD_STUB", "");
  final JavacParser parser = parserFactory.newParser(code, true, true, true);
  final JCCompilationUnit compilationUnit = parser.parseCompilationUnit();
  return null;
 }

 private String generateName(final String base) {
  final BigInteger randomNumber = new BigInteger(1, new BigInteger(64, new Random()).toByteArray());
  String name;
  while (!registeredAnnotations.add((name = base + "_impl_" + randomNumber)));
  return name;
 }
}
