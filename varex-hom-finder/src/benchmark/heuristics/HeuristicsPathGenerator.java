package benchmark.heuristics;

import java.io.File;
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
import evaluation.analysis.Mutation;
import evaluation.io.MutationParser;

/**
 * TODO description 
 * 
 * @author Jens Meinicke
 *
 */
public class HeuristicsPathGenerator implements IPathGenerator {

	private final List<FirstOrderMutant> nodes;
	private final Map<Description, Set<FirstOrderMutant>> testsMap;
	
	private Map<FirstOrderMutant, Set<FirstOrderMutant>> connections = new HashMap<>();
	private final Map<String, Mutation> mutations;

	public HeuristicsPathGenerator(List<FirstOrderMutant> nodes, Map<Description, Set<FirstOrderMutant>> testsMap) {
		this.nodes = nodes;
		this.testsMap = testsMap;
		// TODO change lookup
		mutations = MutationParser.instance.getMutations(new File("bin/evaluationfiles/" + BenchmarkPrograms.PROGRAM.toString().toLowerCase(), "mapping.txt"));
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
			getHOMCandidates(paths, entry.getKey(), conndectedNodes, Configuration.initialDegree);
			coveredStartingNodes.add(entry.getKey());
		}
		coveredStartingNodes.clear();
		return paths;
	}
	
	// TODO should not be a field
	private Set<FirstOrderMutant> coveredStartingNodes = new HashSet<>();
	
	@Override
	public Collection<Set<FirstOrderMutant>> getAllDirektChildren(Set<FirstOrderMutant> nodes) {
		if (nodes.size() >= Configuration.maxDegree) {
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
		return true;
	}

	private boolean isSameMethod(Collection<FirstOrderMutant> currentSelection) {
		Set<String> methods = new HashSet<>();
		for (FirstOrderMutant mutant : currentSelection) {
			methods.add(mutations.get(mutant.getMutant()).methodName);
		}
		return methods.size() <= 2;
	}

	private boolean isSameClass(Collection<FirstOrderMutant> currentSelection) {
		Set<String> classes = new HashSet<>();
		for (FirstOrderMutant mutant : currentSelection) {
			classes.add(mutations.get(mutant.getMutant()).className);
		}
		return classes.size() <= 1;
	}

}
