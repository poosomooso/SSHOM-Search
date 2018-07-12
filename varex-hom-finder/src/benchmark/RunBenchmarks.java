package benchmark;

import mutated.triangle.Triangle;
import mutated.triangle.Triangle_ESTest_improved;

public class RunBenchmarks {
  public static final Class[] classes = { Triangle.class };
  public static final Class[] testClasses = { Triangle_ESTest_improved.class };

  public static void main(String[] args)
      throws NoSuchFieldException, IllegalAccessException {
//    runNaive(classes, testClasses);
//    runEvolutionary(classes, testClasses);
    runVarex(classes, testClasses);
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
