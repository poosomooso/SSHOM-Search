package benchmark.heuristics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.runner.Description;

public final class DefaultPathGenerator implements IPathGenerator {

	private final List<FirstOrderMutant> nodes;

	public DefaultPathGenerator(List<FirstOrderMutant> nodes, Map<Description, Set<FirstOrderMutant>> testsMap) {
		this.nodes = nodes;
	}

	@Override
	public Collection<HigherOrderMutant> getPaths() {
		return getOrder(Configuration.getMaxDegree(), 0);
	}

	private List<HigherOrderMutant> getOrder(int order, int startIndex) {
		List<HigherOrderMutant> allCombinations = new ArrayList<>();
		if (order == 0) {
			return allCombinations;
		}
		if (order == 1) {
			for (int i = startIndex; i < nodes.size(); i++) {
				Set<FirstOrderMutant> selectedMutants = new HashSet<>();
				selectedMutants.add(nodes.get(i));
				allCombinations.add(new HigherOrderMutant(selectedMutants));
			}
			return allCombinations;
		}

		for (int i = startIndex; i < nodes.size(); i++) {
			List<HigherOrderMutant> selectedMutants = getOrder(order - 1, i + 1);
			for (HigherOrderMutant hom : selectedMutants) {
				Set<FirstOrderMutant> newCombination = new HashSet<>(hom.getFoms());
				newCombination.add(nodes.get(i));
				allCombinations.add(new HigherOrderMutant(newCombination));
			}
		}

		return allCombinations;
	}

	@Override
	public Collection<Set<FirstOrderMutant>> getAllDirektChildren(Set<FirstOrderMutant> nodes) {
		if (nodes.size() >= Configuration.getMaxDegree()) {
			return Collections.emptySet();
		}
		final Collection<Set<FirstOrderMutant>> children = new HashSet<>();
		for (FirstOrderMutant node : this.nodes) {
			if (!nodes.contains(node)) {
				Set<FirstOrderMutant> child = new HashSet<>(nodes);
				children.add(child);
			}
		}
		return children;
	}

}
