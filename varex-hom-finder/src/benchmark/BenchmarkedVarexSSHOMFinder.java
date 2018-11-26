package benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import cmu.conditional.Conditional;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.FeatureModel;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.FExprBuilder;
import gov.nasa.jpf.JPF;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDException;
import net.sf.javabdd.BDDFactory;
import testRunner.RunTests;
import util.BDDSolver;
import util.SATSolver;
import util.SatHelper;
import varex.SATSSHOMExprFactory;
import varex.SSHOMExprFactory;

public class BenchmarkedVarexSSHOMFinder {
	private static final boolean SEPARATE_JVM = true;
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
		System.setProperty("bddValNum", Integer.toString(6_000_000));

		String paths;
		if (RunBenchmarks.RUNNING_LOCALLY) {
			if (JENS) {
				paths = "+classpath=" 
						+ baseDir + "bin," 
						+ baseDir + "code-ut/jars/monopoli100.jar,"
						+ baseDir + "code-ut/jars/commons-validator.jar,"
						+ baseDir + "code-ut/jars/mutated-cli.jar,"
						+ baseDir + "lib/bcel-6.0.jar,"
						+ baseDir + "lib/junit.jar";
			} else {
				paths = "+classpath="
						+ baseDir + "out/production/code-ut,"
						+ baseDir + "code-ut/jars/monopoli100.jar,"
						+ baseDir + "code-ut/jars/commons-validator.jar,"
						+ baseDir + "code-ut/jars/mutated-cli.jar,"
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

		int numberOfTests = tests.size();
		int i = 1;
		for (Entry<Method, FeatureExpr> test : tests.entrySet()) {
			System.out.println(i++ + "/" + numberOfTests + " tests");
			
			if (test.getKey().getName().equals("testGanaJugador")) {
				continue;
			}
			
			if (new File(RunTests.getTestDesc(test.getKey()) + ".txt").exists()) {
				continue;
			}

			if (!Modifier.isAbstract(test.getKey().getDeclaringClass().getModifiers())) {
				if (SEPARATE_JVM) {
				CommandLineRunner.process("java", "-Xmx8g", "-jar", baseDir + "lib/RunJPF.jar",
						"+search.class=.search.RandomSearch", "+bddCacheSize=100000",
						"+bddValNum=1500000", paths, "+choice=MapChoice",
						"+mutants=" + baseDir + "varex-hom-finder/resources/" + BenchmarkPrograms.getFeatureModelResource(),
						// TODO not sure if path works for Jens
						TestRunner.class.getName(), test.getKey().getDeclaringClass().getName(),
						test.getKey().getName());
				} else {
					try {
						JPF.main(new String[] {"+search.class=.search.RandomSearch", "+bddCacheSize=100000",
								"+bddValNum=1500000", paths, "+choice=MapChoice",
								"+mutants=" + baseDir + "varex-hom-finder/resources/" + BenchmarkPrograms.getFeatureModelResource(),
								// TODO not sure if path works for Jens
								TestRunner.class.getName(), test.getKey().getDeclaringClass().getName(),
								test.getKey().getName()});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		benchmarker.timestamp("create features");
		
		mutantExprs = mutantNamesToFeatures(mutants);
		
		benchmarker.timestamp("load f(t)");
		loadTestExpressions(tests);
		
		Map<String, FeatureExpr> stringTests = new HashMap<>();
		for (Entry<Method, FeatureExpr> t: tests.entrySet()) {
			stringTests.put(t.getKey().getName(), t.getValue());
		}
		
		final boolean strict = true;
		// BDD solutions
		Set<Set<String>> solutionsBDD = getBDDSolutions(mutants, stringTests, strict);
		
		// SAT solutions
		Set<Set<String>> solutionsSAT = getSATSolutions(stringTests, strict); 
		
		checkSolutions(solutionsBDD, solutionsSAT);
	}

	private Set<Set<String>> getBDDSolutions(String[] mutants, Map<String, FeatureExpr> stringTests, boolean strict) {
		benchmarker.timestamp("generate FOMs");
		FeatureExpr[] fomExprs = SSHOMExprFactory.genFOMs(mutantExprs, mutants.length);
		
		benchmarker.timestamp("create SSHOM expression");
		
		FeatureExpr finalExpr;
		if (strict) {
			finalExpr = SSHOMExprFactory.getStrictSSHOMExpr(stringTests, mutantExprs, mutantExprs.length);
		} else {
			finalExpr = SSHOMExprFactory.getSSHOMExpr(stringTests, mutantExprs, mutantExprs.length);
		}
		// exclude foms
		for (FeatureExpr m : fomExprs) {
			finalExpr = finalExpr.andNot(m);
		}
		return new BDDSolver(2, benchmarker).getSolutionsBDD((BDDFeatureExpr)finalExpr, mutants, BenchmarkPrograms::homIsValid);
	}

	private Set<Set<String>> getSATSolutions(Map<String, FeatureExpr> stringTests, boolean strict) {
		benchmarker.timestamp("create SAT formula");
		
		final boolean splitExpr1 = true;
		final boolean splitExpr2 = true;
		final boolean splitExpr3 = true;
		
		Collection<File> dimcsFiles;
		if (strict) {
			dimcsFiles = SATSSHOMExprFactory.getStrictSSHOMExpr(stringTests, mutantExprs, mutantExprs.length, splitExpr1, splitExpr2, splitExpr3);
		} else {
			dimcsFiles = SATSSHOMExprFactory.getSSHOMExpr(stringTests, mutantExprs, mutantExprs.length, splitExpr1, splitExpr2);
		}
		SATSSHOMExprFactory.andDimacsFiles(dimcsFiles, mutantExprs, "fullmodel");
		
		FeatureExprFactory.setDefault(FeatureExprFactory.sat());
		FeatureModel featureModel = SatHelper.instance.getModel("fullmodel.dimacs");
		
		benchmarker.timestamp("get SAT solutions");
		return new SATSolver(2, benchmarker).getSolutions(featureModel, mutantExprs, BenchmarkPrograms::homIsValid);
	}

	private void checkSolutions(Set<Set<String>> solutionsBDD, Set<Set<String>> solutionsSAT) {
		boolean success = true;
		System.out.flush();
		for (Set<String> solution : solutionsSAT) {
			if (solutionsBDD.contains(solution)) {
				// good
			} else {
				System.err.println(solution + "not valid");
				success = false;
			}
		}
		
		solutionsBDD.removeAll(solutionsSAT);
		for (Set<String> solution : solutionsBDD) {
			System.err.println(solution + "missed");
			success = false;
		}
		System.err.flush();
		
		if (success) {
			System.out.println("found correct solutions");
		}
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
			String fname = RunTests.getTestDesc(test.getKey()) + ".txt";
			final File file = new File(fname);
			if (file.exists()) {
				try (BufferedReader br = new BufferedReader(new FileReader(fname))) {
					BDD bdd2 = bddFactory.load(br);
					FeatureExpr expr = new BDDFeatureExpr(bdd2);
					test.setValue(expr);
//					file.deleteOnExit();
				} catch (BDDException e) {
					System.err.println(fname);
					e.printStackTrace();
				}
			}
		}
	}

	private SingleFeatureExpr[] mutantNamesToFeatures(String[] mutants) {
		SingleFeatureExpr[] mutantExprs = new SingleFeatureExpr[mutants.length];
		for (int i = 0; i < mutants.length; i++) {
			mutantExprs[i] = Conditional.createFeature(mutants[i]);
		}
		Arrays.sort(mutantExprs, (m1, m2)-> Conditional.getCTXString(m1).substring(1).compareTo(Conditional.getCTXString(m2).substring(1)));
		return mutantExprs;
	}

}
