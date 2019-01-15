package benchmark;

import org.junit.runner.Description;
import util.SSHOMListener;
import util.SSHOMRunner;

import javax.script.Bindings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FOMTests {

  public static void main(String[] args)
      throws NoSuchFieldException, IllegalAccessException {
    Class<?>[] testClasses = BenchmarkPrograms.getTestClasses();
    Class<?>[] targetClasses = BenchmarkPrograms.getTargetClasses();
    String[] mutants = BenchmarkPrograms.getMutantNames();

    SSHOMRunner runner = new SSHOMRunner(targetClasses, testClasses);

    Map<String, Set<Description>> tests = runFoms(testClasses, mutants,
        runner);

    for (Map.Entry<String, Set<Description>> entry : tests.entrySet()) {
      System.out.print(entry.getKey()+";");
      Set<Description> failedTests = entry.getValue();
      List<String> failedTestNames = failedTests
          .stream()
          .map(t-> t.getClassName()+"."+t.getMethodName())
          .sorted()
          .collect(Collectors.toList());
      System.out.println(String.join(",", failedTestNames));

    }
  }

  private static Map<String, Set<Description>> runFoms(Class<?>[] testClasses, String[] mutants, SSHOMRunner runner)
      throws NoSuchFieldException, IllegalAccessException {
    Map<String, Set<Description>> foms = new HashMap<>();
    for (String m : mutants) {
      SSHOMListener listener;
      if (BenchmarkPrograms.programHasInfLoops()) {
        listener =InfLoopTestProcess.runTests(testClasses, new String[] { m });
      } else {
        listener = runner.runJunitOnHOM(m);
      }
      foms.put(m, listener.getHomTests());
    }
    return foms;
  }
}
