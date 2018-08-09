package benchmark;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.FeatureModel;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.FExprBuilder;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.vm.JPF_gov_nasa_jpf_ConsoleOutputStream;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.immutable.List;
import testRunner.RunTests;
import util.SSHOMRunner;
import varex.SSHOMExprFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class BenchmarkedVarexSSHOMFinder {
	private Benchmarker benchmarker;
	private SSHOMRunner runner;
	public static SingleFeatureExpr[] mutantExprs = null;

	private String baseDir = "/home/serena/reuse/hom-generator/";

	public static class TestRunner {
		public static void main(String[] args) {
			if (args.length == 0) {
				RunTests.runTests(RunBenchmarks.TEST_CLASSES);
			} else {
				String testName = args[0];
				System.out.println("run test  " + testName);
				RunTests.runTests(RunBenchmarks.TEST_CLASSES, testName);
			}

		}
	}

	public BenchmarkedVarexSSHOMFinder() {
		benchmarker = new Benchmarker();
	}

	public void varexSSHOMFinder(Class[] targetClasses, Class[] testClasses) throws IOException {
		benchmarker.start();
		runner = new SSHOMRunner(targetClasses, testClasses);
		String[] mutants = runner.getMutants().toArray(new String[0]);
		String paths;
		if (RunBenchmarks.RUNNING_LOCALLY) {
			paths = "+classpath="
					+ baseDir + "out/production/code-ut,"
					+ baseDir + "code-ut/jars/monopoli100.jar,"
					+ baseDir + "code-ut/jars/commons-validator.jar,"
					+ baseDir + "lib/bcel-6.0.jar,"
					+ baseDir + "out/test/code-ut,"
					+ baseDir + "out/production/varex-hom-finder,"
					+ baseDir + "lib/junit.jar";
		} else {
			paths = "+classpath=" + "/home/feature/serena/varex-hom-finder.jar," + "/home/feature/serena/junit-4.12.jar";
		}

		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());

		Map<String, FeatureExpr> tests = new LinkedHashMap<>();

		getTestNames(tests);// XXX only implemented for triangle

		for (Entry<String, FeatureExpr> test : tests.entrySet()) {
			CommandLineRunner.process("java", "-jar",
					baseDir + "lib/RunJPF.jar",
					"+search.class=.search.RandomSearch",
					paths, "+choice=MapChoice", TestRunner.class.getName(), test.getKey());
		}
		benchmarker.timestamp("create features");
		createFeatures(mutants);
		
		benchmarker.timestamp("load f(t)");
		loadTestExpressions(tests);
		
		benchmarker.timestamp("generate FOMs");
		FeatureExpr[] fomExprs = SSHOMExprFactory.genFOMs(mutantExprs, mutants.length);
		
		benchmarker.timestamp("create SSHOM expression");
		FeatureExpr finalExpr = SSHOMExprFactory.getSSHOMExpr(tests, mutantExprs, mutantExprs.length);

		// exclude foms
		for (FeatureExpr m : fomExprs) {
			finalExpr = finalExpr.andNot(m);
		}

		benchmarker.timestamp("get solutions");
		getSolutions(finalExpr, mutants);
	}
	
	int numSolutions = 0;

	@SuppressWarnings("unchecked")
	private Set<java.util.List<String>> getSolutions(FeatureExpr expr, String[] mutants) {
		java.util.List<byte[]> solutions = (java.util.List<byte[]>)((BDDFeatureExpr)expr).bdd().allsat();
		Set<java.util.List<String>> allSolutions = new LinkedHashSet<>();
		for (byte[] s : solutions) {
			getSolutions(s, mutants, allSolutions);
		}
		return allSolutions;
	}

	/**
	 * In the byte arrays, -1 means dont-care, 0 means 0, and 1 means 1.
	 * So we need to expand the given solution.
	 * @param solutions
	 * @param mutants
	 */
	private void getSolutions(byte[] solutions, String[] mutants, Set<java.util.List<String>> allSolutions) {
		java.util.List<String> selections = new ArrayList<>();
		for (int i = 1; i <= mutants.length; i++) {
			byte selection = solutions[i];
			if (selection != 0) {
				if (selection == -1) {
					byte[] copy = Arrays.copyOf(solutions, mutants.length + 1);
					copy[i] = 0;
					getSolutions(copy, mutants, allSolutions);
				}
				
				selections.add(mutants[i - 1]);
				solutions[i] = 1;
			}
		}
		if (isValid(selections)) {
			allSolutions.add(selections);
			benchmarker.timestamp(numSolutions++ + " " + selections);
		}
	}

	// TODO implement this
	private void getTestNames(Map<String, FeatureExpr> tests) {
		for (int i = 0; i <= 20; i++) {
			tests.put("test" + (i < 10 ? "0" : "") + i, FeatureExprFactory.False());
		}
		for (int i = 0; i <= 4; i++) {
			tests.put("testCustom" + i, FeatureExprFactory.False());
		}
	}

	private void loadTestExpressions(Map<String, FeatureExpr> tests) throws IOException {
		final BDDFactory bddFactory = FExprBuilder.bddFactory();
		for (Entry<String, FeatureExpr> test : tests.entrySet()) {
			final File file = new File(test.getKey() + ".txt");
			if (file.exists()) {
				try (BufferedReader br = new BufferedReader(new FileReader(test.getKey() + ".txt"))) {
					BDD bdd2 = bddFactory.load(br);
					FeatureExpr expr = new BDDFeatureExpr(bdd2);
					test.setValue(expr);
					file.deleteOnExit();
				}
			}
		}
	}

	/**
	 * Generates the features. 
	 * This method essentially initializes the BDD variables.
	 * It is importnat that the features are generated in the same order as when executing with Varex 
	 */
	private void createFeatures(String[] mutants) {
		Arrays.sort(mutants, (o1, o2) -> Integer.compare(Integer.parseInt(o1.substring(1)), Integer.parseInt(o2.substring(1))));
		mutantExprs = mutantNamesToFeatures(mutants);
	}

	private boolean isValid(Tuple2<List<SingleFeatureExpr>, List<SingleFeatureExpr>> next) {
		boolean[] check = new boolean[33];
		for (SingleFeatureExpr e : JavaConversions.asJavaIterable(next._1)) {
			int number = 0;
			try {
				if (e.feature().startsWith("CONFIG_")) {
					number = Integer.parseInt(e.feature().substring("CONFIG_m".length()));
				} else {
					number = Integer.parseInt(e.feature().substring("m".length()));
				}
			} catch (Exception ex) {
				System.out.println(e.feature());
				throw ex;
			}
			if (!check(check, number)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isValid(java.util.List<String> selections) {
		boolean[] check = new boolean[33];
		for (String mutation : selections) {
			int number = Integer.parseInt(mutation.substring("m".length()));
			if (!check(check, number)) {
				return false;
			}
		}
		return true;
	}

	private boolean check(boolean[] check, int number) {
		int id = -1;
		if (number <= 4) {
			id = 0;
		} else if (number <= 9) {
			id = 1;
		} else if (number <= 10) {
			id = 2;
		} else if (number <= 15) {
			id = 3;
		} else if (number <= 16) {
			id = 4;
		} else if (number <= 21) {
			id = 5;
		} else if (number <= 25) {
			id = 6;
		} else if (number <= 30) {
			id = 7;
		} else if (number <= 34) {
			id = 8;
		} else if (number <= 39) {
			id = 9;
		} else if (number <= 43) {
			id = 10;
		} else if (number <= 48) {
			id = 11;
		} else if (number <= 52) {
			id = 12;
		} else if (number <= 57) {
			id = 13;
		} else if (number <= 61) {
			id = 14;
		} else if (number <= 66) {
			id = 15;
		} else if (number <= 67) {
			id = 16;
		} else if (number <= 71) {
			id = 17;
		} else if (number <= 76) {
			id = 18;
		} else if (number <= 77) {
			id = 19;
		} else if (number <= 82) {
			id = 20;
		} else if (number <= 87) {
			id = 21;
		} else if (number <= 91) {
			id = 22;
		} else if (number <= 96) {
			id = 23;
		} else if (number <= 97) {
			id = 24;
		} else if (number <= 102) {
			id = 25;
		} else if (number <= 106) {
			id = 26;
		} else if (number <= 111) {
			id = 27;
		} else if (number <= 112) {
			id = 28;
		} else if (number <= 117) {
			id = 29;
		} else if (number <= 121) {
			id = 30;
		} else if (number <= 126) {
			id = 31;
		} else if (number <= 127) {
			id = 32;
		} else {
			throw new RuntimeException(number + "");
		}
		if (check[id]) {
			return false;
		} else {
			check[id] = true;
		}
		return true;
	}

	private SingleFeatureExpr[] mutantNamesToFeatures(String[] mutants) {
		SingleFeatureExpr[] mutantExprs = new SingleFeatureExpr[mutants.length];
		for (int i = 0; i < mutants.length; i++) {
			mutantExprs[i] = FeatureExprFactory.createDefinedExternal(mutants[i]);
		}
		return mutantExprs;
	}

}
