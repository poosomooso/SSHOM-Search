package util;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.*;
import java.util.stream.Collectors;

public class SSHOMListener extends RunListener {
  Set<Description> homTests;
  List<Set<Description>> fomTests = new LinkedList<>();
  Set<Description> currentTests;
  int numTests = 0;

  public void signalHOMBegin() {
    currentTests = new HashSet<>();
  }

  public void signalHOMEnd() {
    homTests = currentTests;
    currentTests = null;
  }

  public void signalFOMBegin() {
    currentTests = new HashSet<>();
  }

  public void signalFOMEnd() {
    fomTests.add(currentTests);
    currentTests = null;
  }

  @Override
  public void testFailure(Failure failure) throws Exception {
    super.testFailure(failure);
    failure.getException().printStackTrace();
    System.err.println(failure.getDescription());
    currentTests.add(failure.getDescription());
  }

  @Override
  public void testAssumptionFailure(Failure failure) {
    super.testAssumptionFailure(failure);
    currentTests.add(failure.getDescription());
  }

  @Override
  public void testIgnored(Description description) throws Exception {
    super.testIgnored(description);
  }

  public Set<Description> getHomTests() {
    return Collections.unmodifiableSet(homTests);
  }

  public List<Set<Description>> getFomTests() {
    return Collections.unmodifiableList(fomTests.stream().map(
        Collections::unmodifiableSet).collect(
        Collectors.toList()));
  }

}

