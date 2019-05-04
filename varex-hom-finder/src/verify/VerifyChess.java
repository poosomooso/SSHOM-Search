package verify;

import benchmark.BenchmarkPrograms;

public class VerifyChess {
  static String fname = "data/found-mutations/chess-19-04-28.txt";
  public static void main(String[] args) {
    BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.CHESS;
    VerifySSHOM sshom = new VerifySSHOM(BenchmarkPrograms.getTargetClasses(), BenchmarkPrograms
        .getTestClasses(), fname);
    sshom.printVerify(",");
  }
}
