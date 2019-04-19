package benchmark;

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
import benchmark.heuristics.MutationGraph;
import benchmark.heuristics.SSHOMJUnitChecker;
import benchmark.heuristics.TestRunListener;
import br.ufmg.labsoft.mutvariants.schematalib.SchemataLibMethods;
import util.SSHOMListener;
import util.SSHOMRunner;

public class HeuristicsBasedSSHOMFinder {
	
	private SSHOMRunner runner;
	private Map<String, Set<Description>> foms;
	private Map<String, Set<String>> testMap = new LinkedHashMap<>();
	private Class<?>[] testClasses;
	
	private int[] distribution = new int[10];

	private MutationGraph graph;
	
	ISSHOMChecker checker = null;
	
	public void run() throws NoSuchFieldException, IllegalAccessException {
		System.setProperty("line.separator", "\n");
		Benchmarker.instance.start();
		Class<?>[] targetClasses = BenchmarkPrograms.getTargetClasses();
		this.testClasses = BenchmarkPrograms.getTestClasses();
		foms = new HashMap<>();
		runner = new SSHOMRunner(targetClasses, testClasses);
		checker = new SSHOMJUnitChecker(runner, testClasses, foms);
		String[] mutants = BenchmarkPrograms.getMutantNames();
		
		Benchmarker.instance.timestamp("start homs");
		createTestMap();
		populateFoms(mutants);
		
		Benchmarker.instance.timestamp("create hom candidates");
		graph = new MutationGraph(foms);
		Map<Integer, Set<HigherOrderMutant>> homCandidates = createHOMMap(graph.getHOMPaths());
		while (!homCandidates.isEmpty()) {
			int minScore = Integer.MAX_VALUE;
			for (Entry<Integer, Set<HigherOrderMutant>> entry : homCandidates.entrySet()) {
				if (!entry.getValue().isEmpty()) {
					minScore = Math.min(minScore, entry.getKey());
				}
			}
			
			Collection<HigherOrderMutant> homPaths = homCandidates.get(minScore);
			
			int foundHoms = sshoms.size();
			for (Iterator<HigherOrderMutant> iterator = homPaths.iterator(); iterator.hasNext();) {
				HigherOrderMutant collection = iterator.next();
				iterator.remove();
				run(homCandidates, collection);
				if (sshoms.size() > foundHoms) {
					break;
				}
			}
			if (homPaths.isEmpty()) {
				homCandidates.remove(minScore);
			}
		}
		System.out.println(Arrays.toString(distribution));
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

	private void populateFoms(String[] mutants) throws NoSuchFieldException, IllegalAccessException {
		for (String m : mutants) {
			SSHOMListener listener;
			if (BenchmarkPrograms.programHasInfLoops()) {
				listener = InfLoopTestProcess.getFailedTests(testClasses, testMap, new String[] { m });
			} else {
				listener = runner.runJunitOnHOM(m);
			}
			foms.put(m, listener.getHomTests());
		}
	}

	private void createTestMap() {
		TestRunListener testRunListener = new TestRunListener(testMap);
		SchemataLibMethods.listener = (String methodName) -> {
			testRunListener.methodExecuted(methodName);
		};

		InfLoopTestProcess.listener.testRunListener = testRunListener;

		SSHOMListener listener = InfLoopTestProcess.getFailedTests(testClasses, new String[] {});
		Set<Description> failingTests = listener.getHomTests();
		if (!failingTests.isEmpty()) {
			failingTests.forEach(System.out::println);
			 throw new RuntimeException("test suite failed without mutants");
		}

		InfLoopTestProcess.listener.testRunListener = null;
		SchemataLibMethods.listener = (String methodName) -> {
			if (InfLoopTestProcess.timedOut) {
				throw new RuntimeException();
			}
		};
	}

	int foundHoms = 0;
	int homsChecked = 0;
	
	// TODO Set<FirstOrderMutant>
	Set<String> coveredFoms = new HashSet<>();
	Collection<HigherOrderMutant> sshoms = new HashSet<>();
	
	private void run(Map<Integer, Set<HigherOrderMutant>> homCandidates, HigherOrderMutant homCandidate) {
		homsChecked++;
		Collection<String> selectedMutants = new ArrayList<>();
		for (FirstOrderMutant mutant : homCandidate) {
			selectedMutants.add(mutant.getMutant());
		}
		boolean isStronglySubsuming = checker.isSSHOM(testMap, homCandidate);
		if (isStronglySubsuming) {
			sshoms.add(homCandidate);
			updateHOMCandidates(homCandidate, homCandidates);
			
			foundHoms++;
			HashSet<String> sshom = new HashSet<>();
			sshom.addAll(selectedMutants);
			distribution[selectedMutants.size()]++;
			coveredFoms.addAll(selectedMutants);
			float efficiency = ((float) foundHoms * 100)/ (float)homsChecked;
			Benchmarker.instance.timestamp(foundHoms + "(" + homsChecked + ") "+ efficiency + "% FOMs: " + coveredFoms.size()+ " "  + selectedMutants);
//			Benchmarker.instance.timestamp("size: " + homCandidate.size() + " div: " + computeDiv(homCandidate.getFoms()) + " isN+1: " + (isNPlusOne(homCandidate) == 1));
			
//			+ " is_strict_subsuming: "
//					+ CheckStronglySubsuming.isStrictStronglySubsuming(listener.getHomTests(), currentFoms));
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
				if (!sshoms.contains(hom)) {// TODO should be all HOMStested
					// TODO duplicate code
					candidatesMap.putIfAbsent(newScore, new HashSet<>());
					candidatesMap.get(newScore).add(hom);
					isNPlusOne.add(hom);
				}				
			}
		}
		
	}

}
