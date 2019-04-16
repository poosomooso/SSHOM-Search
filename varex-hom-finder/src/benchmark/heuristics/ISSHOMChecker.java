package benchmark.heuristics;

import java.util.Map;
import java.util.Set;

public interface ISSHOMChecker {
	
	boolean isSSHOM(Map<String, Set<String>> testMap, HigherOrderMutant homCandidate);

}
