package verify;

import mutated.triangleAll.Triangle;

import mutated.triangleAll.Triangle_ESTest_improved;

public class VerifyTriangle {

  public static void main(String[] args) {
    VerifySSHOM sshom = new VerifySSHOM(Triangle.class, Triangle_ESTest_improved.class, "data/found-mutations/alltriangle_test.txt");
    sshom.printVerify();
  }


}
