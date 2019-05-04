package verify;

import benchmark.BenchmarkPrograms;

public class VerifyMonopoly {
  public static void main(String[] args) {
    BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.MONOPOLY;
    VerifySSHOM sshom = new VerifySSHOM(BenchmarkPrograms.getTargetClasses(), BenchmarkPrograms
        .getTestClasses(), "data/found-mutations/monopoly.txt");
    sshom.printVerify();
  }

}
