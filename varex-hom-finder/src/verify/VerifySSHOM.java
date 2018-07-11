package verify;

import util.CheckStronglySubsuming;
import util.SSHOMListener;
import util.SSHOMRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class VerifySSHOM {
  private       SSHOMRunner runner;
  private final String      fname;

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
        System.out.print(CheckStronglySubsuming.isStronglySubsuming(sshomListener) ? "Is strongly subsuming" : "NOT STRONGLY SUBSUMING ****");
        System.out.println(CheckStronglySubsuming.isStrictStronglySubsuming(sshomListener) ? " -- Strict" : "");
      }
    } catch (FileNotFoundException | IllegalAccessException | NoSuchFieldException e) {
      e.printStackTrace();
    }
  }

}
