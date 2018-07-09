package util;

import java.util.*;

public class SetArithmetic {
  public static <T> Set<T> getIntersection(Set<T> a, Set<T> b) {
    Set<T> newSet;
    if (a.size() < b.size()) {
      newSet = new HashSet<T>(a);
      newSet.retainAll(b);
    } else {
      newSet = new HashSet<T>(b);
      newSet.retainAll(a);
    }
    return newSet;
  }

  public static <T> Set<T> getIntersection(Set<T>... allSets) {

    return getIntersection(Arrays.asList(allSets));
  }

  public static <T> Set<T> getIntersection(Collection<Set<T>> allSets) {
    Set<T> newSet = null;
    for (Set<T> s : allSets) {
      if (newSet == null) {
        newSet = new HashSet<>(s);
      }
      else {
        newSet.retainAll(s);
      }
    }

    return newSet;
  }

  public static <T> Set<T> getUnion(Set<T>... allSets) {
    Set<T> newSet = new HashSet<>();
    for (Set<T> s : allSets) {
      newSet.addAll(s);
    }

    return newSet;
  }

  public static <T> Set<T> getUnion(Collection<Set<T>> allSets) {
    Set<T> newSet = new HashSet<>();
    for (Set<T> s : allSets) {
      newSet.addAll(s);
    }

    return newSet;
  }

  public static <T> Set<T> getUnion(Set<T> a, Set<T> b) {
    Set<T> newSet;
    if (a.size() > b.size()) {
      newSet = new HashSet<>(a);
      newSet.addAll(b);
    } else {
      newSet = new HashSet<>(b);
      newSet.addAll(a);
    }
    return newSet;
  }

  @SafeVarargs
  public static <T> Set<T> getDifference(Set<T> a, Set<T>... subtrahends) {
    Set<T> result = new HashSet<>(a);
    for (Set<T> s : subtrahends) {
      result.removeAll(s);
    }
    return result;
  }

  public static <T> int getIntersectionSize(Set<T> a, Set<T> b) {
    return (int) (a.stream().filter(b::contains).count());
  }

  public static <T> int getUnionSize(Set<T> a, Set<T> b) {
    return a.size() + b.size() - getIntersectionSize(a, b);
  }

  @SafeVarargs
  public static <T> int getDifferenceSize(Set<T> a, Set<T>... subtrahends) {
    Set<Object> allSubtrahends = new HashSet<>();
    for (Set<T> s : subtrahends) {
      allSubtrahends.addAll(s);
    }
    return (int) (a.stream().filter(x -> !allSubtrahends.contains(x)).count());
  }
}
