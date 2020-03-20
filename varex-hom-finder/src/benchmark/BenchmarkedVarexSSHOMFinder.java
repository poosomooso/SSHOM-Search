package benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.jline.utils.InputStreamReader;
import org.junit.Test;

import benchmark.VarexOptions.SOLVER;
import cmu.conditional.Conditional;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.FExprBuilder;
import me.tongfei.progressbar.ProgressBar;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDException;
import net.sf.javabdd.BDDFactory;
import solver.bdd.BDDSolver;
import solver.sat.SATSolver;
import testRunner.RunTests;
import util.ConfigLoader;
import util.ProgressBarFactory;
import varex.SATSSHOMExprFactory;
import varex.SSHOMExprFactory;

public class BenchmarkedVarexSSHOMFinder {
	
	static {
		ConfigLoader.initialize(VarexOptions.class);
	}

	public static SingleFeatureExpr[] mutantExprs = null;

	public void varexSSHOMFinder() throws IOException {
		Benchmarker.instance.start();

		Class<?>[] testClasses = BenchmarkPrograms.getTestClasses();
		String[] mutants = BenchmarkPrograms.getMutantNames();

		System.setProperty("bddCacheSize", Integer.toString(Math.max(Integer.parseInt(System.getProperty("bddCacheSize", "0")), 100000)));
		System.setProperty("bddValNum", Integer.toString(Math.max(Integer.parseInt(System.getProperty("bddValNum", "0")), 6_000_000)));

		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());

		Map<Class<?>, Map<Method, FeatureExpr>> tests = new LinkedHashMap<>();
		getTestNames(testClasses, tests);
		filterTests(tests);

		Benchmarker.instance.timestamp("create features");
		
		mutantExprs = mutantNamesToFeatures(mutants);
		
		Benchmarker.instance.timestamp("load f(t)");
		loadTestExpressions(tests);
		
		Map<String, FeatureExpr> stringTests = new HashMap<>();
		for (Entry<Class<?>, Map<Method, FeatureExpr>> t1: tests.entrySet()) {
			for (Entry<Method, FeatureExpr> t2 : t1.getValue().entrySet()) {
				stringTests.put(t1.getKey().getName() + "." + t2.getKey().getName(), t2.getValue());
			}
		}
		
		final boolean strict = VarexOptions.isComputeStrict();
		// BDD solutions
		
		SOLVER solver = VarexOptions.getSolver();
		if (solver == SOLVER.BDD) {
			@SuppressWarnings("unused")
			Set<Set<String>> solutionsBDD = getBDDSolutions(mutants, stringTests, strict);
		} else {
			// SAT solutions
			@SuppressWarnings("unused")
			Set<Set<String>> solutionsSAT = getSATSolutions(mutants, stringTests, strict); 
		}
		
//		checkSolutions(solutionsBDD, solutionsSAT);
	}

	private void filterTests(Map<Class<?>, Map<Method, FeatureExpr>> tests) {
		int size = 0;
		for (Map<Method, FeatureExpr> t : tests.values()) {
			size += t.size();
		}
		System.out.println("All tests: " + size);
		
		tests.entrySet().forEach(t1 -> t1.getValue().entrySet().removeIf(
				 entry -> (entry.getKey().getName().contains("testSerial")) // serialization for varexc
				 	|| (entry.getKey().getName().toLowerCase().contains("serialization") // serialization for varexc
		)));
		
		tests.entrySet().removeIf(
				entry -> entry.getValue().isEmpty() 
					|| Modifier.isAbstract(entry.getKey().getModifiers())
					|| entry.getKey().getSimpleName().equals("ValuesTest"));
		
		size = 0;
		for (Map<Method, FeatureExpr> t : tests.values()) {
			size += t.size();
		}
		System.out.println("Remaining tests after filter: " + size);
	}

	private Set<Set<String>> getBDDSolutions(String[] mutants, Map<String, FeatureExpr> stringTests, boolean strict) {
		Benchmarker.instance.timestamp("create SSHOM expression");
		
		FeatureExpr finalExpr;
		if (strict) {
			finalExpr = SSHOMExprFactory.getStrictSSHOMExpr(stringTests, mutantExprs, mutantExprs.length);
		} else {
			finalExpr = SSHOMExprFactory.getSSHOMExpr(stringTests, mutantExprs, mutantExprs.length);
		}

		BigInteger count = new BDDSolver(2).getSolutionsCount((BDDFeatureExpr)finalExpr, mutants);
		Benchmarker.instance.timestamp("Number of solutions:" + count);
		return new BDDSolver(2).getSolutions((BDDFeatureExpr)finalExpr, mutants, BenchmarkPrograms::homIsValid);
	}
	
	private Set<Set<String>> getSATSolutions(String[] mutants, Map<String, FeatureExpr> stringTests, boolean strict) {
		Benchmarker.instance.timestamp("create SAT formula");
		
		boolean reuseExisting = false;
		if (new File("fullmodel.dimacs").exists()) {
			System.out.println("fullmodel.dimacs already exists! Do you want to reuse the model? Y/N:");
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)); ) {
		        String line = reader.readLine();
		        if (line.trim().equalsIgnoreCase("Y")) {
		        	Benchmarker.instance.timestamp("Reusing existing model.");
		        	reuseExisting = true;
		        }
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		if (!reuseExisting) {
			Collection<File> dimcsFiles;
			if (strict) {
				dimcsFiles = SATSSHOMExprFactory.getStrictSSHOMExpr(stringTests, mutantExprs, mutantExprs.length);
			} else {
				dimcsFiles = SATSSHOMExprFactory.getSSHOMExpr(stringTests, mutantExprs, mutantExprs.length);
			}
			SATSSHOMExprFactory.andDimacsFiles(dimcsFiles, mutantExprs, "fullmodel");
		}
		
		Benchmarker.instance.timestamp("get SAT solutions");
		Set<Set<String>> solutions = new SATSolver(2).getSolutions("fullmodel.dimacs", mutants);
		System.out.println("Number of solutions:" + solutions.size());
		return solutions;
	}

	/**
	 * Debugging purpose only: Compares solutions generated using BDD with solutions from SAT.
	 * @param solutionsBDD
	 * @param solutionsSAT
	 */
	@SuppressWarnings("unused")
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
	
	private void getTestNames(Class<?>[] testClasses, Map<Class<?>, Map<Method, FeatureExpr>> tests) {
		for (Class<?> c : testClasses) {
			final List<Method> methods = Arrays.stream(c.getMethods())
					.filter(m -> m.getAnnotation(Test.class) != null)
					.collect(Collectors.toList());

			Map<Method, FeatureExpr> currentTests = new LinkedHashMap<>();
			for (Method m : methods) {
				currentTests.put(m, FeatureExprFactory.False());
			}
			tests.put(c, currentTests);
		}

	}
	
	private void loadTestExpressions(Map<Class<?>, Map<Method, FeatureExpr>> tests) throws IOException {
		final BDDFactory bddFactory = FExprBuilder.bddFactory();
		
		int size = 0;
		for (Entry<Class<?>, Map<Method, FeatureExpr>> t1 : tests.entrySet()) {
			size += t1.getValue().size();
		}
				
		try (ProgressBar pb = ProgressBarFactory.create("load test expressions", size)) {
			for (Entry<Class<?>, Map<Method, FeatureExpr>> t1 : tests.entrySet()) {
				for (Entry<Method, FeatureExpr> t2 : t1.getValue().entrySet()) {
					pb.step();
					String fname = RunTests.getTestDesc(t1.getKey(), t2.getKey()) + ".txt";
					InputStream res = getClass().getResourceAsStream("/BDDS/" + BenchmarkPrograms.PROGRAM.name() + '/' + fname);
					if (res != null) {
						try (BufferedReader br = new BufferedReader(new InputStreamReader(res))) {
							BDD bdd2 = bddFactory.load(br);
							FeatureExpr expr = new BDDFeatureExpr(bdd2);
							t2.setValue(expr);
						} catch (BDDException e) {
							System.err.println(fname);
							e.printStackTrace();
						}
					}
				}
			}
		}
	}


	private SingleFeatureExpr[] mutantNamesToFeatures(String[] mutants) {
		SingleFeatureExpr[] mutantExprs = new SingleFeatureExpr[mutants.length];
		for (int i = 0; i < mutants.length; i++) {
			mutantExprs[i] = Conditional.createFeature(mutants[i]);
		}
		return mutantExprs;
	}

}
