package benchmark;

import java.io.IOException;
import java.lang.reflect.Field;

public class RunBenchmarks {

  public static void main(String[] args)
      throws NoSuchFieldException, IOException, IllegalAccessException {
    if (args.length < 2) {
      errAndExit();
    }
    
    initializeFlags();

    String whichProgram = args[0];
    String whichMode = args[1];

    
    // TODO revise this
    if (whichProgram.equalsIgnoreCase(BenchmarkPrograms.Program.TRIANGLE.name())) {
      BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.TRIANGLE;
    } else if (whichProgram.equalsIgnoreCase(BenchmarkPrograms.Program.MONOPOLY.name())) {
      BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.MONOPOLY;
    } else if (whichProgram.equalsIgnoreCase(BenchmarkPrograms.Program.CLI.name())) {
      BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.CLI;
    } else if (whichProgram.equalsIgnoreCase(BenchmarkPrograms.Program.VALIDATOR.name())) {
      BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.VALIDATOR;
    } else if (whichProgram.equalsIgnoreCase(BenchmarkPrograms.Program.CHESS.name())) {
      BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.CHESS;
    } else if (whichProgram.equalsIgnoreCase(BenchmarkPrograms.Program.MATH.name())) {
        BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.MATH;
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

	private static void initializeFlags() {
		System.out.println("### initialize: " + Flags.class.getName());
		try {
			Field[] flags = Flags.class.getFields();
			for (Field flag : flags) {
				String value = System.getProperty(flag.getName(), Boolean.toString(flag.getBoolean(null)));
				flag.setBoolean(null, Boolean.parseBoolean(value));
				System.out.println("### " + flag.getName() + ": " + value);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

private static void errAndExit() {
    System.err.println("A program and a mode must be specified, in that order.");
    System.err.println("Programs : triangle, monopoly, validator, cli, or chess");
    System.err.println("Modes : naive, ga, smart, or varex");
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
