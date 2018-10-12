package benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.sf.javabdd.BDDException;
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
import testRunner.TestRunner;
import varex.SSHOMExprFactory;

public class BenchmarkedVarexSSHOMFinder {
	private Benchmarker benchmarker;
	public static SingleFeatureExpr[] mutantExprs = null;

	private enum Machine {
		JENS,
		SERENA,
		FEATURE_SERVER;

		String getBaseDir() {
			switch (this) {
			case JENS: return "C:\\Users\\Jens Meinicke\\git\\mutationtest-varex\\";
			case SERENA: return "/home/serena/reuse/hom-generator/";
//			case FEATURE_SERVER: return "/home/feature/serena/";
			case FEATURE_SERVER: return "/home/serena/";
			}
			return "";
		}
	}
	private static Machine machine = Machine.FEATURE_SERVER;
	private        String  baseDir = machine.getBaseDir();

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

		String paths = getVarexClasspaths(), mutantFile = getMutantFile(), jpfPath = getJpfPath(), jvmClasspath = getJvmClasspath();

		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());

		Map<Method, FeatureExpr> tests = new LinkedHashMap<>();

		getTestNames(testClasses, tests);

		for (Entry<Method, FeatureExpr> test : tests.entrySet()) {
			if (test.getKey().getName().equals("testGanaJugador"))
				continue;
			if (Modifier.isAbstract(test.getKey().getDeclaringClass().getModifiers()))
				continue;

      if (jvmClasspath == null) {
				CommandLineRunner.process("java", "-jar", jpfPath,
						"+search.class=.search.RandomSearch", "+bddCacheSize=100000",
						"+bddValNum=1500000", paths, "+choice=MapChoice", "+mutants=" + mutantFile,
						TestRunner.class.getName(), test.getKey().getDeclaringClass().getName(),
						test.getKey().getName());
			} else {
				CommandLineRunner.process("java", "-classpath", jvmClasspath, "-jar", jpfPath,
						"+search.class=.search.RandomSearch", "+bddCacheSize=100000",
						"+bddValNum=1500000", paths, "+choice=MapChoice", "+mutants=" + mutantFile,
						TestRunner.class.getName(), test.getKey().getDeclaringClass().getName(),
						test.getKey().getName());
			}
		}
		benchmarker.timestamp("create features");

		mutantExprs = mutantNamesToFeatures(mutants);

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

	private String getVarexClasspaths() {
		switch (machine) {
		case FEATURE_SERVER: return "+classpath=" + baseDir + "hom-generator.jar," + baseDir + "junit-4.12.jar," + baseDir + "lib/bcel-6.0.jar";
		case JENS: return  "+classpath=" + baseDir + "bin," + baseDir + "code-ut/jars/monopoli100.jar," + baseDir + "lib/bcel-6.0.jar,"
				+ baseDir + "lib/junit.jar";
		case SERENA: return "+classpath="
				+ baseDir + "out/production/code-ut,"
				+ baseDir + "code-ut/jars/monopoli100.jar,"
				+ baseDir + "code-ut/jars/commons-validator.jar,"
				+ baseDir + "code-ut/jars/mutated-cli.jar,"
				+ baseDir + "lib/bcel-6.0.jar,"
				+ baseDir + "out/test/code-ut,"
				+ baseDir + "out/production/varex-hom-finder,"
				+ baseDir + "lib/junit.jar";
		default: throw new IllegalStateException("Machine " + machine.toString() + " is not supported");
		}
	}

	private String getMutantFile() {
		switch (machine) {
		case FEATURE_SERVER: return baseDir + BenchmarkPrograms.getFeatureModelResource();
		default: return baseDir + "varex-hom-finder/resources/" + BenchmarkPrograms.getFeatureModelResource();
		}
	}

	private String getJpfPath() {
		return baseDir + "lib/RunJPF.jar";
	}

	private String getJvmClasspath() {
		switch (machine) {
		case FEATURE_SERVER: return "\""+baseDir+"hom-generator.jar;"+baseDir+"lib/*\"";
		default: return null;
		}
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
		if (selections.size() >= 2 && !BenchmarkPrograms.homIsValid(selections)) {
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
				if (selections.size() >= 2 && !BenchmarkPrograms.homIsValid(selections)) {
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
			String fname = RunTests.getTestDesc(test.getKey()) + ".txt";
			final File file = new File(fname);
			if (file.exists()) {
				try (BufferedReader br = new BufferedReader(new FileReader(fname))) {
					BDD bdd2 = bddFactory.load(br);
					FeatureExpr expr = new BDDFeatureExpr(bdd2);
					test.setValue(expr);
					file.deleteOnExit();
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
		return mutantExprs;
	}

}
