package benchmark.heuristics;

public final class Configuration {

	/**
	 * Defines the highest degree to search for
	 */
	private static int maxDegree = 6;

	/**
	 * Defines the initial degree of the search
	 */
	private static int initialDegree = 5;

	/**
	 * Maximum number of methods in SSHOMS.
	 */
	private static int MAX_METHODS = 4;

	/**
	 * Maximum number of classes in SSHOMS.
	 */
	private static int MAX_CLASSES = 3;

	/*
	 * Weights for prioritization.
	 */
	private static int K_TEST_OVERLAP = 1;
	private static int K_DEGREE = 3;
	private static int K_N_PLUS_ONE = 15;
	// TODO prioritize based on nr methods
	// TODO prioritize based on nr classes

	private Configuration() {
	}
	
	public static int getInitialDegree() {
		return initialDegree;
	}
	
	public static int getK_DEGREE() {
		return K_DEGREE;
	}
	
	public static int getK_N_PLUS_ONE() {
		return K_N_PLUS_ONE;
	}
	
	public static int getK_TEST_OVERLAP() {
		return K_TEST_OVERLAP;
	}
	
	public static int getMAX_CLASSES() {
		return MAX_CLASSES;
	}
	
	public static int getMAX_METHODS() {
		return MAX_METHODS;
	}
	
	public static int getMaxDegree() {
		return maxDegree;
	}
	
}
