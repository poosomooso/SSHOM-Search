package testRunner;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RunTests {
  public static void runTests(Class testClass) {

    for (Method method : testClass.getMethods()) {
      if (method.getAnnotation(Test.class) != null) {
        try {
          method.invoke(testClass.newInstance(), null);
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
