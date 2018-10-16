package testRunner;

import benchmark.BenchmarkPrograms;

public class TestRunner {

  public static void main(String[] args) throws ClassNotFoundException {
    if (args.length < 2) {
      RunTests.runTests(BenchmarkPrograms.getTestClasses());
    } else {
      String className = args[0];
      String testName = args[1];
      System.out.println("run test  " + testName);
      RunTests.runTests(Class.forName(className), testName); // TODO: pass in class so we don't have to run getTestClasses()
    }

  }
}
