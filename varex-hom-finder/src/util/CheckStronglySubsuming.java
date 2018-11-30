package util;

import java.util.List;
import java.util.Set;

public class CheckStronglySubsuming {
  public static <T> boolean isStronglySubsuming(Set<T> homTests, List<Set<T>> fomTests) {
    if (homTests.size() == 0) return false;
    Set<T> overlap = SetArithmetic
        .getIntersection(fomTests);
    return overlap.containsAll(homTests);
  }

  public static <T> boolean isStrictStronglySubsuming(Set<T> homTests, List<Set<T>> fomTests) {
    if (homTests.size() == 0) return false;
    Set<T> overlap = SetArithmetic
        .getIntersection(fomTests);
    if (!overlap.containsAll(homTests))
      return false;
    overlap.removeAll(homTests);
    return !overlap.isEmpty();
  }
}
