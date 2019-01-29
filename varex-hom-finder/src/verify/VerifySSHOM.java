package verify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;

import util.CheckStronglySubsuming;
import util.SSHOMListener;
import util.SSHOMRunner;

public class VerifySSHOM {
  private       SSHOMRunner runner;
  private final String      fname;

  public VerifySSHOM(Class<?> targetClass, Class<?> testClass, String fname) {
    this(new Class[] { targetClass }, new Class[] { testClass }, fname);
  }
  public VerifySSHOM(Class<?>[] targetClass, Class<?>[] testClass, String fname) {
    runner = new SSHOMRunner(targetClass, testClass);
    this.fname = fname;
  }

  public void printVerify() {
    String[] mutants;

    PrintStream out = System.out;

//    System.setOut(new PrintStream(new OutputStream() {
//      public void write(int b) {
//        // NO-OP
//      }
//    }));


    try (Scanner in = new Scanner(new File(fname))) {
      while (in.hasNextLine()) {
        mutants = in.nextLine().split(" ");
        SSHOMListener sshomListener = runner.runJunitOnHOMAndFOMs(mutants);

        out.println(Arrays.toString(mutants));
        out.print(CheckStronglySubsuming.isStronglySubsuming(sshomListener.getHomTests(), sshomListener.getFomTests()) ? "Is strongly subsuming" : "NOT STRONGLY SUBSUMING ****");
        out.println(CheckStronglySubsuming.isStrictStronglySubsuming(sshomListener.getHomTests(), sshomListener.getFomTests()) ? " -- Strict" : "");
//        out.println(sshomListener.getHomTests());
//        out.println(SetArithmetic.getIntersection(sshomListener.getFomTests()));
        out.println();
      }
    } catch (FileNotFoundException | IllegalAccessException | NoSuchFieldException e) {
      e.printStackTrace();
    }
  }

}
