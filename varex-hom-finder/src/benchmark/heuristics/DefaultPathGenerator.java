package benchmark.heuristics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.runner.Description;

public final class DefaultPathGenerator implements IPathGenerator {

	private final List<FirstOrderMutant> nodes;
	private final Map<Description, Set<FirstOrderMutant>> testsMap;

	public DefaultPathGenerator(List<FirstOrderMutant> nodes, Map<Description, Set<FirstOrderMutant>> testsMap) {
		this.nodes = nodes;
		this.testsMap = testsMap;
	}

	@Override
	public Collection<Set<FirstOrderMutant>> getPaths() {// would be nice to have an iterator instead
		return getOrder(3, 0);
	}

	private List<Set<FirstOrderMutant>> getOrder(int order, int startIndex) {
		List<Set<FirstOrderMutant>> allCombinations = new ArrayList<>();
		if (order == 0) {
			return allCombinations;
		}
		if (order == 1) {
			for (int i = startIndex; i < nodes.size(); i++) {
				Set<FirstOrderMutant> selectedMutants = new HashSet<>();
				selectedMutants.add(nodes.get(i));
				allCombinations.add(selectedMutants);
			}
			return allCombinations;
		}

		for (int i = startIndex; i < nodes.size(); i++) {
			List<Set<FirstOrderMutant>> selectedMutants = getOrder(order - 1, i + 1);
			for (Collection<FirstOrderMutant> collection : selectedMutants) {
				Set<FirstOrderMutant> newCombination = new HashSet<>(collection);
				newCombination.add(nodes.get(i));
				allCombinations.add(newCombination);
			}
		}

		return allCombinations;
	}

	@Override
	public Collection<Set<FirstOrderMutant>> getAllDirektChildren(Set<FirstOrderMutant> nodes) {
		throw new RuntimeException("not implemented");
	}

}
