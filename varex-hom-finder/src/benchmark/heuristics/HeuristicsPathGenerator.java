package benchmark.heuristics;

import java.io.File;
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

public class HeuristicsPathGenerator implements IPathGenerator {

	private static final int maxDegree = 6;
	private static final int initialDegree = 2;

	private final List<FirstOrderMutant> nodes;
	private final Map<Description, Set<FirstOrderMutant>> testsMap;
	
	private Map<FirstOrderMutant, Set<FirstOrderMutant>> connections = new HashMap<>();
	private final Map<String, Mutation> mutations;

	public HeuristicsPathGenerator(List<FirstOrderMutant> nodes, Map<Description, Set<FirstOrderMutant>> testsMap) {
		this.nodes = nodes;
		this.testsMap = testsMap;
		// TODO
		mutations = MutationParser.instance.getMutations(new File("bin/evaluationfiles/" + BenchmarkPrograms.PROGRAM.toString().toLowerCase(), "mapping.txt"));
	}

	
	@Override
	public Collection<Set<FirstOrderMutant>> getPaths() {
		for (FirstOrderMutant node : nodes) {
			connections.putIfAbsent(node, new HashSet<>());
			Collection<FirstOrderMutant> connectedNodes = connections.get(node);
			for (Description test : node.tests) {
				Set<FirstOrderMutant> failingNodes = testsMap.get(test);
				for (FirstOrderMutant other : failingNodes) {
					if (other == node) {
						continue;
					}
					if (BenchmarkPrograms.homIsValid(other.mutant, node.mutant)) {
						connectedNodes.add(other);
					}
				}
			}
		}

		final Collection<Set<FirstOrderMutant>> paths = new HashSet<>();
		int i = 1;
		for (Entry<FirstOrderMutant, Set<FirstOrderMutant>> entry : connections.entrySet()) {
			getHOMCandidates(paths, entry.getKey(), entry.getValue(), initialDegree);
			System.out.println(i++ + " / " + connections.size() + " (" + paths.size() + ")");
		}
		return paths;
	}
	
	@Override
	public Collection<Set<FirstOrderMutant>> getAllDirektChildren(Set<FirstOrderMutant> nodes) {
		if (nodes.size() >= maxDegree) {
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
	
	
	private void getHOMCandidates(Collection<Set<FirstOrderMutant>> paths, FirstOrderMutant startNode, Set<FirstOrderMutant> nodes, int order) {
		FirstOrderMutant[] nodeArray = new FirstOrderMutant[nodes.size()];
		int index = 0;
		int startIndex = 0;
		for (FirstOrderMutant node : nodes) {
			nodeArray[index] = node;
			index++;
		}
		Set<FirstOrderMutant> currentSelection = new HashSet<>(order);
		currentSelection.add(startNode);
		getHOMCandidates(new HashSet<>(startNode.tests), paths, currentSelection, nodeArray, order - 1, startIndex);
	}
	
	private void getHOMCandidates(Set<Description> tests, Collection<Set<FirstOrderMutant>> paths, Set<FirstOrderMutant> currentSelection, FirstOrderMutant[] nodeArray, int order, int startIndex) {
		if (tests.isEmpty()) {
			return;
		}
		
		if (!fulfilsHardConstraints(currentSelection)) {
			return;
		}
		
		if (currentSelection.size() >= 2) {
			paths.add(new HashSet<>(currentSelection));
		}
		if (order == 0) {
			return;
		}
		for (int i = startIndex; i <= nodeArray.length - order; i++) {
			currentSelection.add(nodeArray[i]);
			if (!paths.contains(currentSelection)) {
				Set<Description> intersectingTests = new HashSet<>(tests);
				intersectingTests.retainAll(nodeArray[i].tests);
				getHOMCandidates(intersectingTests, paths, currentSelection, nodeArray, order - 1, i + 1);
			}
			currentSelection.remove(nodeArray[i]);
		}
	}

	private boolean fulfilsHardConstraints(Collection<FirstOrderMutant> currentSelection) {
		// TODO rewrite this (do not create new set)
		Collection<String> config = new HashSet<>(currentSelection.size());
		for (FirstOrderMutant node : currentSelection) {
			config.add(node.mutant);
		}
		if (!BenchmarkPrograms.homIsValid(config)) {
			return false;
		}
		if (!isSameClass(config)) {
			return false;
		}
		if (!isSameMethod(config)) {
			return false;
		}
		return true;
	}

	private boolean isSameMethod(Collection<String> config) {
		Set<String> methods = new HashSet<>();
		for (String mutant : config) {
			methods.add(mutations.get(mutant).methodName);
		}
		return methods.size() <= 2;
	}

	private boolean isSameClass(Collection<String> config) {
		Set<String> classes = new HashSet<>();
		for (String mutant : config) {
			classes.add(mutations.get(mutant).className);
		}
		return classes.size() <= 1;
	}

}
