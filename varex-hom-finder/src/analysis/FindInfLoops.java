package analysis;

import benchmark.BenchmarkPrograms;
import benchmark.BenchmarkedVarexSSHOMFinder;
import benchmark.CommandLineRunner;
import benchmark.RunBenchmarks;
import org.junit.runner.Description;
import util.CheckStronglySubsuming;
import util.SSHOMListener;
import util.SSHOMRunner;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FindInfLoops {
  private static SSHOMRunner runner;

  public static void findInfLoops(Class[] targetClasses, Class[] testClasses)
      throws NoSuchFieldException, IllegalAccessException {
    runner = new SSHOMRunner(targetClasses, testClasses);
    String[] mutants = runner.getMutants().toArray(new String[0]);
//    checkFOMs(mutants);
    for (int i = 2; i < 3; i++) {
      checkNOrder(i, new ArrayList<>(), mutants, 0);
    }
  }

  private static void checkFOMs(String[] mutants)
      throws NoSuchFieldException, IllegalAccessException {
    for (String m : mutants) {
      printIfInf(m);
    }
  }

  private static void checkNOrder(int order, List<String> startingSelectedMutants, String[] allMutants, int mutantStart)
      throws NoSuchFieldException, IllegalAccessException {
    if (order <= 0) {
      printIfInf(startingSelectedMutants.toArray(new String[0]));
    } else if (mutantStart < allMutants.length) {
      for (int i = mutantStart; i < allMutants.length; i++) {
        List<String> newSelected = new ArrayList<>(startingSelectedMutants);
        newSelected.add(allMutants[i]);
        checkNOrder(order-1, newSelected, allMutants, i+1);
      }
    }
  }

  public static void printIfInf(String... mutants) {
    PrintStream originalStream = System.out;

    PrintStream dummyStream = new PrintStream(new OutputStream() {
      public void write(int b) {
        // NO-OP
      }
    });

    System.setOut(dummyStream);

//    String[] args = new String[mutants.length + 4];
//    args[0] = "java";
//    args[1] = "-cp";
//    args[2] = "out/production/varex-hom-finder";
//    args[3] = "analysis/FindInfLoops";
//    System.arraycopy(mutants, 0, args, 4, mutants.length);
//    ProcessBuilder processBuilder = new ProcessBuilder(args);
//    System.out.println(Arrays.toString(args));;
//    Process process = null;
//
//    CommandLineRunner.process(args);
//    try {
//      process = processBuilder.start();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }

    Thread fomRunner = new Thread(() -> {
      try {
        runner.runJunitOnHOM(mutants);
      } catch (IllegalAccessException | NoSuchFieldException e) {
        e.printStackTrace();
      }
    });
    fomRunner.start();

    long startTime = System.currentTimeMillis();
    long waitTime = 100;
    while (System.currentTimeMillis() - startTime < 6000 && fomRunner
        .isAlive()) {
      try {
        Thread.sleep(waitTime);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      waitTime += 50;
    }
    if (fomRunner.isAlive()) {
      fomRunner.interrupt();
      System.setOut(originalStream);
      System.out.println(Arrays.toString(mutants));
    }
    System.setOut(originalStream);
  }

  public static void main(String[] args)
      throws NoSuchFieldException, IllegalAccessException {
    if(args.length == 0) {
      findInfLoops(BenchmarkPrograms.getTargetClasses(), BenchmarkPrograms.getTestClasses());
    } else {
      runner = new SSHOMRunner(BenchmarkPrograms.getTargetClasses(), BenchmarkPrograms.getTestClasses());
      runner.runJunitOnHOM(args);
    }
  }
}