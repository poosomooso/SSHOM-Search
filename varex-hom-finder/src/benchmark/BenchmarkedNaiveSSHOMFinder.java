package benchmark;

import util.CheckStronglySubsuming;
import util.SSHOMListener;
import util.SSHOMRunner;

import java.util.Arrays;

public class BenchmarkedNaiveSSHOMFinder {
  private Benchmarker benchmarker;

  public BenchmarkedNaiveSSHOMFinder() {
    benchmarker = new Benchmarker();
  }

  public void naiveSSHOMFinder(Class[] targetClasses, Class[] testClasses)
      throws NoSuchFieldException, IllegalAccessException {
    benchmarker.start();
    SSHOMRunner runner = new SSHOMRunner(targetClasses, testClasses);
    String[] mutants = runner.getMutants().toArray(new String[0]);

    // 2nd order
    for (int i = 0; i < mutants.length; i++) {
      for (int j = i+1; j < mutants.length; j++) {
        SSHOMListener sshomListener = runner
            .runJunitOnHOMAndFOMs(mutants[i], mutants[j]);
        if (CheckStronglySubsuming.isStronglySubsuming(sshomListener)) {
          benchmarker.timestamp(mutants[i] + "," + mutants[j]);
        }
      }
    }

    // third order
    for (int i = 0; i < mutants.length; i++) {
      for (int j = i+1; j < mutants.length; j++) {
        for (int k = j+1; k < mutants.length; k++) {
          SSHOMListener sshomListener = runner.runJunitOnHOMAndFOMs(mutants[i], mutants[j], mutants[k]);
          if (CheckStronglySubsuming.isStronglySubsuming(sshomListener)) {
            benchmarker.timestamp(mutants[i] + "," + mutants[j] + "," + mutants[k]);
          }
        }
      }
    }

    // fourth order
    for (int i = 0; i < mutants.length; i++) {
      for (int j = i+1; j < mutants.length; j++) {
        for (int k = j+1; k < mutants.length; k++) {
          for (int l = k+1; l < mutants.length; l++) {
            SSHOMListener sshomListener = runner.runJunitOnHOMAndFOMs(mutants[i], mutants[j], mutants[k], mutants[l]);
            if (CheckStronglySubsuming.isStronglySubsuming(sshomListener)) {
              benchmarker.timestamp(mutants[i] + "," + mutants[j] + "," + mutants[k] + "," + mutants[l]);
            }
          }
        }
      }
    }

  }

//  private runOnNOrder(int order) {
//
//  }
}
