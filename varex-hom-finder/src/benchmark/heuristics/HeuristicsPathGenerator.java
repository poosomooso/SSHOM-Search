package benchmark.heuristics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.runner.Description;

import benchmark.BenchmarkPrograms;
import benchmark.Flags;
import evaluation.analysis.Mutation;

/**
 * TODO description 
 * 
 * @author Jens Meinicke
 *
 */
public final class HeuristicsPathGenerator implements IPathGenerator {

	private final List<FirstOrderMutant> nodes;
	private final Map<Description, Set<FirstOrderMutant>> testsMap;
	
	private Map<FirstOrderMutant, Set<FirstOrderMutant>> connections = new HashMap<>();
	private final Map<String, Mutation> mutations;
	private Map<String, Set<String>> testCoverage;

	public HeuristicsPathGenerator(List<FirstOrderMutant> nodes, Map<Description, Set<FirstOrderMutant>> testsMap, Map<String, Set<String>> testCoverage) {
		this.nodes = nodes;
		this.testsMap = testsMap;
		this.testCoverage = testCoverage;
		// TODO change lookup
		mutations = BenchmarkPrograms.getMuationInfo();
	}

	
	@Override
	public Collection<HigherOrderMutant> getPaths() {
		for (FirstOrderMutant node : nodes) {
			connections.putIfAbsent(node, new HashSet<>());
			Collection<FirstOrderMutant> connectedNodes = connections.get(node);
			for (Description test : node.getTests()) {
				Set<FirstOrderMutant> failingNodes = testsMap.get(test);
				for (FirstOrderMutant other : failingNodes) {
					if (other == node) {
						continue;
					}
					ArrayList<FirstOrderMutant> hom = new ArrayList<>(2);
					hom.add(other);
					hom.add(node);
					if (fulfilsHardConstraints(hom)) {
						connectedNodes.add(other);
					}
				}
			}
		}

		final Set<HigherOrderMutant> paths = new HashSet<>();
		for (Entry<FirstOrderMutant, Set<FirstOrderMutant>> entry : connections.entrySet()) {
			Set<FirstOrderMutant> conndectedNodes = new HashSet<>(entry.getValue());
			conndectedNodes.removeAll(coveredStartingNodes);
			getHOMCandidates(paths, entry.getKey(), conndectedNodes, Configuration.getInitialDegree());
			coveredStartingNodes.add(entry.getKey());
		}
		coveredStartingNodes.clear();
		return paths;
	}
	
	// TODO should not be a field
	private Set<FirstOrderMutant> coveredStartingNodes = new HashSet<>();
	
	@Override
	public Collection<Set<FirstOrderMutant>> getAllDirektChildren(Set<FirstOrderMutant> nodes) {
		if (nodes.size() >= Configuration.getMaxDegree()) {
			return Collections.emptyList();
		}
		Set<Set<FirstOrderMutant>> children = new HashSet<>();
		
		Set<FirstOrderMutant> connectedNodes = new HashSet<>();
		for (FirstOrderMutant node : nodes) {
			connectedNodes.addAll(connections.get(node));
		}
		connectedNodes.removeAll(nodes);
		
		for (FirstOrderMutant node : connectedNodes) {
			Set<FirstOrderMutant> child = new HashSet<>(nodes);
			child.add(node);
			if (fulfilsHardConstraints(child)) {
				children.add(child);
			}
		}
		return children;
	}
	
	private void getHOMCandidates(Set<HigherOrderMutant> paths, FirstOrderMutant startNode, Set<FirstOrderMutant> nodes, int order) {
		FirstOrderMutant[] nodeArray = new FirstOrderMutant[nodes.size()];
		int index = 0;
		int startIndex = 0;
		for (FirstOrderMutant node : nodes) {
			nodeArray[index] = node;
			index++;
		}
		Set<FirstOrderMutant> currentSelection = new HashSet<>();
		currentSelection.add(startNode);
		getHOMCandidates(new HashSet<>(startNode.getTests()), paths, currentSelection, nodeArray, order - 1, startIndex);
	}
	
	private void getHOMCandidates(Set<Description> tests, Set<HigherOrderMutant> paths, Set<FirstOrderMutant> currentSelection, FirstOrderMutant[] nodeArray, int order, int startIndex) {
		if (tests.isEmpty()) {
			return;
		}
		
		
		if (currentSelection.size() >= 2) {
			if (!fulfilsHardConstraints(currentSelection)) {
				return;
			}
			HigherOrderMutant higherOrderMutant = new HigherOrderMutant(currentSelection);
			paths.add(higherOrderMutant);
		}
		if (order == 0) {
			return;
		}
		for (int i = startIndex; i < nodeArray.length; i++) {
			currentSelection.add(nodeArray[i]);
			Set<Description> intersectingTests = new HashSet<>(tests);
			intersectingTests.retainAll(nodeArray[i].getTests());
			getHOMCandidates(intersectingTests, paths, currentSelection, nodeArray, order - 1, i + 1);
			currentSelection.remove(nodeArray[i]);
		}
	}

	private boolean fulfilsHardConstraints(Collection<FirstOrderMutant> currentSelection) {
		if (!BenchmarkPrograms.homIsValidFOM(currentSelection)) {
			return false;
		}
		if (!isSameClass(currentSelection)) {
			return false;
		}
		if (!isSameMethod(currentSelection)) {
			return false;
		}
		if (hasInvalidCovereage(currentSelection)) {
			return false;
		}
		return true;
	}

	/**
	 * Checks whether the candidate can be strongly subsuming by checking if the mutants can interact (i.e., are covered by the test case) such that the test case does not fail anymore.
	 * That is, a test case must at least e covering two mutants. 
	 */
	private boolean hasInvalidCovereage(Collection<FirstOrderMutant> currentSelection) {
		if (!Flags.isCoverage()) {
			return false;
		}
		Map<String, Integer> methods = new HashMap<>();
		for (FirstOrderMutant mutant : currentSelection) {
			Mutation mutation = mutations.get(mutant.getMutant());
			String methodName = mutation.className + "." + mutation.methodName;
			methods.compute(methodName, (k, v) -> v == null ? 1 : v + 1);
		}
		if (methods.size() < 2) {
			return false;
		}
		
		Map<Description, Integer> testCounter = new HashMap<>();
		for (FirstOrderMutant node : currentSelection) {
			Set<Description> tests = node.getTests();
			for (Description t : tests) {
				testCounter.compute(t, (k, v) -> v == null ? 1 : v + 1);
			}
		}
		
		for (Entry<Description, Integer> testEntry : testCounter.entrySet()) {
			if (testEntry.getValue() < 2) {
				Description description = testEntry.getKey();					
				String testName = description.getClassName() + "." + description.getMethodName();
				Set<String> coverage = testCoverage.get(testName);
				int count = 0;
				for (Entry<String, Integer> entry : methods.entrySet()) {
					if (coverage.contains(entry.getKey())) {
						count += entry.getValue();
					}
				}
				if (count == 1) {
					return true;
				}
				assert count > 1;
			}
		}
		return false;
	}


	private boolean isSameMethod(Collection<FirstOrderMutant> currentSelection) {
		Set<String> methods = new HashSet<>();
		for (FirstOrderMutant mutant : currentSelection) {
			methods.add(mutations.get(mutant.getMutant()).methodName);
		}
		return methods.size() <= Configuration.getMAX_METHODS();
	}

	private boolean isSameClass(Collection<FirstOrderMutant> currentSelection) {
		Set<String> classes = new HashSet<>();
		for (FirstOrderMutant mutant : currentSelection) {
			classes.add(mutations.get(mutant.getMutant()).className);
		}
		return classes.size() <= Configuration.getMAX_CLASSES();
	}

}
