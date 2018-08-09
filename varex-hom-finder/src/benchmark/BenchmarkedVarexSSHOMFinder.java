package benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.FExprBuilder;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.immutable.List;
import testRunner.RunTests;
import util.SSHOMRunner;
import varex.SSHOMExprFactory;
import varex.SatisfiableAssignmentIterator;

public class BenchmarkedVarexSSHOMFinder {
	private Benchmarker benchmarker;
	private SSHOMRunner runner;
	public static SingleFeatureExpr[] mutantExprs = null;

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
			String baseDir = "C:\\Users\\Jens Meinicke\\git\\mutationtest-varex\\";
			paths = "+classpath=" + baseDir + "bin," + baseDir + "code-ut/jars/monopoli100.jar," + baseDir + "lib/bcel-6.0.jar,"
					+ baseDir + "lib/junit.jar";
		} else {
			paths = "+classpath=" + "/home/feature/serena/varex-hom-finder.jar," + "/home/feature/serena/junit-4.12.jar";
		}

		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());

		Map<String, FeatureExpr> tests = new LinkedHashMap<>();

		getTestNames(tests);// XXX only implemented for triangle

		for (Entry<String, FeatureExpr> test : tests.entrySet()) {
//    	JPF.main(new String[] { 
//        		"+choice=MapChoice",
//        		"+featuremodel= model.dimacs",
//        		"+search.class=.search.RandomSearch", paths,
//             TestRunner.class.getName(), test.getKey()});

			CommandLineRunner.process("java", "-jar", "C:\\Users\\Jens Meinicke\\git\\VarexJ\\VarexJ\\build\\libs\\RunJPF.jar",
					paths, "+choice=MapChoice", TestRunner.class.getName(), test.getKey());
		}

		createFeatures(mutants);
		loadTestExpressions(tests);

		FeatureExpr[] fomExprs = SSHOMExprFactory.genFOMs(mutantExprs, mutants.length);

		FeatureExpr finalExpr = SSHOMExprFactory.getSSHOMExpr(tests, mutantExprs, mutantExprs.length);

		//exclude foms
		for (FeatureExpr m : fomExprs) {
			finalExpr = finalExpr.andNot(m);
		}

		SatisfiableAssignmentIterator iterator = new SatisfiableAssignmentIterator(mutantExprs, finalExpr);
		int solutions = 0;
		while (iterator.hasNext()) {
			Tuple2<List<SingleFeatureExpr>, List<SingleFeatureExpr>> next = iterator.next();
			if (isValid(next)) {
				System.out.println(solutions++);
				benchmarker.timestamp(SSHOMExprFactory.parseAssignment(next).toString());
			}
		}
		System.out.println(solutions);
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
					file.delete();
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
			int id = -1;

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
				throw new RuntimeException(e.toString());
			}
			if (check[id]) {
				return false;
			} else {
				check[id] = true;
			}
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
