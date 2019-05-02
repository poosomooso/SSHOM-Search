package benchmark;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.runner.Description;

import benchmark.heuristics.Configuration;
import benchmark.heuristics.FirstOrderMutant;
import benchmark.heuristics.HigherOrderMutant;
import benchmark.heuristics.ISSHOMChecker;
import benchmark.heuristics.ISSHOMChecker.HOM_TYPE;
import benchmark.heuristics.MutationGraph;
import benchmark.heuristics.SSHOMJUnitChecker;
import benchmark.heuristics.TestRunListener;
import br.ufmg.labsoft.mutvariants.schematalib.ISchemataLibMethodsListener;
import br.ufmg.labsoft.mutvariants.schematalib.SchemataLibMethods;
import evaluation.analysis.Mutation;
import evaluation.io.MutationParser;
import util.SSHOMListener;
import util.SSHOMRunner;
import util.io.ObjectReader;
import util.io.ObjectWriter;

public class HeuristicsBasedSSHOMFinder {
	
	private final long TIME_FOMS = Flags.getMaxGroupTime() / 2;
	private final long TIME_HOMS = Flags.getMaxGroupTime() / 2;
	
	private SSHOMRunner runner;
	private final Map<String, Set<Description>> foms = new HashMap<>();
	private final Map<String, Set<String>> testMap = new HashMap<>();
	private Class<?>[] testClasses;
	
	private int[] distribution = new int[10];

	private MutationGraph graph;
	
	private final ISSHOMChecker checker;
	
	public HeuristicsBasedSSHOMFinder() {
		System.setProperty("line.separator", "\n");
		Benchmarker.instance.start();
		Class<?>[] targetClasses = BenchmarkPrograms.getTargetClasses();
		this.testClasses = BenchmarkPrograms.getTestClasses();
		runner = new SSHOMRunner(targetClasses, testClasses);
		checker = new SSHOMJUnitChecker(runner, testClasses, foms);
		Benchmarker.instance.timestamp("createTestMap");
		createTestMap();
		InfLoopTestProcess.setTimeOutListener();
	}
	
	public void run() throws NoSuchFieldException, IllegalAccessException {
		String[] mutants = BenchmarkPrograms.getMutantNames();
		Map<String, Mutation> mutations = MutationParser.instance.getMutations(new File("bin/evaluationfiles/" + BenchmarkPrograms.PROGRAM.toString().toLowerCase(), "mapping.txt"));
		
		// group mutations
		Map<String, Set<String>> groupMutants = new LinkedHashMap<>();
		for (String m : mutants) {
			Mutation mutation = mutations.get(m);
			if (mutation == null) {
				throw new RuntimeException("Mutation not found: " + m);
			}
			final String groupIdenntifyer = getGroupIdentifyer(mutation);
			groupMutants.putIfAbsent(groupIdenntifyer, new HashSet<>());
			groupMutants.get(groupIdenntifyer).add(m);
		}
		Benchmarker.instance.timestamp("start homs");
		groupLoop: for (Entry<String, Set<String>> packageEntry : groupMutants.entrySet()) {
			System.out.println("group: " + packageEntry.getKey());
			System.out.println("nr mutatns: " + packageEntry.getValue().size());
			foms.clear();
			populateFoms(packageEntry.getValue());
			Benchmarker.instance.timestamp("create hom candidates");
			graph = new MutationGraph(foms, testMap);

			// Map<package, Graph>
			// merge hom candidates?
			long start = System.currentTimeMillis();
			Map<Integer, Set<HigherOrderMutant>> homCandidates = createHOMMap(graph.getHOMPaths());
			while (!homCandidates.isEmpty()) {
				int minScore = Integer.MAX_VALUE;
				for (Entry<Integer, Set<HigherOrderMutant>> entry : homCandidates.entrySet()) {
					if (!entry.getValue().isEmpty()) {
						minScore = Math.min(minScore, entry.getKey());
					}
				}
				
				Collection<HigherOrderMutant> homPaths = homCandidates.get(minScore);
				
				int foundHoms = homsTried.size();
				for (Iterator<HigherOrderMutant> iterator = homPaths.iterator(); iterator.hasNext();) {
					HigherOrderMutant collection = iterator.next();
					iterator.remove();
					run(homCandidates, collection);
					long time = System.currentTimeMillis();
					if (time - start > TIME_HOMS) {
						continue groupLoop;
					}
					if (homsTried.size() > foundHoms) {
						break;
					}
				}
				if (homPaths.isEmpty()) {
					homCandidates.remove(minScore);
				}
			}
			System.out.println(Arrays.toString(distribution));
		}
	}

	private String getGroupIdentifyer(Mutation mutation) {
		final  String groupIdenntifyer;
		
		switch (Flags.getGranularity()) {
		case ALL:
			groupIdenntifyer = "";
			break;
		case PACKAGE:
			groupIdenntifyer = mutation.className.substring(0, mutation.className.lastIndexOf('.'));
			break;
		case CLASS:
			groupIdenntifyer = mutation.className;
			break;
		case METHOD:
			groupIdenntifyer = mutation.className + "." + mutation.methodName;
			break;
		default:
			throw new RuntimeException("granularity not implemented: " + Flags.getGranularity());
		}
		return groupIdenntifyer;
	}

	// TODO this should be the direct result of the graph
	private Map<Integer, Set<HigherOrderMutant>> createHOMMap(Collection<HigherOrderMutant> homPaths) {
		Map<Integer, Set<HigherOrderMutant>> candidates = new HashMap<>();
		for (Iterator<HigherOrderMutant> iterator = homPaths.iterator(); iterator.hasNext();) {
			HigherOrderMutant c = iterator.next();
			int score = Configuration.K_TEST_OVERLAP * computeDiv(c.getFoms()) + Configuration.K_DEGREE * c.size() - Configuration.K_N_PLUS_ONE * (c.size() == 2 ? 1 : 0);
			candidates.putIfAbsent(score, new HashSet<>());
			candidates.get(score).add(c);
			iterator.remove();
		}
		
		return candidates;
	}

	// TODO remove this
	private Set<HigherOrderMutant> isNPlusOne = new HashSet<>();
	
	private int computeDiv(Collection<FirstOrderMutant> c1) {
		Set<Description> tests = new HashSet<>();
		for (FirstOrderMutant node : c1) {
			tests.addAll(node.getTests());
		}
		int testsSize = tests.size();
		for (FirstOrderMutant node : c1) {
			tests.retainAll(node.getTests());
		}
		int testsSizeIntersection = tests.size();
		return testsSize - testsSizeIntersection;
	}

	private void populateFoms(Collection<String> mutants) throws NoSuchFieldException, IllegalAccessException {
		Benchmarker.instance.timestamp("populateFoms");
		long start = System.currentTimeMillis();
		for (String m : mutants) {
			SSHOMListener listener;
			if (BenchmarkPrograms.programHasInfLoops()) {
				listener = InfLoopTestProcess.getFailedTests(testClasses, testMap, new String[] { m });
			} else {
				listener = runner.runJunitOnHOM(m);
			}
			foms.put(m, listener.getHomTests());
			long time = System.currentTimeMillis();
			if (time - start > TIME_FOMS) {
				break;
			}
		}
	}

	private void createTestMap() {
		if (Flags.saveTestResults()) {
			boolean resultsRead = readTestResults();
			if (resultsRead) {
				boolean timesRead = InfLoopTestProcess.readTestTimes();
				if (timesRead) {
					return;
				}
			}
		}		
		
		TestRunListener testRunListener = new TestRunListener(testMap);
		SchemataLibMethods.listener = new ISchemataLibMethodsListener() {
			
			@Override
			public void listen(String methodName) {
				testRunListener.methodExecuted(methodName);
			}
			
			@Override
			public void listen() {
				if (Flags.isCoverage()) {
					testRunListener.methodExecuted(Thread.currentThread().getStackTrace()[3]);
				}
			}
		};
		
		InfLoopTestProcess.listener.testRunListener = testRunListener;

		SSHOMListener listener = InfLoopTestProcess.getFailedTests(testClasses);
		Set<Description> failingTests = listener.getHomTests();
		if (!failingTests.isEmpty()) {
			failingTests.forEach(System.out::println);
			 throw new RuntimeException("test suite failed without mutants");
		}

		InfLoopTestProcess.listener.testRunListener = null;
		
		if (Flags.saveTestResults()) {
			writeTestResults();
		}
	}
	
	private void writeTestResults() {
		try {
			File testsMapFile = new File("testMap_" + BenchmarkPrograms.PROGRAM + ".serial");
			ObjectWriter.writeObject((Serializable)testMap, testsMapFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean readTestResults() {
		File testsMapFile = new File("testMap_" + BenchmarkPrograms.PROGRAM + ".serial");
		if (testsMapFile.exists()) {
			try {
				Object readTestCoverage = ObjectReader.readObject(testsMapFile);
				if (readTestCoverage instanceof Map) {
					testMap.putAll((Map)readTestCoverage);
					return true;
				} else {
					throw new IOException("type does not match: " + readTestCoverage.getClass());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private int foundHoms = 0;
	private int homsChecked = 0;
	
	private final Set<FirstOrderMutant> coveredFoms = new HashSet<>();
	private final Collection<HigherOrderMutant> homsTried = new HashSet<>();
	
	private int numberOfStrictFound = 0;
	
	private void run(Map<Integer, Set<HigherOrderMutant>> homCandidates, HigherOrderMutant homCandidate) {
		homsTried.add(homCandidate);
		homsChecked++;
		Collection<String> selectedMutants = new ArrayList<>();
		for (FirstOrderMutant mutant : homCandidate) {
			selectedMutants.add(mutant.getMutant());
		}
		HOM_TYPE homType = checker.getHOMType(testMap, homCandidate);
		if (homType != HOM_TYPE.NONE) {
			updateHOMCandidates(homCandidate, homCandidates);
			
			foundHoms++;
			HashSet<String> sshom = new HashSet<>();
			sshom.addAll(selectedMutants);
			distribution[selectedMutants.size()]++;
			coveredFoms.addAll(homCandidate.getFoms());
			float efficiency = ((float) foundHoms * 100)/ (float)homsChecked;
			int computeDiv = computeDiv(homCandidate.getFoms());
			String descriptor = foundHoms + "(" + homsChecked + ") "+ efficiency + "% FOMs: " + coveredFoms.size()+ " "  + selectedMutants;
			if (homType == HOM_TYPE.STRICT_STRONGLY_SUBSUMING) {
				numberOfStrictFound++;
				descriptor += " is_strict: " + numberOfStrictFound;
			}
			if (computeDiv > 0) {
				descriptor += " div: " + computeDiv;	
			}
			Benchmarker.instance.timestamp(descriptor);
		}
	}

	private void updateHOMCandidates(HigherOrderMutant homCandidate, Map<Integer, Set<HigherOrderMutant>> candidatesMap) {
		Collection<Set<FirstOrderMutant>> children = graph.getAllDirektChildren(homCandidate.getFoms());
		for (Set<FirstOrderMutant> child : children) {
			final int div = computeDiv(child);
			final int size = child.size();
			final int oldScore = Configuration.K_TEST_OVERLAP * div + Configuration.K_DEGREE * size;
			final int newScore = Configuration.K_TEST_OVERLAP * div + Configuration.K_DEGREE * size - Configuration.K_N_PLUS_ONE;
			
			Set<HigherOrderMutant> bucket = candidatesMap.get(oldScore);
			HigherOrderMutant hom = new HigherOrderMutant(child);
			if (bucket != null && bucket.contains(hom)) {
					bucket.remove(hom);
					if (bucket.isEmpty()) {
						candidatesMap.remove(oldScore);
					}
					candidatesMap.putIfAbsent(newScore, new HashSet<>());
					candidatesMap.get(newScore).add(hom);
					isNPlusOne.add(hom);
			} else {
				if (!homsTried.contains(hom)) {
					// TODO duplicate code
					candidatesMap.putIfAbsent(newScore, new HashSet<>());
					candidatesMap.get(newScore).add(hom);
					isNPlusOne.add(hom);
				}				
			}
		}
		
	}

}
