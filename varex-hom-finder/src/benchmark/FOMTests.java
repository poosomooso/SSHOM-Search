package benchmark;

import org.junit.runner.Description;
import util.SSHOMListener;
import util.SSHOMRunner;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FOMTests {

  public static void main(String[] args)
      throws NoSuchFieldException, IllegalAccessException {
    Class<?>[] testClasses = BenchmarkPrograms.getTestClasses();
    Class<?>[] targetClasses = BenchmarkPrograms.getTargetClasses();
    String[] mutants = BenchmarkPrograms.getMutantNames();

    SSHOMRunner runner = new SSHOMRunner(targetClasses, testClasses);

    printFailedForFOMs(testClasses, mutants, runner);
  }

  private static void printFailedForFOMs(Class<?>[] testClasses, String[] mutants, SSHOMRunner runner)
      throws NoSuchFieldException, IllegalAccessException {
    for (String m : mutants) {
      SSHOMListener listener;
      if (BenchmarkPrograms.programHasInfLoops()) {
        listener =InfLoopTestProcess.getFailedTests(testClasses, new String[] { m });
      } else {
        listener = runner.runJunitOnHOM(m);
      }

      Set<Description> failedTests = listener.getHomTests();
      List<String> failedTestNames = failedTests
          .stream()
          .map(t-> t.getClassName()+"."+t.getMethodName())
          .sorted()
          .collect(Collectors.toList());
      failedTestNames.add(0, m);
      System.out.println(String.join(",", failedTestNames));
    }
  }
}
