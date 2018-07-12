package benchmark;

import util.CheckStronglySubsuming;
import util.SSHOMListener;
import util.SSHOMRunner;

import java.util.Arrays;

public class BenchmarkedNaiveSSHOMFinder {
  private Benchmarker benchmarker;
  private SSHOMRunner runner;

  public BenchmarkedNaiveSSHOMFinder() {
    benchmarker = new Benchmarker();
  }

  public void naiveSSHOMFinder(Class[] targetClasses, Class[] testClasses)
      throws NoSuchFieldException, IllegalAccessException {
    benchmarker.start();
    runner = new SSHOMRunner(targetClasses, testClasses);
    String[] mutants = runner.getMutants().toArray(new String[0]);

    runOnNOrder(2, new String[0], mutants, 0);
    runOnNOrder(3, new String[0], mutants, 0);
    runOnNOrder(4, new String[0], mutants, 0);

  }

  private void runOnNOrder(int order, String[] selectedMutants, String[] allMutants, int mutantStart)
      throws NoSuchFieldException, IllegalAccessException {
    if (order <= 0) {
      SSHOMListener sshomListener = runner.runJunitOnHOMAndFOMs(selectedMutants);
      if (CheckStronglySubsuming.isStronglySubsuming(sshomListener)) {
        benchmarker.timestamp(String.join(",", selectedMutants));
      }
    } else if (mutantStart < allMutants.length) {
      for (int i = mutantStart; i < allMutants.length; i++) {
        String[] newSelected = Arrays
            .copyOf(selectedMutants, selectedMutants.length + 1);
        newSelected[selectedMutants.length] = allMutants[i];
        runOnNOrder(order-1, newSelected, allMutants, i+1);
      }
    }
  }
}
