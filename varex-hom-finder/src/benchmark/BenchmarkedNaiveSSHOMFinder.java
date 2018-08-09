package benchmark;

import org.junit.runner.Description;
import util.CheckStronglySubsuming;
import util.SSHOMListener;
import util.SSHOMRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class BenchmarkedNaiveSSHOMFinder {
  private Benchmarker                   benchmarker;
  private SSHOMRunner                   runner;
  private Map<String, Set<Description>> foms;

  public BenchmarkedNaiveSSHOMFinder() {
    benchmarker = new Benchmarker();
  }

  public void naiveSSHOMFinder(Class[] targetClasses, Class[] testClasses)
      throws NoSuchFieldException, IllegalAccessException {
    benchmarker.start();
    foms = new HashMap<>();
    runner = new SSHOMRunner(targetClasses, testClasses);
    String[] mutants = runner.getMutants().toArray(new String[0]);
    populateFoms(mutants);

    int maxOrder = 33;
    benchmarker.timestamp("start homs");
    for (int i = 2; i < maxOrder; i++) {
      runOnNOrder(i, new ArrayList<>(), mutants, 0);
      benchmarker.timestamp("order "+i+" done");
    }
  }

  private void populateFoms(String[] mutants)
      throws NoSuchFieldException, IllegalAccessException {
    for (String m : mutants) {
      SSHOMListener sshomListener = runner.runJunitOnHOM(m);
      foms.put(m, sshomListener.getHomTests());
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
      SSHOMListener sshomListener = runner.runJunitOnHOMAndFOMs(selectedMutants.toArray(new String[0]));
      List<Set<Description>> currentFoms = selectedMutants.stream()
          .map(m -> foms.get(m)).collect(Collectors.toList());

      if (CheckStronglySubsuming
          .isStronglySubsuming(sshomListener.getHomTests(), currentFoms)) {
        //        System.setOut(out);
        benchmarker.timestamp(String.join(",", selectedMutants));
        //        System.setOut(temp);
      }
    } else if (mutantStart < allMutants.length) {
      for (int i = mutantStart; i < allMutants.length; i++) {
        List<String> newSelected = new ArrayList<>(selectedMutants);
        newSelected.add(allMutants[i]);
        runOnNOrder(order-1, newSelected, allMutants, i+1);
      }
    }
  }

}
