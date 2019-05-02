package benchmark.heuristics;

import java.util.Map;
import java.util.Set;

public interface ISSHOMChecker {
	
	public enum HOM_TYPE {
		NONE, STRONGLY_SUBSUMING, STRICT_STRONGLY_SUBSUMING
	}

	HOM_TYPE getHOMType(Map<String, Set<String>> testMap, HigherOrderMutant homCandidate);

}
