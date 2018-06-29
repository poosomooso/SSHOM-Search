package tcas;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import triangle.TriangleTestSuite;

public class RunTriangleTests {

  public static void main(String[] args) {
    Result result = JUnitCore.runClasses(TriangleTestSuite.class);

    for (Failure failure : result.getFailures()) {
      System.out.println(failure.toString());
    }

    System.out.println(result.wasSuccessful());
  }

}
