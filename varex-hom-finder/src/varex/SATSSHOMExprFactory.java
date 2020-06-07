package varex;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import benchmark.BenchmarkPrograms;
import benchmark.Benchmarker;
import benchmark.VarexOptions;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import me.tongfei.progressbar.ProgressBar;
import scala.collection.mutable.HashSet;
import scala.collection.mutable.Set;
import solver.sat.DimacsWriter;
import util.ProgressBarFactory;

/**
 * This class creates and modifies dimacs files to create a cnf expression for finding higher order mutants.
 * 
 * @author Jens Meinicke
 *
 */
public class SATSSHOMExprFactory {

	private SATSSHOMExprFactory() {
		// private constructor
	}
	/**
	 * Creates a conjunction of the given dimacs files.
	 */
	public static File andDimacsFiles(Collection<File> files, SingleFeatureExpr[] features, String fileName) {
		Map<String, Integer> variableNames = new LinkedHashMap<>();
		List<String> clauses = new ArrayList<>();
		int variableId = 1;

		// create features
		for (SingleFeatureExpr feature : features) {
			variableNames.put(feature.feature(), variableId);
			variableId++;
		}

		// collect nodes & collect adjusted clauses
		for (File file : files) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				Map<Integer, Integer> currentVariableMapping = new HashMap<>();
				currentVariableMapping.put(0, 0);

				String line;
				while ((line = reader.readLine()) != null && line.startsWith("c")) {
					String[] split = line.split(" ");
					if (line.contains(" nodevar")) {
						currentVariableMapping.put(Integer.parseInt(split[1]), variableId);
						variableNames.put("nodevar" + variableId, variableId);
						variableId++;
					} else if (line.contains("Alpha")) {
						currentVariableMapping.put(Integer.parseInt(split[1]), variableId);
						variableNames.put("Alpha" + variableId, variableId);
						variableId++;
					} else {
						// map featureID to existing featureID
						String featureName = split[2];
						Integer id = variableNames.get(featureName);
						if (id == null) {
							System.err.println(featureName + " found");
						}
						currentVariableMapping.put(Integer.parseInt(split[1]), id);
					}
				}

				if (!line.startsWith("p cnf ")) {
					throw new RuntimeException("reading went wrong, expected: p cnf");
				}

				// add clauses
				while ((line = reader.readLine()) != null) {
					StringBuilder sb = new StringBuilder();
					// add adjusted clauses
					for (String var : line.split(" ")) {
						int parseInt = Integer.parseInt(var);
						if (parseInt < 0) {
							sb.append('-');
						}
						sb.append(currentVariableMapping.get(Math.abs(parseInt)));
						sb.append(' ');
					}
					clauses.add(sb.substring(0, sb.length() - 1));
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		files.forEach(File::deleteOnExit);
		return writeDimacsFile(fileName, variableNames, clauses);
	}
	
	/**
	 * Creates a disjunction of the given dimacs files.
	 */
	public static File orDimacsFiles(Collection<File> dimacsFiles, SingleFeatureExpr[] mutants,
			String fileName) {
		// collect node ids and adjust indexes
		// get adjusted clauses map
		// get all nodes
		// add mutants

		Map<String, Integer> variableNames = new LinkedHashMap<>();
		List<String> clauses = new ArrayList<>();
		int variableId = 1;
		// create features

		for (SingleFeatureExpr feature : mutants) {
			variableNames.put(feature.feature(), variableId);
			variableId++;
		}

		List<Integer> alphas = new ArrayList<>();
		for (File file : dimacsFiles) {
			final int alpha = variableId;
			alphas.add(alpha);
			variableNames.put("Alpha" + alpha, alpha);
			variableId++;

			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				Map<Integer, Integer> currentVariableMapping = new HashMap<>();
				currentVariableMapping.put(0, 0);

				String line;
				while ((line = reader.readLine()) != null && line.startsWith("c")) {
					String[] split = line.split(" ");
					if (line.contains(" nodevar")) {
						currentVariableMapping.put(Integer.parseInt(split[1]), variableId);
						variableNames.put("nodevar" + variableId, variableId);
						variableId++;
					} else if (line.contains("Alpha")) {
						currentVariableMapping.put(Integer.parseInt(split[1]), variableId);
						variableNames.put("Alpha" + variableId, variableId);
						variableId++;
					} else {
						// map featureID to existing featureID
						String featureName = split[2];
						Integer id = variableNames.get(featureName);
						if (id == null) {
							System.err.println(featureName + " found");
						}
						currentVariableMapping.put(Integer.parseInt(split[1]), id);
					}
				}

				if (!line.startsWith("p cnf ")) {
					throw new RuntimeException("reading went wrong, expected: p cnf");
				}

				// add clauses
				while ((line = reader.readLine()) != null) {
					StringBuilder sb = new StringBuilder();
					// add -alpha
					sb.append(-alpha);
					sb.append(' ');
					// add adjusted clauses
					for (String var : line.split(" ")) {
						int parseInt = Integer.parseInt(var);
						if (parseInt < 0) {
							sb.append('-');
						}
						sb.append(currentVariableMapping.getOrDefault(Math.abs(parseInt), Math.abs(parseInt)));
						sb.append(' ');
					}
					clauses.add(sb.substring(0, sb.length() - 1));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!alphas.isEmpty()) {
			StringBuilder alphaClause = new StringBuilder();
			for (Integer alpha : alphas) {
				alphaClause.append(alpha);
				alphaClause.append(' ');
			}
			alphaClause.append('0');
			clauses.add(alphaClause.toString());
		}

		dimacsFiles.forEach(File::deleteOnExit);
		return writeDimacsFile(fileName, variableNames, clauses);
	}

	/**
	 * Writes the given variables and clauses to a dimacs file.
	 */
	private static File writeDimacsFile(String fileName, Map<String, Integer> variables, List<String> clauses) {
		// write new dimacs file
		File file = new File(fileName + ".dimacs");
		try (PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file)))) {
			// write variables
			for (Entry<String, Integer> variable : variables.entrySet()) {
				out.print("c ");
				out.print(variable.getValue());
				out.print(' ');
				out.print(variable.getKey());
				out.println();
			}

			// write p cnf #vars #clauses
			out.print("p cnf ");
			out.print(variables.size());
			out.print(' ');
			out.print(clauses.size());
			out.println();

			// write clauses
			clauses.forEach(out::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	private static boolean fomKillsTest(String m, FeatureExpr test) {
		Set<String> fom = new HashSet<>();
		fom.add(m);
		return test.evaluate(fom.toSet());
	}

	public static Collection<File> getStrictSSHOMExpr(Map<String, FeatureExpr> tests,
			SingleFeatureExpr[] mutants, int numMutants) {
		Collection<File> files = getSSHOMExpr(tests, mutants, numMutants);
		createExpression3(tests, mutants, numMutants, files);
		return files;
	}

	public static Collection<File> getSSHOMExpr(Map<String, FeatureExpr> tests, SingleFeatureExpr[] mutants,
			int numMutants) {
		List<File> files = new ArrayList<>();
		createExpression1(tests, files, mutants);
		createExpression2(tests, mutants, numMutants, files);
		createFeatureModel(mutants, files);
		return files;
	}

	/**
	 * At least one test must fail.
	 * 
	 * Creates a disjunction of all test expressions.
	 */
	private static void createExpression1(Map<String, FeatureExpr> tests, List<File> files, SingleFeatureExpr[] mutants) {
		final String expression1 = "expression1";
		File file = new File(expression1+ ".dimacs"); 
		if (file.exists()) {
			Benchmarker.instance.timestamp("Reuse existing expression from: " + file.getName());
			files.add(file);
			return;
		}
		try (ProgressBar pb = ProgressBarFactory.create("create expression 1", tests.size())) {
			if (VarexOptions.splitExpr1()) {
				List<File> expression1Files = new ArrayList<>(tests.size());
				
				List<String> orderedTests = new ArrayList<>();
				orderedTests.addAll(tests.keySet());
				Collections.sort(orderedTests, (o1, o2) -> Integer.compare(tests.get(o1).size(), tests.get(o2).size()));
								
				int fileNumber = 1;
				FeatureExpr orExpr = FeatureExprFactory.False();
				for (String test : orderedTests) {
					FeatureExpr testBDD = tests.get(test);
					pb.step();
					long start = System.currentTimeMillis();
					orExpr = orExpr.or(testBDD);
					long end = System.currentTimeMillis();
					long duration = end - start;
					if (duration >= VarexOptions.getSatSplitDuration() && !orExpr.isContradiction()) {
						File fileOr = DimacsWriter.instance.bddToDimacsTseytinTransformation((BDDFeatureExpr) orExpr, "expr_1_" + fileNumber + ".dimacs");
						expression1Files.add(fileOr);
						fileNumber++;
						orExpr = FeatureExprFactory.False();
					}
				}
				
				if (!orExpr.isContradiction()) {
					// create the final or file
					System.out.println("create final file: " + "expr_1_" + fileNumber + ".dimacs");
					File fileOr = DimacsWriter.instance.bddToDimacsTseytinTransformation((BDDFeatureExpr) orExpr, "expr_1_" + fileNumber + ".dimacs");
					expression1Files.add(fileOr);
				}
				
				files.add(orDimacsFiles(expression1Files, mutants, expression1));
			} else {
				
				FeatureExpr gt0 = FeatureExprFactory.False();
				for (Map.Entry<String, FeatureExpr> testEntry : tests.entrySet()) {
					pb.step();
					gt0 = gt0.or(testEntry.getValue());
				}
				DimacsWriter.instance.bddToDimacsTseytinTransformation((BDDFeatureExpr) gt0, expression1);
				files.add(file);
			}
		}
	}
	
	/**
	 * For each test, if it fails for the hom, then it must fail for each fom
	 * 
	 * and over all tests (f(t) -> and over all mutants (!m or (test fails for m)))
	 * 
	 */
	private static void createExpression2(Map<String, FeatureExpr> tests, SingleFeatureExpr[] mutants, int numMutants,
			List<File> files) {
		final String expression2 = "expression2";
		File file = new File(expression2 + ".dimacs"); 
		if (file.exists()) {
			Benchmarker.instance.timestamp("Reuse existing expression from: " + file.getName());
			files.add(file);
			return;
		}
		try (ProgressBar pb = ProgressBarFactory.create("create expression 2", tests.size())) {
			if (VarexOptions.splitExpr2()) {
				List<File> expression2Files = new ArrayList<>(tests.size());
				
				List<String> orderedTests = new ArrayList<>();
				orderedTests.addAll(tests.keySet());
				Collections.sort(orderedTests, (o1, o2) -> Integer.compare(tests.get(o1).size(), tests.get(o2).size()));
								
				int fileNumber = 1;
				FeatureExpr andExpr = FeatureExprFactory.True();
				for (String test : orderedTests) {
					pb.step();
					FeatureExpr expr = tests.get(test);
					if (!expr.isContradiction()) {
						FeatureExpr subexpression2 = createSubexpression2(mutants, numMutants, expr);
						long start = System.currentTimeMillis();
						andExpr = andExpr.and(subexpression2);
						long end = System.currentTimeMillis();
						long duration = end - start;
						if (duration >= VarexOptions.getSatSplitDuration()) {
							File andFile = DimacsWriter.instance.bddToDimacsTseytinTransformation((BDDFeatureExpr) andExpr, "expr_2_" + fileNumber + ".dimacs");
							expression2Files.add(andFile);
							andExpr = FeatureExprFactory.True();
							fileNumber++;
						}
					}
				}
				
				if (!andExpr.isTautology()) {
					// create the final and file
					File andFile = DimacsWriter.instance.bddToDimacsTseytinTransformation((BDDFeatureExpr) andExpr, "expr_2_" + fileNumber + ".dimacs");
					expression2Files.add(andFile);
				}
				
				
				files.add(andDimacsFiles(expression2Files, mutants, expression2));
			} else {
				FeatureExpr expression = FeatureExprFactory.True();
				for (Map.Entry<String, FeatureExpr> testEntry : tests.entrySet()) {
					pb.step();
					FeatureExpr testExpr = testEntry.getValue();
					if (!testExpr.isContradiction()) {
						FeatureExpr subexpression2 = createSubexpression2(mutants, numMutants, testExpr);
						expression = expression.and(subexpression2);
					}
				}
				DimacsWriter.instance.bddToDimacsTseytinTransformation((BDDFeatureExpr) expression, "expression2");
				files.add(file);
			}
		}
	}
	
	
	private static FeatureExpr createSubexpression2(SingleFeatureExpr[] mutants, int numMutants, FeatureExpr testExpr) {
		FeatureExpr subexpression2 = FeatureExprFactory.True();
		for (int i = 0; i < numMutants; i++) {
			boolean fomKillsTest = fomKillsTest(mutants[i].feature(), testExpr);
			if (!fomKillsTest) {
				subexpression2 = subexpression2.and(mutants[i].not());
			}
		}
		return testExpr.implies(subexpression2);
	}

	private static void createExpression3(Map<String, FeatureExpr> tests, SingleFeatureExpr[] mutants, int numMutants,
			Collection<File> files) {
		final String expression3 = "expression3";
		File file = new File(expression3 + ".dimacs"); 
		if (file.exists()) {
			Benchmarker.instance.timestamp("Reuse existing expression from: " + file.getName());
			files.add(file);
			return;
		}
		try (ProgressBar pb = ProgressBarFactory.create("create expression 3", tests.size())) {
			FeatureExpr strictExpr = FeatureExprFactory.False();
			List<File> dimacsFiles = new ArrayList<>();
			
			List<String> orderedTests = new ArrayList<>();
			orderedTests.addAll(tests.keySet());
			Collections.sort(orderedTests, (o1, o2) -> Integer.compare(tests.get(o1).size(), tests.get(o2).size()));
			
			int fileNumber = 0;
			for (String test : tests.keySet()) {
				pb.step();
				
				FeatureExpr testExpr = tests.get(test);
				FeatureExpr killsMutant = FeatureExprFactory.True();
	
				for (int i = 0; i < numMutants; i++) {
					boolean fomKillsTest = fomKillsTest(mutants[i].feature(), testExpr);
					if (!fomKillsTest) {
						killsMutant = killsMutant.and(mutants[i].not());
					}
				}
				
				FeatureExpr expr = killsMutant.andNot(testExpr);
				long start = System.currentTimeMillis();
				strictExpr = strictExpr.or(expr);
				long end = System.currentTimeMillis();
				long duration = end - start;
				if (VarexOptions.splitExpr3() && duration >= VarexOptions.getSatSplitDuration()) {
					File strictFile = DimacsWriter.instance.bddToDimacsTseytinTransformation((BDDFeatureExpr) strictExpr,  "strict_" + fileNumber);
					dimacsFiles.add(strictFile);
					fileNumber++;
					strictExpr = FeatureExprFactory.False();
				}
			}
	
			if (VarexOptions.splitExpr3()) {
				if (!strictExpr.isContradiction()) {
					File strictFile = DimacsWriter.instance.bddToDimacsTseytinTransformation((BDDFeatureExpr) strictExpr,  "strict_" + fileNumber);
					dimacsFiles.add(strictFile);
				}
				file = orDimacsFiles(dimacsFiles, mutants, expression3);
			} else {
				file = DimacsWriter.instance.bddToDimacsTseytinTransformation((BDDFeatureExpr) strictExpr, expression3);
			}
			files.add(file);
		}
	}

	public static void createFeatureModel(SingleFeatureExpr[] mutantExprs, List<File> files) {
		Map<String, Integer> makeshiftFeatureModel = BenchmarkPrograms.getMakeshiftFeatureModel();
		Map<Integer, List<String>> makeshiftFeatureModelGroups = new HashMap<>();
		makeshiftFeatureModel.forEach((k1, v1) -> makeshiftFeatureModelGroups.compute(v1, (k2, v2) -> {
			List<String> l = v2; 
			if (v2 == null) {
				l = new ArrayList<>();
			}
			l.add(k1);
			return l;
		}));
		
		Map<String, Integer> variables = new LinkedHashMap<>(); 
		int id = 1;
		for (SingleFeatureExpr singleFeatureExpr : mutantExprs) {
			variables.put(singleFeatureExpr.feature(), id++);
		}
		
		List<String> clauses = new ArrayList<>();
		for (List<String> features : makeshiftFeatureModelGroups.values()) {
			if (features.size() > 1) { 
				clauses.addAll(createEntries(features, variables));
			}
		}
		
		files.add(writeDimacsFile("featureModel", variables, clauses));
	}

	private static List<String> createEntries(List<String> features, Map<String, Integer> variables) {
		List<String> clauses = new ArrayList<>();
		for (int i = 0; i < features.size() - 1; i++) {
			int var1 = variables.get("CONFIG_" + features.get(i));
			for (int j = i + 1; j < features.size(); j++) {
				int var2 = variables.get("CONFIG_" + features.get(j));
				StringBuilder clause = new StringBuilder();
				clause.append(-var1);
				clause.append(' ');
				clause.append(-var2);
				clause.append(' ');
				clause.append(0);
				clauses.add(clause.toString());
			}
		}
		return clauses;
	}

}
