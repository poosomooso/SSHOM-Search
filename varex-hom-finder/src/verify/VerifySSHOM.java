package verify;

import org.junit.runner.Description;
import util.SetArithmetic;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class VerifySSHOM {
  private SSHOMRunner runner;
  private final String fname;

  public VerifySSHOM(Class targetClass, Class testClass, String fname) {
    runner = new SSHOMRunner(targetClass, testClass);
    this.fname = fname;
  }

  public void printVerify() {
    String[] mutants;

    try (Scanner in = new Scanner(new File(fname))) {
      while (in.hasNextLine()) {
        mutants = in.nextLine().split(" ");
        SSHOMListener sshomListener = runner.runJunitOnHOM(mutants);

        System.out.println(Arrays.toString(mutants));
        System.out.print(isStronglySubsuming(sshomListener) ? "Is strongly subsuming" : "NOT STRONGLY SUBSUMING ****");
        System.out.println(isStrictStronglySubsuming(sshomListener) ? " -- Strict" : "");
      }
    } catch (FileNotFoundException | IllegalAccessException | NoSuchFieldException e) {
      e.printStackTrace();
    }
  }

  private boolean isStronglySubsuming(SSHOMListener listener) {
    Set<Description> overlap = SetArithmetic.getIntersection(listener.getFomTests());
    return overlap.containsAll(listener.getHomTests());
  }

  private boolean isStrictStronglySubsuming(SSHOMListener listener) {
    Set<Description> overlap = SetArithmetic
        .getIntersection(listener.getFomTests());
    if(!overlap.containsAll(listener.getHomTests())) return false;
    overlap.removeAll(listener.getHomTests());
    return !overlap.isEmpty();
  }
}
