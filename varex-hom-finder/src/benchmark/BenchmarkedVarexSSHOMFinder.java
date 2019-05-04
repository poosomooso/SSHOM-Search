package benchmark;

import cmu.conditional.Conditional;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.FExprBuilder;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDException;
import net.sf.javabdd.BDDFactory;
import org.junit.Test;
import solver.bdd.BDDSolver;
import solver.sat.SATSolver;
import testRunner.RunTests;
import varex.SATSSHOMExprFactory;
import varex.SSHOMExprFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class BenchmarkedVarexSSHOMFinder {

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

	
	public void varexSSHOMFinder() throws IOException {
		Benchmarker.instance.start();

		Class<?>[] testClasses = BenchmarkPrograms.getTestClasses();
		String[] mutants = BenchmarkPrograms.getMutantNames();

		System.setProperty("bddCacheSize", Integer.toString(100000));
		System.setProperty("bddValNum", Integer.toString(6_000_000));
		FExprBuilder.bddFactory().setMaxNodeNum(429496728);

		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());

		Map<Class<?>, Map<Method, FeatureExpr>> tests = new LinkedHashMap<>();
		getTestNames(testClasses, tests);
		filterTests(tests);

		// TODO ignore varex for now
//		final String paths = getVarexClasspaths();
//		final String mutantFile = getMutantFile();
//		final String jpfPath = getJpfPath();
//		final String jvmClasspath = getJvmClasspath();
//		int numberOfTests = tests.size();
//		int i = 1;
//		for (Entry<Method, FeatureExpr> test : tests.entrySet()) {
//			System.out.println(i++ + "/" + numberOfTests + " tests");
//
//			if (jvmClasspath == null) {
//				CommandLineRunner.process("java", "-jar", jpfPath, "+search.class=.search.RandomSearch", "+bddCacheSize=100000",
//						"+bddValNum=1500000", paths, "+choice=MapChoice", "+mutants=" + mutantFile, VarexTestRunner.class
//								.getName(), test.getKey().getDeclaringClass().getName(), test.getKey().getName());
//			} else {
//				CommandLineRunner.process("java", "-classpath", jvmClasspath, "-jar", jpfPath,
//						"+search.class=.search.RandomSearch", "+bddCacheSize=100000", "+bddValNum=1500000", paths,
//						"+choice=MapChoice", "+mutants=" + mutantFile, VarexTestRunner.class.getName(), test.getKey()
//								.getDeclaringClass().getName(), test.getKey().getName());
//			}
//		}
		/* VarexC */
		switch (BenchmarkPrograms.PROGRAM) {
			case TRIANGLE:
				runTriangleVarexC();
				break;
			case MONOPOLY:
				runMonopolyVarexC();
				break;
			case CLI:
				runCLIVarexC();
				break;
			case VALIDATOR:
				runValidatorVarexC();
				break;
		}

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
		
		final boolean strict = false;
		// BDD solutions
		Set<Set<String>> solutionsBDD = getBDDSolutions(mutants, stringTests, strict);
		
		// SAT solutions
//		Set<Set<String>> solutionsSAT = getSATSolutions(mutants, stringTests, strict); 
		
//		checkSolutions(solutionsBDD, solutionsSAT);
	}

	private void runMonopolyVarexC() {
		CommandLineRunner.process("java", "-Xmx12g", "-cp", "code-ut/jars/vbc-monopoly.jar",
				"edu.cmu.cs.vbc.testutils.MonopoliForkTestLauncher",
				"code-ut/jars/vbc-monopoly.jar",
				"code-ut/jars/monopoli100.jar"
		);
	}

	private void runCLIVarexC() {
		CommandLineRunner.process("java", "-Xmx12g", "-cp", "code-ut/jars/vbc-cli.jar",
				"edu.cmu.cs.vbc.testutils.CLIForkTestLauncher",
				"code-ut/jars/vbc-cli.jar",
				"code-ut/jars/mutated-cli.jar"
		);
	}

	private void runValidatorVarexC() {
		CommandLineRunner.process("java", "-Xmx12g", "-cp", "code-ut/jars/vbc-validator.jar",
				"edu.cmu.cs.vbc.testutils.ValidatorTestLauncher",
				"code-ut/jars/commons-validator.jar"
		);
	}

	private void runTriangleVarexC() {
		CommandLineRunner.process("java", "-Xmx12g", "-cp", "code-ut/jars/vbc-triangle.jar",
				"edu.cmu.cs.vbc.testutils.TriangleTestLauncher"
		);
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
		Benchmarker.instance.timestamp("generate FOMs");
//		FeatureExpr[] fomExprs = SSHOMExprFactory.genFOMs(mutantExprs, mutants.length);
		
		Benchmarker.instance.timestamp("create SSHOM expression");
		
		FeatureExpr finalExpr;
		if (strict) {
			finalExpr = SSHOMExprFactory.getStrictSSHOMExpr(stringTests, mutantExprs, mutantExprs.length);
		} else {
			finalExpr = SSHOMExprFactory.getSSHOMExpr(stringTests, mutantExprs, mutantExprs.length);
		}
		// exclude foms
//		for (FeatureExpr m : fomExprs) {
//			finalExpr = finalExpr.andNot(m);
//		}
		return new BDDSolver(2).getSolutions((BDDFeatureExpr)finalExpr, mutants, BenchmarkPrograms::homIsValid);
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
		case FEATURE_SERVER: return "\""+baseDir+"hom-generator.jar;"+baseDir+"lib/scala/scala-library-2.12.6.jar;"+baseDir+"lib/\"";
		default: return null;
		}
	}

	private Set<Set<String>> getSATSolutions(String[] mutants, Map<String, FeatureExpr> stringTests, boolean strict) {
		Benchmarker.instance.timestamp("create SAT formula");
		
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
		
		Benchmarker.instance.timestamp("get SAT solutions");
		return new SATSolver(2).getSolutions("fullmodel.dimacs", mutants);
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
		File folder = new File(".");//"BDDS/" + BenchmarkPrograms.PROGRAM.name());
		
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
