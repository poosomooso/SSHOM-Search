package benchmark.heuristics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.runner.Description;

public final class MutationGraph {

	private final List<FirstOrderMutant> nodes = new ArrayList<>();
	
	private final Map<Description, Set<FirstOrderMutant>> testsMap = new HashMap<>();
	
	private IPathGenerator pathGenerator = PathGeneratorFactory.create(nodes, testsMap);

	public MutationGraph(Map<String, Set<Description>> fomTestResults) {
		for (Entry<String, Set<Description>> entry : fomTestResults.entrySet()) {
			Set<Description> tests = entry.getValue();
			FirstOrderMutant node = new FirstOrderMutant(entry.getKey(), tests);
			nodes.add(node);
		}
		for (FirstOrderMutant node : nodes) {
			for (Description test : node.getTests()) {
				testsMap.putIfAbsent(test, new HashSet<>());
				testsMap.get(test).add(node);
			}
		}
	}

	public Collection<HigherOrderMutant> getHOMPaths() {
		return pathGenerator.getPaths();
	}

	public Collection<Set<FirstOrderMutant>> getAllDirektChildren(Set<FirstOrderMutant> homCandidate) {
		return pathGenerator.getAllDirektChildren(homCandidate);
	}
	
}
