package util;

import gov.nasa.jpf.annotation.Conditional;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;

import java.lang.reflect.Field;
import java.util.Set;

public class SSHOMRunner {
  private Class targetClass;
  private final Class testClass;

  public SSHOMRunner(Class targetClass, Class testClass) {
    this.targetClass = targetClass;
    this.testClass = testClass;
  }

  public static boolean isStronglySubsuming(SSHOMListener listener) {
    Set<Description> overlap = SetArithmetic.getIntersection(listener.getFomTests());
    return overlap.containsAll(listener.getHomTests());
  }

  public static boolean isStrictStronglySubsuming(SSHOMListener listener) {
    Set<Description> overlap = SetArithmetic
        .getIntersection(listener.getFomTests());
    if(!overlap.containsAll(listener.getHomTests())) return false;
    overlap.removeAll(listener.getHomTests());
    return !overlap.isEmpty();
  }

  public SSHOMListener runJunitOnHOM(String[] mutants)
      throws IllegalAccessException, NoSuchFieldException {
    JUnitCore jUnitCore = new JUnitCore();
    SSHOMListener sshomListener = new SSHOMListener();
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
    return sshomListener;
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
