package varex;

import java.util.HashSet;
import java.util.Set;

public class SetArithmetic {
  public static Set<String> getIntersection(Set<String> a, Set<String> b) {
    Set<String> newSet;
    if (a.size() < b.size()) {
      newSet = new HashSet<>(a);
      newSet.retainAll(b);
    } else {
      newSet = new HashSet<>(b);
      newSet.retainAll(a);
    }
    return newSet;
  }

  public static Set<String> getUnion(Set<String> a, Set<String> b) {
    Set<String> newSet;
    if (a.size() > b.size()) {
      newSet = new HashSet<>(a);
      newSet.addAll(b);
    } else {
      newSet = new HashSet<>(b);
      newSet.addAll(a);
    }
    return newSet;
  }

  public static int getIntersectionSize(Set<String> a, Set<String> b) {
    return (int) (a.stream().filter(b::contains).count());
  }

  public static int getUnionSize(Set<String> a, Set<String> b) {
    return a.size() + b.size() - getIntersectionSize(a, b);
  }

  public static int getDifferenceSize(Set<String> a, Set<String>... subtrahends) {
    Set<String> allSubtrahends = new HashSet<>();
    for (Set<String> s : subtrahends) {
      allSubtrahends.addAll(s);
    }
    return (int) (a.stream().filter(x -> !allSubtrahends.contains(x)).count());
  }
}
