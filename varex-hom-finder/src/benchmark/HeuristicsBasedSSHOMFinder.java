package benchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import util.SSHOMListener;
import util.SSHOMRunner;

public class HeuristicsBasedSSHOMFinder {
	
	private SSHOMRunner runner;
	private Map<String, Set<Description>> foms;
	private Class<?>[] testClasses;
	
	private int[] distribution = new int[10];

	private MutationGraph graph;
	
	ISSHOMChecker checker = null;
	
	public void run() throws NoSuchFieldException, IllegalAccessException {
		Benchmarker.instance.start();
		Class<?>[] targetClasses = BenchmarkPrograms.getTargetClasses();
		this.testClasses = BenchmarkPrograms.getTestClasses();
		foms = new HashMap<>();
		runner = new SSHOMRunner(targetClasses, testClasses);
		checker = new SSHOMJUnitChecker(runner, targetClasses, foms);
		String[] mutants = BenchmarkPrograms.getMutantNames();
		
		Benchmarker.instance.timestamp("start homs");
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
			int[] candidateDistribution = new int[10];
			for (HigherOrderMutant j : homPaths) {
				candidateDistribution[j.size()]++;
			}
			
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
	
	// TODO remove this
	private int isNPlusOne(HigherOrderMutant c1) {
		if (c1.size() == 2) {
			return 1;
		}
		if (isNPlusOne.contains(c1)) {
			return 1;
		}
		return 0;
	}

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
		{
			SSHOMListener listener = InfLoopTestProcess.getFailedTests(testClasses, new String[] {});
			Set<Description> failingTests = listener.getHomTests();
			if (!failingTests.isEmpty()) {
				failingTests.forEach(System.out::println);
				throw new RuntimeException();
			}
		}
		for (String m : mutants) {
			SSHOMListener listener;
			if (BenchmarkPrograms.programHasInfLoops()) {
				listener = InfLoopTestProcess.getFailedTests(testClasses, new String[] { m });
			} else {
				listener = runner.runJunitOnHOM(m);
			}
			foms.put(m, listener.getHomTests());
		}
	}

	int foundHoms = 0;
	int homsChecked = 0;
	
	Set<String> coveredFoms = new HashSet<>();
	Collection<HigherOrderMutant> sshoms = new HashSet<>();
	
	private void run(Map<Integer, Set<HigherOrderMutant>> homCandidates, HigherOrderMutant homCandidate) {
		homsChecked++;
		Collection<String> selectedMutants = new ArrayList<>();
		for (FirstOrderMutant mutant : homCandidate) {
			selectedMutants.add(mutant.getMutant());
		}
		boolean isStronglySubsuming = checker.isSSHOM(homCandidate);
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
			Benchmarker.instance.timestamp("size: " + homCandidate.size() + " div: " + computeDiv(homCandidate.getFoms()) + " isN+1: " + (isNPlusOne(homCandidate) == 1));
			
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
				if (!sshoms.contains(hom)) {
					// TODO duplicate code
					candidatesMap.putIfAbsent(newScore, new HashSet<>());
					candidatesMap.get(newScore).add(hom);
					isNPlusOne.add(hom);
				}				
			}
		}
		
	}

}
