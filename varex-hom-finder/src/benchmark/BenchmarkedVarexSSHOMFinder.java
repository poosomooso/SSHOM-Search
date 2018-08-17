package benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.junit.Test;

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
	public static SingleFeatureExpr[] mutantExprs = null;

	private static boolean JENS = false;
	private String baseDir = JENS ? "C:\\Users\\Jens Meinicke\\git\\mutationtest-varex\\" :
									"/home/serena/reuse/hom-generator/";

	public static class TestRunner {
		
		public static void main(String[] args) throws ClassNotFoundException {
			if (args.length < 2) {
				RunTests.runTests(BenchmarkPrograms.getTestClasses());
			} else {
				String className = args[0];
				String testName = args[1];
				System.out.println("run test  " + testName);
				RunTests.runTests(Class.forName(className), testName); // TODO: pass in class so we don't have to run getTestClasses()
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
		String[] mutants = BenchmarkPrograms.getMutantNames();

		System.setProperty("bddCacheSize", Integer.toString(100000));
		System.setProperty("bddValNum", Integer.toString(2_000_000));
		System.setProperty("bddVarNum", Integer.toString(128));

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

		Map<Method, FeatureExpr> tests = new LinkedHashMap<>();

		getTestNames(testClasses, tests);

		for (Entry<Method, FeatureExpr> test : tests.entrySet()) {
			if (test.getKey().getName().equals("testGanaJugador")) continue;
			
			CommandLineRunner.process("java", "-jar",
					baseDir + "lib/RunJPF.jar",
					"+search.class=.search.RandomSearch",
					"+bddCacheSize=100000",
					"+bddValNum=1500000",
					"+bddVarNum=128",
					paths, "+choice=MapChoice",
					TestRunner.class.getName(), test.getKey().getDeclaringClass().getName(), test.getKey().getName());
		}
		benchmarker.timestamp("create features");
		createFeatures(mutants);
		
		benchmarker.timestamp("load f(t)");
		loadTestExpressions(tests);
		
		benchmarker.timestamp("generate FOMs");
		FeatureExpr[] fomExprs = SSHOMExprFactory.genFOMs(mutantExprs, mutants.length);
		
		benchmarker.timestamp("create SSHOM expression");
		Map<String, FeatureExpr> stringTests = new HashMap<>();
		for (Entry<Method, FeatureExpr> t: tests.entrySet()) {
			stringTests.put(t.getKey().getName(), t.getValue());
		}
		FeatureExpr finalExpr = SSHOMExprFactory.getSSHOMExpr(stringTests, mutantExprs, mutantExprs.length);

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

	private void getTestNames(Class<?>[] testClasses, Map<Method, FeatureExpr> tests) {
		for (Class<?> c : testClasses) {
			final List<Method> methods = Arrays.stream(c.getMethods())
					.filter(m -> m.getAnnotation(Test.class) != null)
					.collect(Collectors.toList());

			for (Method m : methods) {
				tests.put(m, FeatureExprFactory.False());
			}
		}

	}

	private void loadTestExpressions(Map<Method, FeatureExpr> tests) throws IOException {
		final BDDFactory bddFactory = FExprBuilder.bddFactory();
		for (Entry<Method, FeatureExpr> test : tests.entrySet()) {
			final File file = new File(test.getKey().getName() + ".txt");
			if (file.exists()) {
				try (BufferedReader br = new BufferedReader(new FileReader(test.getKey().getName() + ".txt"))) {
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
		mutantExprs = mutantNamesToFeatures(mutants);
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
			mutantExprs[i] = FeatureExprFactory.createDefinedExternal(mutants[i]);
		}
		return mutantExprs;
	}

}
