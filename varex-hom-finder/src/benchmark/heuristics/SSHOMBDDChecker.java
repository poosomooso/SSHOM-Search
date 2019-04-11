package benchmark.heuristics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.Description;

import benchmark.BenchmarkPrograms;
import benchmark.Benchmarker;
import cmu.conditional.Conditional;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.FExprBuilder;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDException;
import net.sf.javabdd.BDDFactory;
import testRunner.RunTests;

public class SSHOMBDDChecker  implements ISSHOMChecker {
	public static SingleFeatureExpr[] mutantExprs = null;

	private enum Machine {
		JENS("C:\\Users\\Jens Meinicke\\git\\mutationtest-varex\\"),
		SERENA("/home/serena/reuse/hom-generator/"),
//		FEATURE_SERVER("/home/feature/serena/"),
		FEATURE_SERVER("/home/ubuntu/");

		private final String path;
		private Machine(String path) {
			this.path = path;
		}
		String getBaseDir() {
			return path;
		}
	}
	private static final Machine machine = Machine.FEATURE_SERVER;
	private static final String  baseDir = machine.getBaseDir();

	Map<String, FeatureExpr> stringTests = new HashMap<>();
	public SSHOMBDDChecker() {
		Class<?>[] testClasses = BenchmarkPrograms.getTestClasses();
		String[] mutants = BenchmarkPrograms.getMutantNames();

		System.setProperty("bddCacheSize", Integer.toString(100000));
		System.setProperty("bddValNum", Integer.toString(6_000_000));

		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());

		Map<Class<?>, Map<Method, FeatureExpr>> tests = new LinkedHashMap<>();
		getTestNames(testClasses, tests);
		filterTests(tests);

		Benchmarker.instance.timestamp("create features");
		
		mutantExprs = mutantNamesToFeatures(mutants);
		
		Benchmarker.instance.timestamp("load f(t)");
		try {
			loadTestExpressions(tests);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		for (Entry<Class<?>, Map<Method, FeatureExpr>> t1: tests.entrySet()) {
			for (Entry<Method, FeatureExpr> t2 : t1.getValue().entrySet()) {
				stringTests.put(t1.getKey().getName() + "." + t2.getKey().getName(), t2.getValue());
			}
		}
			
	}
	
	public boolean isSSHOM(HigherOrderMutant candidate) {
		Collection<Description> allFailingTests = new HashSet<>();
		Set<String> failingTestNames = new HashSet<>();
		
		for (FirstOrderMutant node : candidate) {
			allFailingTests.addAll(node.getTests());
		}
		Collection<Description> failingTestsIntersection = new HashSet<>(allFailingTests);
		for (FirstOrderMutant node : candidate) {
			failingTestsIntersection.retainAll(node.getTests());
		}
		Collection<Description> allFailingTestsNonIntersection = new HashSet<>(allFailingTests);
		allFailingTestsNonIntersection.removeAll(failingTestsIntersection);
		
		scala.collection.mutable.HashSet<String> selectedFeatures = new scala.collection.mutable.HashSet<>();
		for (FirstOrderMutant c : candidate) {
			selectedFeatures.add("CONFIG_" + c.getMutant());
		}
		scala.collection.immutable.Set<String> selectedFeatures2 = selectedFeatures.toSet();
		
		// check that intersecting tests at least one fails
		int failingTests = 0; 
		for (Description test : failingTestsIntersection) {
			String name = test.getClassName() + "." + test.getMethodName();
			failingTestNames.add(name);
			if (failingTests == 0) {
				FeatureExpr testCase = stringTests.get(name);
				boolean result = testCase.evaluate(selectedFeatures2);
				if (result) {
					failingTests++;
				}
			}
		}
		if (failingTests == 0) {
			return false;
		}
		
		
		// check that non intersecting tests do not fail
		for (Description test : allFailingTestsNonIntersection) {
			String name = test.getClassName() + "." + test.getMethodName();
			failingTestNames.add(name);
			FeatureExpr testCase = stringTests.get(name);
			boolean result = testCase.evaluate(selectedFeatures2);
			if (result) {
				return false;
			}
		}
		
		// check that other tests do not fail
		for (Entry<String, FeatureExpr> test : stringTests.entrySet()) {
			if (failingTestNames.contains(test.getKey())) {
				continue;
			}
			boolean result = test.getValue().evaluate(selectedFeatures2);
			if (result) {
				return false;
			}
		}
		
		return true;
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
		File folder = new File("BDDS/" + BenchmarkPrograms.PROGRAM.name());
		
		String[] files = folder.list();
		Set<String> set = new HashSet<>();
		for (String f : files) {
			set.add(f);
		}
		
		final BDDFactory bddFactory = FExprBuilder.bddFactory();
		for (Entry<Class<?>, Map<Method, FeatureExpr>> t1 : tests.entrySet()) {
			for (Entry<Method, FeatureExpr> t2 : t1.getValue().entrySet()) {
				String fname = RunTests.getTestDesc(t1.getKey(), t2.getKey()) + ".txt";
				set.remove(fname);
				final File file = new File(folder, fname);
				if (file.exists()) {
					try (BufferedReader br = new BufferedReader(new FileReader(file))) {
						BDD bdd2 = bddFactory.load(br);
						FeatureExpr expr = new BDDFeatureExpr(bdd2);
						t2.setValue(expr);
					} catch (BDDException e) {
						System.err.println(fname);
						e.printStackTrace();
					}
				} else {
					System.err.println("file not found: " + fname);
				}
			}
		}
		for (String string : set) {
			System.err.println("test missing for: " + string);
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
