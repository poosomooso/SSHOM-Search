package benchmark;

import java.io.IOException;
import java.util.Arrays;

import benchmark.BenchmarkPrograms.Program;

public class RunBenchmarks {

  public static void main(String[] args)
      throws NoSuchFieldException, IOException, IllegalAccessException {
    if (args.length < 2) {
      errAndExit();
    }
    
    Flags.initialize();

    String whichProgram = args[0];
    String whichMode = args[1];

    setProgram(whichProgram);

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

	private static void setProgram(String whichProgram) {
		Program selectedProgram = null;
		for (Program program : BenchmarkPrograms.Program.values()) {
			if (whichProgram.equalsIgnoreCase(program.name())) {
				selectedProgram = program;
				break;
			}
		}
		if (selectedProgram == null) {
			System.err.println("Program not supported: " + whichProgram);
			System.err.println("Use one of: " + Arrays.toString(BenchmarkPrograms.Program.values()));
			
			System.exit(-1);
		} else {
			BenchmarkPrograms.PROGRAM = selectedProgram;
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
