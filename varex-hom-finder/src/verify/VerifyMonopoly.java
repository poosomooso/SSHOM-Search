package verify;

import benchmark.RunBenchmarks;

public class VerifyMonopoly {
  public static void main(String[] args) {
    VerifySSHOM sshom = new VerifySSHOM(RunBenchmarks.TARGET_CLASSES, RunBenchmarks.TEST_CLASSES, "data/found-mutations/monopoly.txt");
    sshom.printVerify();
  }

}
