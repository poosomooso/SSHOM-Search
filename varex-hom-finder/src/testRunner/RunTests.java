package testRunner;

import junit.framework.TestCase;
import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class RunTests {
  public static void runTests(Class testClass) {
    runTests(new Class[] { testClass });
  }

  public static void runTests(Class[] testClasses) {
    for (Class c : testClasses) {
      runTestAnnotations(c);
    }
  }

  private static void runTestAnnotations(Class c) {


    Optional<Method> beforeMethod = findOfAnnotation(c, Before.class);
    Optional<Method> beforeClassMethod = findOfAnnotation(c, BeforeClass.class);
    Optional<Method> afterMethod = findOfAnnotation(c, After.class);
    Optional<Method> afterClassMethod = findOfAnnotation(c, AfterClass.class);

    // creating test object
    Object instance;
    try {
      instance = c.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    // before class
    try {
      invokeIfNonempty(beforeClassMethod, instance);
    } catch (InvocationTargetException | IllegalAccessException e) {
      e.printStackTrace();
    }

    // run all tests
    for (Method method : c.getMethods()) {
      if (method.getAnnotation(Test.class) != null) {
        try {
          invokeIfNonempty(beforeMethod, instance);
          System.out.println("METHOD: " + method);
          method.invoke(instance, null);
          invokeIfNonempty(afterMethod, instance);
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        } catch (Throwable e) {
          System.out.println(method.getName());
        }
      }
    }

    // after class
    try {
      invokeIfNonempty(afterClassMethod, instance);
    } catch (InvocationTargetException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  private static Optional<Method> findOfAnnotation(Class c, Class annotation) {
    for (Method method : c.getMethods()) {
      if (method.getAnnotation(annotation) != null) {
        return Optional.of(method);
      }
    }
    return Optional.empty();
  }

  private static <T> void invokeIfNonempty(Optional<Method> m, T instance)
      throws InvocationTargetException, IllegalAccessException {
    if (m.isPresent()) {
        m.get().invoke(instance, null);
    }
  }
}
