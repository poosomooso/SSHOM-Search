package varex;

import triangle.Triangle;
import triangle.testTriangleExhaustive;

public class VerifyTriangle {

  public static void main(String[] args) {
    VerifySSHOM sshom = new VerifySSHOM(Triangle.class, testTriangleExhaustive.class, "sshom.txt");
    sshom.printVerify();
  }


}
