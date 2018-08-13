package verify;

import benchmark.BenchmarkPrograms;

public class VerifyMonopoly {
  public static void main(String[] args) {
    if (BenchmarkPrograms.PROGRAM != BenchmarkPrograms.Program.MONOPOLY) {
      throw new IllegalStateException(
          "BenchmarkPrograms.PROGRAM must be set to MONOPOLY");
    }
    VerifySSHOM sshom = new VerifySSHOM(BenchmarkPrograms.getTargetClasses(), BenchmarkPrograms
        .getTestClasses(), "data/found-mutations/monopoly.txt");
    sshom.printVerify();
  }

}
