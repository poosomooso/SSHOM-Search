package verify;

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
        System.out.print(isStronglySubsuming(sshomListener) ? "Is strongly subsuming" : "Is NOT strongly subsuming");
        System.out.println(isStrictStronglySubsuming(sshomListener) ? " -- Strict" : "");
      }
    } catch (FileNotFoundException | IllegalAccessException | NoSuchFieldException e) {
      e.printStackTrace();
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

  private boolean isStronglySubsuming(SSHOMListener listener) {
    Set<Description> overlap = new HashSet<>(listener.getFomTests().get(0));
    for (Set<Description> fom : listener.getFomTests()) {
      overlap.retainAll(fom);
    }
    return overlap.containsAll(listener.getHomTests());
  }

  private boolean isStrictStronglySubsuming(SSHOMListener listener) {
    Set<Description> overlap = new HashSet<>(listener.getFomTests().get(0));
    for (Set<Description> fom : listener.getFomTests()) {
      overlap.retainAll(fom);
    }
    if(!overlap.containsAll(listener.getHomTests())) return false;
    overlap.removeAll(listener.getHomTests());
    return !overlap.isEmpty();
  }
}
