package benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import cmu.conditional.Conditional;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.FExprBuilder;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import testRunner.RunTests;
import util.SSHOMRunner;
import varex.SSHOMExprFactory;

public class BenchmarkedVarexSSHOMFinder {
	private Benchmarker benchmarker;
	private SSHOMRunner runner;
	public static SingleFeatureExpr[] mutantExprs = null;

	private static boolean JENS = true;
	private String baseDir = JENS ? "C:/Users/jensm/git/mutationtest-varex/" :
									"/home/serena/reuse/hom-generator/";

	public static class TestRunner {
		
		public static void main(String[] args) {
			if (args.length == 0) {
				RunTests.runTests(BenchmarkPrograms.getTestClasses());
			} else {
				String testName = args[0];
				System.out.println("run test  " + testName);
				RunTests.runTests(BenchmarkPrograms.getTestClasses(), testName); // TODO: pass in class so we don't have to run getTestClasses()
			}

		}
	}

	public BenchmarkedVarexSSHOMFinder() {
		benchmarker = new Benchmarker();
	}

	public void varexSSHOMFinder() throws IOException {
		benchmarker.start();

		Class[] targetClasses = BenchmarkPrograms.getTargetClasses();
		Class[] testClasses = BenchmarkPrograms.getTestClasses();

		System.setProperty("bddCacheSize", Integer.toString(100000));
		System.setProperty("bddValNum", Integer.toString(6_000_000));
		System.setProperty("bddVarNum", Integer.toString(128));
		
		runner = new SSHOMRunner(targetClasses, testClasses);
		String[] mutants = runner.getMutants().toArray(new String[0]);
		String paths;
		if (RunBenchmarks.RUNNING_LOCALLY) {
			if (JENS) {
				paths = "+classpath=" + baseDir + "bin," + baseDir + "code-ut/jars/monopoli100.jar," + baseDir + "lib/bcel-6.0.jar,"
						+ baseDir + "lib/junit.jar";
			} else {
				paths = "+classpath="
						+ baseDir + "out/production/code-ut,"
						+ baseDir + "code-ut/jars/monopoli100.jar,"
						+ baseDir + "code-ut/jars/commons-validator.jar,"
						+ baseDir + "lib/bcel-6.0.jar,"
						+ baseDir + "out/test/code-ut,"
						+ baseDir + "out/production/varex-hom-finder,"
						+ baseDir + "lib/junit.jar";
			}
		} else {
			paths = "+classpath=" + "/home/feature/serena/varex-hom-finder.jar," + "/home/feature/serena/junit-4.12.jar";
		}

		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());

		Map<String, FeatureExpr> tests = new LinkedHashMap<>();

		getTestNames(testClasses, tests);

		for (Entry<String, FeatureExpr> test : tests.entrySet()) {
			
			CommandLineRunner.process("java", "-jar",
					baseDir + "lib/RunJPF.jar",
					"+search.class=.search.RandomSearch",
					"+bddCacheSize=100000",
					"+bddValNum=1500000",
					"+bddVarNum=128",
					paths, "+choice=MapChoice", 
					"+mutants="+ baseDir + "varex-hom-finder\\resources\\mutantgroups\\triangle.txt",
					TestRunner.class.getName(), test.getKey());
		}
		benchmarker.timestamp("create features");
		
		Conditional.createAndGetFeatures(baseDir + "varex-hom-finder\\resources\\mutantgroups\\triangle.txt").toArray(mutants);
		mutantExprs = mutantNamesToFeatures(mutants);
		
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
	
	@SuppressWarnings("unchecked")
	private Set<List<String>> getSolutions(FeatureExpr expr, String[] mutants) {
		List<byte[]> solutions = (List<byte[]>)((BDDFeatureExpr)expr).bdd().allsat();
		Set<List<String>> allSolutions = new LinkedHashSet<>();
		for (byte[] s : solutions) {
			getSolutions(s, mutants, allSolutions, new ArrayList<>(), 1);
		}
		return allSolutions;
	}

	/**
	 * In the byte arrays, -1 means dont-care, 0 means 0, and 1 means 1.
	 * So we need to expand the given solution.
	 * @param solutions
	 * @param mutants
	 */
	private void getSolutions(byte[] solutions, String[] mutants, Set<List<String>> allSolutions, List<String> selections, int start) {
		if (selections.size() >= 2 && !isValid(selections)) {
			return;
		}
		for (int i = start; i <= mutants.length; i++) {
			byte selection = solutions[i];
			if (selection != 0) {
				if (selection == -1) {
					byte[] copy = new byte[mutants.length + 1];
					for (int j = i + 1; j <= mutants.length; j++) {
						copy[j] = solutions[j];
					}
					getSolutions(copy, mutants, allSolutions, new ArrayList<>(selections), i + 1);
				}
				
				selections.add(mutants[i - 1]);
				if (selections.size() >= 2 && !isValid(selections)) {
					return;
				}
				solutions[i] = 1;
			}
		}
		allSolutions.add(selections);
		benchmarker.timestamp(allSolutions.size() + " " + selections);
	}

	private void getTestNames(Class<?>[] testClasses, Map<String, FeatureExpr> tests) {
		for (Class<?> c : testClasses) {
			final List<Method> methods = Arrays.stream(c.getMethods())
					.filter(m -> m.getAnnotation(Test.class) != null)
					.collect(Collectors.toList());

			for (Method m : methods) {
				tests.put(m.getName(), FeatureExprFactory.False());
			}
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

	private boolean isValid(List<String> selections) {
		int numMutantGroups = BenchmarkPrograms.getMakeshiftFeatureModel().size();
		boolean[] check = new boolean[numMutantGroups];
		for (String mutation : selections) {
			if (!check(check, mutation)) {
				return false;
			}
		}
		return true;
	}

	private boolean check(boolean[] check, String mutant) {
		int id = BenchmarkPrograms.getMakeshiftFeatureModel().get(mutant);
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
			mutantExprs[i] = Conditional.createFeature(mutants[i]);
		}
		return mutantExprs;
	}

}
