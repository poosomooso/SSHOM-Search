package testRunner;

import benchmark.BenchmarkPrograms;

public class VarexTestRunner {

  public static void main(String[] args) throws ClassNotFoundException {
    if (args.length < 2) {
      RunTests.runTests(BenchmarkPrograms.getTestClasses());
    } else {
      String className = args[0];
      String testName = args[1];
      System.out.println("run test  " + testName);
      RunTests.runTest(Class.forName(className), testName);
    }

  }
}
