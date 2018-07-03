package varex;

import gov.nasa.jpf.annotation.Conditional;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.*;

public class VerifySSHOM {

  private Class targetClass;
  private final Class testClass;
  private final String fname;

  public VerifySSHOM(Class targetClass, Class testClass, String fname) {
    this.targetClass = targetClass;
    this.testClass = testClass;
    this.fname = fname;
  }

  public void printVerify() {
    String[] mutants;

    try (Scanner in = new Scanner(new File(fname))) {
      while (in.hasNextLine()) {
        JUnitCore jUnitCore = new JUnitCore();
        SSHOMListener sshomListener = new SSHOMListener();
        mutants = in.nextLine().split(" ");

        // hom
        resetMutants();
        for (String s : mutants) {
          setMutant(s);
        }
        sshomListener.signalHOMBegin();
        jUnitCore.addListener(sshomListener);
        jUnitCore.run(testClass);
        sshomListener.signalHOMEnd();

        //foms
        for (String s : mutants) {
          resetMutants();
          setMutant(s);
          sshomListener.signalFOMBegin();
          jUnitCore.run(testClass);
          sshomListener.signalFOMEnd();
        }
        System.out.println(Arrays.toString(mutants));
        System.out.print(sshomListener.isStronglySubsuming() ? "Is strongly subsuming" : "Is NOT strongly subsuming");
        System.out.println(sshomListener.isStrictStronglySubsuming() ? " -- Strict" : "");
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }
  }


  private class SSHOMListener extends RunListener {
    Set<Description> homTests;
    List<Set<Description>> fomTests = new LinkedList<>();
    Set<Description> currentTests;

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

    public boolean isStronglySubsuming() {
      Set<Description> overlap = new HashSet<>(fomTests.get(0));
      for (Set<Description> fom : fomTests) {
        overlap.retainAll(fom);
      }
      return overlap.containsAll(homTests);
    }

    public boolean isStrictStronglySubsuming() {
      Set<Description> overlap = new HashSet<>(fomTests.get(0));
      for (Set<Description> fom : fomTests) {
        overlap.retainAll(fom);
      }
      if(!overlap.containsAll(homTests)) return false;
      overlap.removeAll(homTests);
      return !overlap.isEmpty();
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
      super.testFailure(failure);
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
  }

  private void setMutant(String mutant)
      throws NoSuchFieldException, IllegalAccessException {
    targetClass.getField(mutant).setBoolean(null, true);
  }

  private void resetMutants() throws IllegalAccessException {
    for (Field f : targetClass.getFields()) {
      if (f.getAnnotation(Conditional.class) != null) {
        f.setBoolean(null, false);
      }
    }
  }

}
