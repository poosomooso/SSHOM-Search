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
	
	/** 
	 * Maximum number of methods in SSHOMS.
	 */
	public static final int MAX_METHODS = 4;
	
	/** 
	 * Maximum number of classes in SSHOMS.
	 */
	public static final int MAX_CLASSES = 3;
	
	/*
	 * Weights for prioritization.
	 */
	public static final int K_TEST_OVERLAP = 1;
	public static final int K_DEGREE = 5;
	public static final int K_N_PLUS_ONE = 15;
	// TODO prioritize based on nr methods
	// TODO prioritize based on nr classes

	private Configuration() {
	}
}
