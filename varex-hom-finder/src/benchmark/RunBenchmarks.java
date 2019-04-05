package benchmark;

import java.io.IOException;

public class RunBenchmarks {

  public static void main(String[] args)
      throws NoSuchFieldException, IOException, IllegalAccessException {
    if (args.length < 2) {
      errAndExit();
      errAndExit();
    }

    String whichProgram = args[0];
    String whichMode = args[1];

    if (whichProgram.equalsIgnoreCase("triangle")) {
      BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.TRIANGLE;
    } else if (whichProgram.equalsIgnoreCase("monopoly")) {
      BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.MONOPOLY;
    } else if (whichProgram.equalsIgnoreCase("cli")) {
      BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.CLI;
    } else if (whichProgram.equalsIgnoreCase("validator")) {
      BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.VALIDATOR;
    } else if (whichProgram.equalsIgnoreCase("chess")) {
      BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.CHESS;
    } else {
      errAndExit();
    }

    if (whichMode.equalsIgnoreCase("naive")) {
      runNaive();
    } else if (whichMode.equalsIgnoreCase("ga")) {
      runEvolutionary();
    } else if (whichMode.equalsIgnoreCase("varex")) {
      runVarex();
    } else if (whichMode.equalsIgnoreCase("smart")) {
        runSmart();
    } else {
      errAndExit();
    }
  }

  private static void errAndExit() {
    System.err.println("A program and a mode must be specified, in that order.");
    System.err.println("Programs : triangle, monopoly, validator, cli, or chess");
    System.err.println("Modes : naive, ga, or varex");
    System.exit(0);
  }

  private static void runNaive()
      throws NoSuchFieldException, IllegalAccessException {
    BenchmarkedNaiveSSHOMFinder naiveSSHOMFinder = new BenchmarkedNaiveSSHOMFinder();
    naiveSSHOMFinder.naiveSSHOMFinder();
  }
  
	private static void runSmart() throws NoSuchFieldException, IllegalAccessException {
		HeuristicsBasedSSHOMFinder naiveSSHOMFinder = new HeuristicsBasedSSHOMFinder();
		naiveSSHOMFinder.run();
	}

  private static void runEvolutionary()
      throws NoSuchFieldException, IllegalAccessException {
    BenchmarkedEvolutionarySSHOMFinder evolutionarySSHOMFinder = new BenchmarkedEvolutionarySSHOMFinder();
    evolutionarySSHOMFinder.evolutionarySSHOMFinder();
  }

  private static void runVarex()
      throws IOException {
    BenchmarkedVarexSSHOMFinder varexSSHOMFinder = new BenchmarkedVarexSSHOMFinder();
    varexSSHOMFinder.varexSSHOMFinder();
  }

}
