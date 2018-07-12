package verify;

import mutated.triangle.Triangle;

import mutated.triangle.testTriangleExhaustive;

public class VerifyTriangle {

  public static void main(String[] args) {
    VerifySSHOM sshom = new VerifySSHOM(Triangle.class, testTriangleExhaustive.class, "data/found-mutations/evo-automutants.txt");
    sshom.printVerify();
  }


}
