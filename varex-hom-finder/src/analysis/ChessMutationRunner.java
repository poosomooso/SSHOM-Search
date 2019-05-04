package analysis;

import benchmark.BenchmarkPrograms;
import benchmark.ChessInfLoopTestProcess;

public class ChessMutationRunner {
  static String[] mutantsToRun = {"_mut916"};

  public static void main(String[] args) {
    BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.CHESS;
    Class<?>[] testClasses = BenchmarkPrograms.getTestClasses();

    Class<?>[] particularClasses = {};
    try {
      particularClasses = new Class[]{ Class.forName("ajedrez.server.pruebas.sobran.dominio.TestGanarPartida") };
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    ChessInfLoopTestProcess.runTests(particularClasses, mutantsToRun);
  }
}
