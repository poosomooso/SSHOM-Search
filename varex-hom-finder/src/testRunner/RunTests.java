package testRunner;

import junit.framework.TestCase;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RunTests {
  public static void runTests(Class testClass) {

    runTestAnnotations(testClass);
  }

  public static void runTests(Class[] testClasses) {
    for (Class c : testClasses) {

      if (c.getSuperclass().equals(TestCase.class)) {
        try {
          System.out.println(c);
          ((Class<TestCase>)c).newInstance().run();
        } catch (AssertionError e) {
          System.out.println(c.getName());
        } catch (InstantiationException | IllegalAccessException e) {
          e.printStackTrace();
        }
      } else {
        runTestAnnotations(c);
      }
    }
  }

  private static void runTestAnnotations(Class c) {
    for (Method method : c.getMethods()) {
      if (method.getAnnotation(Test.class) != null) {
        try {
          method.invoke(c.newInstance(), null);
        } catch (IllegalAccessException | InstantiationException e) {
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          if (e.getCause() instanceof AssertionError) {
            System.out.println(method.getName());
          } else {
            e.printStackTrace();
          }
        }
      }
    }
  }
}
