package verify;

import manual.triangle.Triangle;
import manual.triangle.Triangle_ESTest_linecoverage;
import manual.triangle.testTriangleExhaustive;
import org.junit.runner.Description;
import util.SSHOMListener;
import util.SSHOMRunner;
import util.SetArithmetic;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class PrintSSHOMStats {
  public static void main(String[] args)
      throws NoSuchFieldException, IllegalAccessException {
//    printDiffM7M9();
    runAllExtraSSHOMs();
  }

  private static void runAllExtraSSHOMs()
      throws NoSuchFieldException, IllegalAccessException {
    String[] mutants;
    mutants = new String[] { "m0", "m2", "m7" };
    System.out.println(Arrays.toString(mutants)
        + " ------------------------------------------------------ ");
    printTestsEvo(mutants);
//
//    mutants = new String[] { "m1", "m9" };
//    System.out.println(Arrays.toString(mutants)
//        + " ------------------------------------------------------ ");
//    printTestsEvo(mutants);
//
//    mutants = new String[] { "m0", "m7" };
//    System.out.println(Arrays.toString(mutants)
//        + " ------------------------------------------------------ ");
//    printTestsEvo(mutants);
//
//    mutants = new String[] { "m1", "m7" };
//    System.out.println(Arrays.toString(mutants)
//        + " ------------------------------------------------------ ");
//    printTestsEvo(mutants);
//
//    mutants = new String[] { "m0", "m1", "m2", "m7" };
//    System.out.println(Arrays.toString(mutants)
//        + " ------------------------------------------------------ ");
//    printTestsEvo(mutants);
//
//    mutants = new String[] { "m1", "m7", "m9" };
//    System.out.println(Arrays.toString(mutants)
//        + " ------------------------------------------------------ ");
//    printTestsEvo(mutants);
//
//    mutants = new String[] { "m0", "m1", "m2" };
//    System.out.println(Arrays.toString(mutants)
//        + " ------------------------------------------------------ ");
//    printTestsEvo(mutants);
//
//    mutants = new String[] { "m0", "m1", "m2", "m9" };
//    System.out.println(Arrays.toString(mutants)
//        + " ------------------------------------------------------ ");
//    printTestsEvo(mutants);
//
//    mutants = new String[] { "m0", "m1", "m2", "m7", "m9" };
//    System.out.println(Arrays.toString(mutants)
//        + " ------------------------------------------------------ ");
//    printTestsEvo(mutants);
//
//    mutants = new String[]{"m0", "m9" };
//    System.out.println(Arrays.toString(mutants)
//        + " ------------------------------------------------------ ");
//    printTestsEvo(mutants);
  }

  private static void printDiffM7M9()
      throws NoSuchFieldException, IllegalAccessException {
    SSHOMRunner r = new SSHOMRunner(Triangle.class,
        testTriangleExhaustive.class);
    SSHOMListener l = r.runJunitOnHOM(new String[] { "m7", "m9" });

    Set<Description> overlap = SetArithmetic
        .getIntersection(l.getFomTests());
    Set<Description> remainingTests = SetArithmetic
        .getDifference(overlap, l.getHomTests());

    printTests(remainingTests);
  }



  private static void printTestsEvo(String[] mutant)
      throws NoSuchFieldException, IllegalAccessException {
    SSHOMRunner r = new SSHOMRunner(Triangle.class,
        Triangle_ESTest_linecoverage.class);
    SSHOMListener evo = r.runJunitOnHOM(mutant);

    Set<Description> allFomTests = SetArithmetic.getUnion(evo.getFomTests());
    Set<Description> overlapFomTests = SetArithmetic.getIntersection(evo.getFomTests());
    Set<Description> notInOverlap = SetArithmetic
        .getDifference(allFomTests, overlapFomTests);


    printTests(allFomTests);
    System.out.println("-------------------------------------------------------------------");
    printTests(overlapFomTests);
    System.out.println("-------------------------------------------------------------------");
    printTests(notInOverlap);
    System.out.println("-------------------------------------------------------------------");
    printTests(evo.getHomTests());

  }

  private static void printTests(Set<Description> tests) {
    for (String d : tests.stream().map(Description::toString).sorted().collect(Collectors.toList())) {
      System.out.println(d);
    }
  }


}
