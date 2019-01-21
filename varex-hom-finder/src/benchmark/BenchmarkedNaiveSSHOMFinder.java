package benchmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.runner.Description;

import util.CheckStronglySubsuming;
import util.SSHOMListener;
import util.SSHOMRunner;

public class BenchmarkedNaiveSSHOMFinder {
  private SSHOMRunner                   runner;
  private Map<String, Set<Description>> foms;
  static long x = 0;
  private Class<?>[] testClasses;


  public void naiveSSHOMFinder()
      throws NoSuchFieldException, IllegalAccessException {
	Benchmarker.instance.start();

    Class<?>[] targetClasses = BenchmarkPrograms.getTargetClasses();
    this.testClasses = BenchmarkPrograms.getTestClasses();

    foms = new HashMap<>();
    runner = new SSHOMRunner(targetClasses, testClasses);
    String[] mutants = BenchmarkPrograms.getMutantNames();
    populateFoms(mutants);

    int maxOrder = 33;
    Benchmarker.instance.timestamp("start homs");
    for (int i = 2; i < maxOrder; i++) {
      runOnNOrder(i, new ArrayList<>(), mutants, 0);
      Benchmarker.instance.timestamp("order "+i+" done");
    }
  }

  private void populateFoms(String[] mutants)
      throws NoSuchFieldException, IllegalAccessException {
    for (String m : mutants) {
//      SSHOMListener sshomListener = runner.runJunitOnHOM(m);
      SSHOMListener listener;
      if (BenchmarkPrograms.programHasInfLoops()) {
      listener =InfLoopTestProcess.runTests(testClasses, new String[] { m });
      } else {
        listener = runner.runJunitOnHOM(m);
      }
      foms.put(m, listener.getHomTests());
    }
  }

  private void runOnNOrder(int order, List<String> selectedMutants, String[] allMutants, int mutantStart)
      throws NoSuchFieldException, IllegalAccessException {
//    PrintStream out = System.out;
//    PrintStream temp = null;
//    try {
//      temp = new PrintStream(new File("_temp.txt"));
//
//    } catch (FileNotFoundException e) {
//      e.printStackTrace();
//    }
//    System.setOut(temp);
    if (order <= 0) {
//      SSHOMListener sshomListener = runner.runJunitOnHOMAndFOMs(selectedMutants.toArray(new String[0]));
//      SSHOMListener listener = InfLoopTestProcess.runTests(testClasses, selectedMutants.toArray(new String[0]));
      SSHOMListener listener;
      if (BenchmarkPrograms.programHasInfLoops()) {
        listener =InfLoopTestProcess.runTests(testClasses, selectedMutants.toArray(new String[0]));
      } else {
        listener = runner.runJunitOnHOM(selectedMutants.toArray(new String[0]));
      }
      List<Set<Description>> currentFoms = selectedMutants.stream()
          .map(m -> foms.get(m)).collect(Collectors.toList());

      System.out.println(x++);
      if (CheckStronglySubsuming
          .isStronglySubsuming(listener.getHomTests(), currentFoms)) {
        //        System.setOut(out);
        Benchmarker.instance.timestamp(
            String.join(",", selectedMutants) + " is_strict_subsuming: "
                + CheckStronglySubsuming
                .isStrictStronglySubsuming(listener.getHomTests(), currentFoms));
        //        System.setOut(temp);
      }
    } else if (mutantStart < allMutants.length) {
      for (int i = mutantStart; i < allMutants.length; i++) {
        List<String> newSelected = new ArrayList<>(selectedMutants);
        newSelected.add(allMutants[i]);
        if (BenchmarkPrograms.homIsValid(newSelected)) {
          runOnNOrder(order - 1, newSelected, allMutants, i + 1);
        }
      }
    }
  }

}
