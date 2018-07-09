package verify;

import manual.triangle.Triangle;
import manual.triangle.testTriangleExhaustive;
import org.junit.runner.Description;
import util.SetArithmetic;

import java.util.Set;

public class PrintSSHOMStats {
  public static void main(String[] args)
      throws NoSuchFieldException, IllegalAccessException {
    printTestBreakdown();
  }
  public static void printTestBreakdown()
      throws NoSuchFieldException, IllegalAccessException {
    SSHOMRunner r = new SSHOMRunner(Triangle.class,
        testTriangleExhaustive.class);
    SSHOMListener l = r.runJunitOnHOM(new String[] { "m7", "m9" });
    Set<Description> overlap = SetArithmetic
        .getIntersection(l.getFomTests());
    Set<Description> remainingTests = SetArithmetic
        .getDifference(overlap, l.getHomTests());
    for (Description d : remainingTests) {
      System.out.println(d);
    }
  }
}
