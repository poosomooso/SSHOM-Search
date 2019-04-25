package benchmark.heuristics;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.runner.Description;

public class PathGeneratorFactory {

	enum PATH_GENERATOR {
		DEFAULT, SMART
	}
	
	public static PATH_GENERATOR selectedGenrator = PATH_GENERATOR.SMART;
	
	public static IPathGenerator create(List<FirstOrderMutant> nodes, Map<Description, Set<FirstOrderMutant>> testsMap, Map<String, Set<String>> testCoverage) {
		switch (selectedGenrator) {
		case DEFAULT:
			return new DefaultPathGenerator(nodes, testsMap);
		case SMART:
			return new HeuristicsPathGenerator(nodes, testsMap, testCoverage);
		default:
			throw new RuntimeException("case missing: " + selectedGenrator);
		}
		
	}

}
