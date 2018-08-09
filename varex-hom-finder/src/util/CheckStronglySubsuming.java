package util;

import org.junit.runner.Description;

import java.util.List;
import java.util.Set;

public class CheckStronglySubsuming {
  public static boolean isStronglySubsuming(Set<Description> homTests, List<Set<Description>> fomTests) {
    if (homTests.size() == 0) return false;
    Set<Description> overlap = SetArithmetic
        .getIntersection(fomTests);
    return overlap.containsAll(homTests);
  }

  public static boolean isStrictStronglySubsuming(Set<Description> homTests, List<Set<Description>> fomTests) {
    if (homTests.size() == 0) return false;
    Set<Description> overlap = SetArithmetic
        .getIntersection(fomTests);
    if (!overlap.containsAll(homTests))
      return false;
    overlap.removeAll(homTests);
    return !overlap.isEmpty();
  }
}
