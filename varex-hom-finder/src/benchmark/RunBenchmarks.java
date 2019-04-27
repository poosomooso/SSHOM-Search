package benchmark;

import java.io.IOException;
import java.lang.reflect.Field;

import benchmark.BenchmarkPrograms.Program;

public class RunBenchmarks {

  public static void main(String[] args)
      throws NoSuchFieldException, IOException, IllegalAccessException {
    if (args.length < 2) {
      errAndExit();
    }
    
    initializeFlags();

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
			System.err.println("Program not supported: " + selectedProgram);
			System.exit(-1);
		} else {
			BenchmarkPrograms.PROGRAM = selectedProgram;
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
