package testRunner;

import manual.triangle.Triangle_ESTest_branchcoverage;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import manual.triangle.testTriangleExhaustive;

public class RunTestsForVarex {
  static Class testClass = Triangle_ESTest_branchcoverage.class;
//  static Class testClass = testTriangleExhaustive.class;
  public static void main(String[] args) {

    for (Method method : testClass.getMethods()) {
//              System.out.println(method.getName());
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
