package verify;

import util.CheckStronglySubsuming;
import util.SSHOMListener;
import util.SSHOMRunner;
import util.SetArithmetic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

public class VerifySSHOM {
  private       SSHOMRunner runner;
  private final String      fname;

  public VerifySSHOM(Class targetClass, Class testClass, String fname) {
    this(new Class[] { targetClass }, new Class[] { testClass }, fname);
  }
  public VerifySSHOM(Class[] targetClass, Class[] testClass, String fname) {
    runner = new SSHOMRunner(targetClass, testClass);
    this.fname = fname;
  }

  public void printVerify() {
    String[] mutants;

    PrintStream out = System.out;
    try {
      System.setOut(new PrintStream(new File("/home/serena/reuse/_temp.txt")));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    try (Scanner in = new Scanner(new File(fname))) {
      while (in.hasNextLine()) {
        mutants = in.nextLine().split(" ");
        SSHOMListener sshomListener = runner.runJunitOnHOMAndFOMs(mutants);

        out.println(Arrays.toString(mutants));
        out.print(CheckStronglySubsuming.isStronglySubsuming(sshomListener.getHomTests(), sshomListener.getFomTests()) ? "Is strongly subsuming" : "NOT STRONGLY SUBSUMING ****");
        out.println(CheckStronglySubsuming.isStrictStronglySubsuming(sshomListener.getHomTests(), sshomListener.getFomTests()) ? " -- Strict" : "");
        out.println(sshomListener.getHomTests());
        out.println(sshomListener.getFomTests());
        out.println();
      }
    } catch (FileNotFoundException | IllegalAccessException | NoSuchFieldException e) {
      e.printStackTrace();
    }
  }

}
