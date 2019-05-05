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
import benchmark.heuristics.ISSHOMChecker.HOM_TYPE;
import benchmark.heuristics.MutationGraph;
import benchmark.heuristics.SSHOMJUnitChecker;
import util.SSHOMListener;
import util.SSHOMRunner;

/**
 * TODO description
 * 
 * @author Jens Meinicke
 *
 */
public class HeuristicsBasedSSHOMFinder {
	
	private final long TIME_FOMS = Flags.getMaxGroupTime() / 3;
	
	private SSHOMRunner runner;
	private final Map<String, Set<Description>> foms = new HashMap<>();
	
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
		InfLoopTestProcess.createTestCovereage(testClasses);
		InfLoopTestProcess.setTimeOutListener();
	}
	
	public void run() throws NoSuchFieldException, IllegalAccessException {
		Benchmarker.instance.timestamp("start homs");
		Map<String, Set<String>> groupMutants = BenchmarkPrograms.createMutationGroups();
		
		groupLoop: for (Entry<String, Set<String>> groupEntry : groupMutants.entrySet()) {
			long start = System.currentTimeMillis();
			System.out.println("group: " + groupEntry.getKey());
			System.out.println("nr mutatns: " + groupEntry.getValue().size());
			foms.clear();
			populateFoms(groupEntry.getValue(), start);
			Benchmarker.instance.timestamp("create hom candidates");
			graph = new MutationGraph(foms, InfLoopTestProcess.testCoverageMap);

			// Map<package, Graph>
			// merge hom candidates?
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
					if (time - start > Flags.getMaxGroupTime()) {
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

	// TODO this should be the direct result of the graph
	private Map<Integer, Set<HigherOrderMutant>> createHOMMap(Collection<HigherOrderMutant> homPaths) {
		Map<Integer, Set<HigherOrderMutant>> candidates = new HashMap<>();
		for (Iterator<HigherOrderMutant> iterator = homPaths.iterator(); iterator.hasNext();) {
			HigherOrderMutant c = iterator.next();
			int score = Configuration.getK_TEST_OVERLAP() * computeDiv(c.getFoms()) + Configuration.getK_DEGREE() * c.size() - Configuration.getK_N_PLUS_ONE() * (c.size() == 2 ? 1 : 0);
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

	private void populateFoms(Collection<String> mutants, long start) throws NoSuchFieldException, IllegalAccessException {
		Benchmarker.instance.timestamp("populateFoms");
		for (String m : mutants) {
			SSHOMListener listener;
			if (BenchmarkPrograms.programHasInfLoops()) {
				listener = InfLoopTestProcess.getFailedTests(testClasses, new String[] { m });
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
		HOM_TYPE homType = checker.getHOMType(InfLoopTestProcess.testCoverageMap, homCandidate);
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
			final int oldScore = Configuration.getK_TEST_OVERLAP() * div + Configuration.getK_DEGREE() * size;
			final int newScore = Configuration.getK_TEST_OVERLAP() * div + Configuration.getK_DEGREE() * size - Configuration.getK_N_PLUS_ONE();
			
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
