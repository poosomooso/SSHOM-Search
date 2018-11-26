package benchmark;

import org.evosuite.shaded.org.hibernate.boot.jaxb.SourceType;
import org.junit.Test;
import org.junit.runner.Description;
import util.CheckStronglySubsuming;
import util.SSHOMListener;
import util.SSHOMRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class BenchmarkedNaiveSSHOMFinder {
  private Benchmarker                   benchmarker;
  private SSHOMRunner                   runner;
  private Map<String, Set<Description>> foms;
  static long x = 0;
  private Class[] testClasses;

  public BenchmarkedNaiveSSHOMFinder() {
    benchmarker = new Benchmarker();
  }

  public void naiveSSHOMFinder()
      throws NoSuchFieldException, IllegalAccessException {
    benchmarker.start();

    Class[] targetClasses = BenchmarkPrograms.getTargetClasses();
    this.testClasses = BenchmarkPrograms.getTestClasses();

    foms = new HashMap<>();
    runner = new SSHOMRunner(targetClasses, testClasses);
    String[] mutants = BenchmarkPrograms.getMutantNames();
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
        benchmarker.timestamp(String.join(",", selectedMutants));
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
