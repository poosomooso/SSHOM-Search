package benchmark;

import mutated.triangle.Triangle;
import mutated.triangle.Triangle_ESTest_improved;

public class RunBenchmarks {
  public static void main(String[] args)
      throws NoSuchFieldException, IllegalAccessException {
    BenchmarkedNaiveSSHOMFinder naiveSSHOMFinder = new BenchmarkedNaiveSSHOMFinder();
    naiveSSHOMFinder.naiveSSHOMFinder(new Class[] { Triangle.class },
        new Class[] { Triangle_ESTest_improved.class });
  }
}
