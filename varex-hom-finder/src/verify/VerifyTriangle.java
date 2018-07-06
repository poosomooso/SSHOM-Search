package verify;

import manual.triangle.Triangle;
import manual.triangle.Triangle_ESTest_linecoverage;

public class VerifyTriangle {

  public static void main(String[] args) {
    VerifySSHOM sshom = new VerifySSHOM(Triangle.class, Triangle_ESTest_linecoverage.class, "data/found-mutations/evo-sshom.txt");
    sshom.printVerify();
  }


}
