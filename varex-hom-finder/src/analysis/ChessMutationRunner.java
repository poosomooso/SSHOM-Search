package analysis;

import benchmark.BenchmarkPrograms;
import benchmark.ChessInfLoopTestProcess;

public class ChessMutationRunner {
  static String[] mutantsToRun = {"_mut0", "_mut4"};

  public static void main(String[] args) {
    Class<?>[] testClasses = BenchmarkPrograms.getTestClasses();

    Class<?>[] particularClasses = {};
    try {
      particularClasses = new Class[]{ Class.forName("ajedrez.server.pruebas.mutacion.dominio.TestGanarPartida") };
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    ChessInfLoopTestProcess.runTests(particularClasses, mutantsToRun);
  }
}
