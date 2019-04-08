package benchmark;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.runner.Description;

import benchmark.heuristics.FirstOrderMutant;
import benchmark.heuristics.MutationGraph;
import benchmark.heuristics.SSHOMChecker;
import util.CheckStronglySubsuming;
import util.SSHOMListener;
import util.SSHOMRunner;

public class HeuristicsBasedSSHOMFinder {
	private static final int K_DIV = 1;
	private static final int K_SIZE = 10;
	private static final int K_N_PLUS_ONE = 100;
	
	private SSHOMRunner runner;
	private Map<String, Set<Description>> foms;
	private Class<?>[] testClasses;
	
	private int[] distribution = new int[10];

	MutationGraph graph;
	
	public void run() throws NoSuchFieldException, IllegalAccessException {
		System.setErr(new PrintStream(System.err) {
			@Override
			public void write(int b) {
				
			}
			@Override
			public void println(String x) {
//				// TODO Auto-generated method stub
//				super.println(x);
			}
		});
		
		Benchmarker.instance.start();

		Class<?>[] targetClasses = BenchmarkPrograms.getTargetClasses();
		this.testClasses = BenchmarkPrograms.getTestClasses();

		foms = new HashMap<>();
		
		runner = new SSHOMRunner(targetClasses, testClasses);
		String[] mutants = BenchmarkPrograms.getMutantNames();
		
		Benchmarker.instance.timestamp("start homs");
		populateFoms(mutants);
		
		Benchmarker.instance.timestamp("create hom candidates");
		graph = new MutationGraph(foms);
		Map<Integer, Set<Set<FirstOrderMutant>>> homCandidates = createHOMMap(graph.getHOMPaths());
		
		while (!homCandidates.isEmpty()) {
			int minScore = Integer.MAX_VALUE;
			for (Entry<Integer, Set<Set<FirstOrderMutant>>> entry : homCandidates.entrySet()) {
				if (!entry.getValue().isEmpty()) {
					minScore = Math.min(minScore, entry.getKey());
				}
			}
			
			Collection<Set<FirstOrderMutant>> homPaths = homCandidates.get(minScore);
			int[] candidateDistribution = new int[10];
			for (Collection<FirstOrderMutant> j : homPaths) {
				candidateDistribution[j.size()]++;
			}
			
			int foundHoms = sshoms.size();
			for (Iterator<Set<FirstOrderMutant>> iterator = homPaths.iterator(); iterator.hasNext();) {
				Set<FirstOrderMutant> collection = iterator.next();
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

	private Map<Integer, Set<Set<FirstOrderMutant>>> createHOMMap(Collection<Set<FirstOrderMutant>> homPaths) {
		Map<Integer, Set<Set<FirstOrderMutant>>> candidates = new HashMap<>();
		for (Iterator<Set<FirstOrderMutant>> iterator = homPaths.iterator(); iterator.hasNext();) {
			Set<FirstOrderMutant> c = iterator.next();
			int score = K_DIV * computeDiv(c) + K_SIZE * c.size() - K_N_PLUS_ONE * isNPlusOne(c);
			candidates.putIfAbsent(score, new HashSet<>());
			candidates.get(score).add(c);
			iterator.remove();
		}
		
		return candidates;
	}

	// TODO remove this
	private Set<Collection<FirstOrderMutant>> isNPlusOne = new HashSet<>();
	
	// TODO remove this
	private int isNPlusOne(Collection<FirstOrderMutant> c1) {
		if (c1.size() == 2) {
			return 1;
		}
		if (isNPlusOne.contains(c1)) {
			return 1;
		}
		Set<FirstOrderMutant> c = new HashSet<>(c1);
		
		for (FirstOrderMutant node : c1) {
			c.remove(node);
			if (sshoms.contains(c)) {
				isNPlusOne.add(c1);
				return 1;
			}
			c.add(node);
		}
		return 0;
	}

	private Map<Collection<FirstOrderMutant>, Integer> scores = new HashMap<>();
	
	private int computeDiv(Collection<FirstOrderMutant> c1) {
		if (scores.containsKey(c1)) {
			return scores.get(c1);
		}
		Set<Description> tests = new HashSet<>();
		for (FirstOrderMutant node : c1) {
			tests.addAll(node.getTests());
		}
		int testsSize = tests.size();
		for (FirstOrderMutant node : c1) {
			tests.retainAll(node.getTests());
		}
		int testsSizeIntersection = tests.size();
		
		int score = testsSize - testsSizeIntersection;
		scores.put(c1, score);
		return score;
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
	Collection<Set<FirstOrderMutant>> sshoms = new HashSet<>();
	
	SSHOMChecker checker = new SSHOMChecker();
	
	private void run(Map<Integer, Set<Set<FirstOrderMutant>>> homCandidates, Set<FirstOrderMutant> homCandidate)
			throws NoSuchFieldException, IllegalAccessException {
		// TODO use interface
		homsChecked++;
		Collection<String> selectedMutants = new ArrayList<>();
		for (FirstOrderMutant mutant : homCandidate) {
			selectedMutants.add(mutant.getMutant());
		}
//		System.out.println(homsChecked +  " / " + nrHOMS);
//		SSHOMListener listener;
//		if (BenchmarkPrograms.programHasInfLoops()) {
//			listener = InfLoopTestProcess.getFailedTests(testClasses, selectedMutants.toArray(new String[0]));
//		} else {
//			listener = runner.runJunitOnHOM(selectedMutants.toArray(new String[0]));
//		}
//		List<Set<Description>> currentFoms = selectedMutants.stream().map(m -> foms.get(m))
//				.collect(Collectors.toList());

		// TODO 
//		boolean stronglySubsumingBDDChecker = CheckStronglySubsuming.isStronglySubsuming(listener.getHomTests(), currentFoms);
		boolean stronglySubsumingBDDChecker = checker.isSSHOM(homCandidate);
		
//		if (stronglySubsuming != stronglySubsumingBDDChecker) {
//			System.out.println(listener.getHomTests());
//			throw new RuntimeException(selectedMutants.toString());
//		}
		if (stronglySubsumingBDDChecker) {
			updateHOMCandidates(homCandidate, homCandidates);
			
			foundHoms++;
			HashSet<String> sshom = new HashSet<>();
			sshom.addAll(selectedMutants);
			if (sshoms.contains(homCandidate)) {
				throw new RuntimeException("duplicate sshom");
			}
			distribution[selectedMutants.size()]++;
			sshoms.add(homCandidate);
			coveredFoms.addAll(selectedMutants);
			float efficiency = ((float) foundHoms * 100)/ (float)homsChecked;
			Benchmarker.instance.timestamp(foundHoms + "(" + homsChecked + "  " + coveredFoms.size()+ ") " + efficiency + "% " + String.join(",", selectedMutants));
			Benchmarker.instance.timestamp("size: " + homCandidate.size() + " div: " + computeDiv(homCandidate) + " isN+1: " + (isNPlusOne(homCandidate) == 1));
			
//			+ " is_strict_subsuming: "
//					+ CheckStronglySubsuming.isStrictStronglySubsuming(listener.getHomTests(), currentFoms));
		}
	}

	private void updateHOMCandidates(Set<FirstOrderMutant> homCandidate, Map<Integer, Set<Set<FirstOrderMutant>>> candidatesMap) {
		Collection<Set<FirstOrderMutant>> children = graph.getAllDirektChildren(homCandidate);
		for (Set<FirstOrderMutant> child : children) {
			final int div = computeDiv(child);
			final int size = child.size();
			final int score = K_DIV * div + K_SIZE * size;
			final int newScore = K_DIV * div + K_SIZE * size - K_N_PLUS_ONE * 1;
			
			Set<Set<FirstOrderMutant>> bucket = candidatesMap.get(score);
			if (bucket != null && bucket.contains(child)) {
					bucket.remove(child);
					if (bucket.isEmpty()) {
						candidatesMap.remove(score);
					}
					candidatesMap.putIfAbsent(newScore, new HashSet<>());
					candidatesMap.get(newScore).add(child);
					isNPlusOne.add(child);
			} else {
				if (!sshoms.contains(child)) {
					// TODO duplicate code
					candidatesMap.putIfAbsent(newScore, new HashSet<>());
					candidatesMap.get(newScore).add(child);
					isNPlusOne.add(child);
				}				
			}
		}
		
	}

}
