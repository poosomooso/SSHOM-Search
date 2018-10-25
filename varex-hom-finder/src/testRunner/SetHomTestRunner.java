package testRunner;

import benchmark.BenchmarkPrograms;
import util.ConditionalMutationWrapper;

public class SetHomTestRunner {

  public static void main(String[] args)
      throws ClassNotFoundException, NoSuchFieldException,
      IllegalAccessException {
    if (args.length >= 4) {
      Class testClass = Class.forName(args[0]);
      String testName = args[1];
      ConditionalMutationWrapper cmw = new ConditionalMutationWrapper(
          BenchmarkPrograms.getTargetClasses());
      cmw.resetMutants();
      for (int i = 2; i < args.length; i++) {
        cmw.setMutant(args[i]);
      }
      System.out.println("run test  " + testName);
      RunTests.runTests(testClass, testName);
    }
  }
}
