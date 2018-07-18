package util;

import org.junit.runner.Description;

import java.util.Set;

public class CheckStronglySubsuming {
  public static boolean isStronglySubsuming(SSHOMListener listener) {
    if (listener.getHomTests().size() == 0) return false;
    Set<Description> overlap = SetArithmetic
        .getIntersection(listener.getFomTests());
    return overlap.containsAll(listener.getHomTests());
  }

  public static boolean isStrictStronglySubsuming(SSHOMListener listener) {
    if (listener.getHomTests().size() == 0) return false;
    Set<Description> overlap = SetArithmetic
        .getIntersection(listener.getFomTests());
    if (!overlap.containsAll(listener.getHomTests()))
      return false;
    overlap.removeAll(listener.getHomTests());
    return !overlap.isEmpty();
  }
}
