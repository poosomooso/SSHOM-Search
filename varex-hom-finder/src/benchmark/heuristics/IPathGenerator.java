package benchmark.heuristics;

import java.util.Collection;
import java.util.Set;

public interface IPathGenerator {

	Collection<HigherOrderMutant> getPaths();

	Collection<Set<FirstOrderMutant>> getAllDirektChildren(Set<FirstOrderMutant> nodes);

}
