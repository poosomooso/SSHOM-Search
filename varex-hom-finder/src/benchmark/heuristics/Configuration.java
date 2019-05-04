package benchmark.heuristics;

public final class Configuration {
	
	/**
	 * Defines the highest degree to search for
	 */
	public static int maxDegree = 6;
	
	/**
	 * Defines the initial degree of the search
	 */
	public static int initialDegree = 2;
	
	/** 
	 * Maximum number of methods in SSHOMS.
	 */
	public static int MAX_METHODS = 4;
	
	/** 
	 * Maximum number of classes in SSHOMS.
	 */
	public static int MAX_CLASSES = 3;
	
	/*
	 * Weights for prioritization.
	 */
	public static int K_TEST_OVERLAP = 1;
	public static int K_DEGREE = 5;
	public static int K_N_PLUS_ONE = 15;
	// TODO prioritize based on nr methods
	// TODO prioritize based on nr classes

	private Configuration() {
	}
}
