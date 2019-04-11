package benchmark.heuristics;

public final class Configuration {
	
	/**
	 * Defines the highest degree to search for
	 */
	public static final int maxDegree = 6;
	
	/**
	 * Defines the initial degree of the search
	 */
	public static final int initialDegree = 2;
	
	/*
	 * Weights for prioritization.
	 */
	public static final int K_TEST_OVERLAP = 1;
	public static final int K_DEGREE = 5;
	public static final int K_N_PLUS_ONE = 100;

	private Configuration() {
	}
}
