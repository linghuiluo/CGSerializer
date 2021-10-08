package cgs.stats;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Callee {

  protected String className;
  protected String methodName;
  protected Type type;
  protected String apk;
  protected int flowID;
  protected int stepID;

  public Callee(String apk, String className, String methodName, Type t, int flowID) {
    this(apk, className, methodName, t, flowID, -1);
  }

  public Callee(String apk, String className, String methodName, Type t, int flowID, int stepID) {
    this.apk = apk;
    this.className = className;
    this.methodName = methodName;
    this.type = t;
    this.flowID = flowID;
    this.stepID = stepID;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append(className);
    b.append(": ");
    b.append(methodName);
    return b.toString();
  }

  protected static Pattern javaMethodPattern = createJavaMethodPattern();
  protected static Pattern javaConstructorPattern = createJavaConstructorPattern();
  protected static Pattern jimpleMethodPattern = createJimpleMethodPattern();

  public static Pattern createJavaMethodPattern() {
    // non capture modifier
    String group0 =
        "(?:public|private|protected|static|final|native|synchronized|abstract|transient|volatile)";
    String group1 = "(.+)"; // return type
    String group2 = "(.+)"; // method name
    String group3 = "(.*?)"; // parameters
    StringBuilder sb = new StringBuilder();
    sb.append(group0);
    sb.append("\\s+");
    sb.append(group1);
    sb.append("\\s+");
    sb.append(group2);
    sb.append("\\(");
    sb.append(group3);
    sb.append("\\)");
    String regex = sb.toString();
    Pattern p = Pattern.compile(regex);
    return p;
  }

  public static Pattern createJavaConstructorPattern() {
    String group0 = "(public|private|protected|static)"; // modifier
    String group1 = "(.+)"; // constructor name
    String group2 = "(.*?)"; // parameters
    StringBuilder sb = new StringBuilder();
    sb.append(group0);
    sb.append("\\s+");
    sb.append(group1);
    sb.append("\\(");
    sb.append(group2);
    sb.append("\\)");
    String regex = sb.toString();
    Pattern p = Pattern.compile(regex);
    return p;
  }

  public static Pattern createJimpleMethodPattern() {
    // non capture class signature
    String group0 = "(.+)";
    String group1 = "(.+)"; // return type
    String group2 = "(.+)"; // method name
    String group3 = "(.*?)"; // parameter types
    StringBuilder sb = new StringBuilder("<");
    sb.append(group0);
    sb.append(":\\s+");
    sb.append(group1);
    sb.append("\\s+");
    sb.append(group2);
    sb.append("\\(");
    sb.append(group3);
    sb.append("\\)>");
    String regex = sb.toString();
    Pattern p = Pattern.compile(regex);
    return p;
  }

  private static String replaceAllGenericTypes(String javaTypeString) {
    // replace all types between < > for generic types
    boolean start = false;
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < javaTypeString.length(); i++) {
      char c = javaTypeString.charAt(i);
      if (c == '<') start = true;
      if (!start) b.append(c);
      if (c == '>') start = false;
    }
    if (b.toString().equals("T")) return "Object";
    return b.toString();
  }

  public static boolean compareMethod(String javaMethod, String jimpleMethod) {
    boolean isInnerClass = false;
    if (javaMethod.contains("$")) {
      javaMethod = javaMethod.replace("$", ".");
      isInnerClass = true;
    }
    String javaClassName = javaMethod.split(": ")[0];
    if (jimpleMethod.contains("$")) jimpleMethod = jimpleMethod.replace("$", ".");
    if (!jimpleMethod.contains(javaClassName) && !javaClassName.contains("AnonymousClass"))
      return false;
    Matcher javaMatcher = javaMethodPattern.matcher(javaMethod);
    Matcher jimpleMatcher = jimpleMethodPattern.matcher(jimpleMethod);
    if (javaMatcher.find()) {
      String javaReturnType = javaMatcher.group(1);
      if (javaReturnType.contains(" ")) {
        String[] strs = javaReturnType.split(" ");
        javaReturnType = strs[strs.length - 1];
      }
      String javaMethodName = javaMatcher.group(2);
      String javaParameterString = javaMatcher.group(3);
      // replace all types between < > for generic types
      javaReturnType = replaceAllGenericTypes(javaReturnType);
      javaParameterString = replaceAllGenericTypes(javaParameterString);
      String[] javaParameters = javaParameterString.split(",");
      if (jimpleMatcher.find()) {
        String jimpleClassName = jimpleMatcher.group(1);
        if (!javaClassName.contains("AnonymousClass") && !javaClassName.equals(jimpleClassName))
          return false;
        if (javaClassName.contains("AnonymousClass")) {
          javaClassName = javaClassName.split(".AnonymousClass")[0];
          if (!jimpleClassName.startsWith(javaClassName)) return false;
        }
        String jimpleReturenType = jimpleMatcher.group(2);
        String jimpleMethodName = jimpleMatcher.group(3);
        String[] jimpleParameterTypes = jimpleMatcher.group(4).split(",");
        if (jimpleReturenType.endsWith(javaReturnType)) {
          if (jimpleMethodName.equals(javaMethodName)) {
            if (javaParameters.length == jimpleParameterTypes.length) {
              boolean paraMatch = true;
              for (int i = 0; i < javaParameters.length; i++) {
                String javaPara = javaParameters[i];
                if (javaPara.startsWith("final ")) javaPara = javaPara.replace("final ", "");
                javaPara = javaPara.split("\\s")[0];
                String jimplePara = jimpleParameterTypes[i];
                if (javaPara.endsWith("...")) // take care of varargs
                {
                  javaPara = javaPara.replace("...", "[]");
                }
                if (!jimplePara.endsWith(javaPara)) {
                  paraMatch = false;
                  break;
                }
              }
              return paraMatch;
            }
          }
        }
      }
    } else {
      // constructor
      javaMatcher = javaConstructorPattern.matcher(javaMethod);
      if (javaMatcher.find()) {
        String javaParameterString = javaMatcher.group(3);
        // replace all types between < > for generic types
        javaParameterString = replaceAllGenericTypes(javaParameterString);
        String[] javaParameters = javaParameterString.split(",");
        if (jimpleMatcher.find()) {
          String jimpleClassName = jimpleMatcher.group(1);
          if (!jimpleClassName.equals(javaClassName)) return false;
          String jimpleMethodName = jimpleMatcher.group(3);
          if (!jimpleMethodName.equals("<init>")) return false;
          String[] jimpleParameterTypes = jimpleMatcher.group(4).split(",");
          if (javaParameters.length == jimpleParameterTypes.length
              || (isInnerClass && javaParameters.length + 1 == jimpleParameterTypes.length)) {
            boolean paraMatch = true;
            for (int i = 0; i < javaParameters.length; i++) {
              String javaPara = javaParameters[i];
              if (javaPara.startsWith("final ")) javaPara = javaPara.replace("final ", "");
              javaPara = javaPara.split("\\s")[0];
              String jimplePara = jimpleParameterTypes[i];

              if (isInnerClass)
                if (i + 1 < jimpleParameterTypes.length) {
                  jimplePara = jimpleParameterTypes[i + 1];
                } else {
                  return false;
                }
              if (javaPara.endsWith("...")) // take care of varargs
              {
                javaPara = javaPara.replace("...", "[]");
              }
              if (!jimplePara.endsWith(javaPara)) {
                paraMatch = false;
                break;
              }
            }
            return paraMatch;
          }
        }
      }
    }
    return false;
  }
}
