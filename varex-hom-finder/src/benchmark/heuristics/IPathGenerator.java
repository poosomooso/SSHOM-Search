package benchmark.heuristics;

import java.util.Collection;
import java.util.Set;

public interface IPathGenerator {

	Collection<Set<FirstOrderMutant>> getPaths();

	Collection<Set<FirstOrderMutant>> getAllDirektChildren(Set<FirstOrderMutant> nodes);

}
