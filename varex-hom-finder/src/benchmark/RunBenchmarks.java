package benchmark;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

public class RunBenchmarks {
  public static final boolean RUNNING_LOCALLY = false;
  public static final Class[] TARGET_CLASSES; // required to find all conditionals
  public static final Class[] TEST_CLASSES;

  static {
    Class[] targetClasses, testClasses;

    try {
      targetClasses = BenchmarkPrograms
          .getAllJavaFilesInDir(BenchmarkPrograms.getSrcDir(), BenchmarkPrograms.getSrcPackage())
          .toArray(new Class[0]);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    try {
      testClasses = BenchmarkPrograms.getAllJavaFilesInDir(BenchmarkPrograms.getTestDir(), BenchmarkPrograms.getTestPackage())
          .toArray(new Class[0]);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    TARGET_CLASSES = targetClasses;
    TEST_CLASSES = testClasses;
    System.out.println("TARGET CLASSES : " + Arrays.toString(TARGET_CLASSES));
    System.out.println("TEST CLASSES : " + Arrays.toString(TEST_CLASSES));
  }

  public static void main(String[] args)
      throws NoSuchFieldException, IllegalAccessException {
    if (args.length < 1) {
      System.err
          .println("A mode must be specified. One of : naive, ga, or varex");
      System.exit(0);
    }
    String whichProgram = args[0];
    if (whichProgram.equalsIgnoreCase("naive")) {
      runNaive(TARGET_CLASSES, TEST_CLASSES);
    } else if (whichProgram.equalsIgnoreCase("ga")) {
      runEvolutionary(TARGET_CLASSES, TEST_CLASSES);
    } else if (whichProgram.equalsIgnoreCase("varex")) {
      runVarex(TARGET_CLASSES, TEST_CLASSES);
    } else {
      System.err
          .println("A mode must be specified. One of : naive, ga, or varex");
      System.exit(0);
    }
  }

  private static void runNaive(Class[] targets, Class[] tests)
      throws NoSuchFieldException, IllegalAccessException {
    BenchmarkedNaiveSSHOMFinder naiveSSHOMFinder = new BenchmarkedNaiveSSHOMFinder();
    naiveSSHOMFinder.naiveSSHOMFinder(targets, tests);
  }

  private static void runEvolutionary(Class[] targets, Class[] tests)
      throws NoSuchFieldException, IllegalAccessException {
    BenchmarkedEvolutionarySSHOMFinder evolutionarySSHOMFinder = new BenchmarkedEvolutionarySSHOMFinder();
    evolutionarySSHOMFinder.geneticAlgorithm(targets, tests);
  }

  private static void runVarex(Class[] targets, Class[] tests)
      throws NoSuchFieldException, IllegalAccessException {
    BenchmarkedVarexSSHOMFinder varexSSHOMFinder = new BenchmarkedVarexSSHOMFinder();
    varexSSHOMFinder.varexSSHOMFinder(targets, tests);
  }

}
