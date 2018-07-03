package verify;

import mutated.triangle.Triangle_ESTest;
import original.triangle.Triangle;

public class VerifyTriangle {

  public static void main(String[] args) {
    VerifySSHOM sshom = new VerifySSHOM(Triangle.class, Triangle_ESTest.class, "sshom.txt");
    sshom.printVerify();
  }


}
