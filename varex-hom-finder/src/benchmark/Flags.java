package benchmark;

/**
 * Flags to enable alternative implementations. 
 * 
 * @author Jens Meinicke
 *
 */
public final class Flags {
	
	public enum GRANULARITY {
		ALL, PACKAGE, CLASS, METHOD
	}
	
	/**
	 * Defines the splits of the code in which the algorithms search in.
	 */
	private static GRANULARITY granularity = GRANULARITY.ALL;
	
	/**
	 * Defines whether to use code coverage to avoid running redundant test cases (only for JUnit).
	 */
	private static boolean coverage = false;
	
	/**
	 * Defines whether to use JUnit to run test cases.
	 */
	private static boolean junit = false;
	
	/**
	 * Defines whether to use cached results for test times and coverage.
	 */
	private static boolean saveTestResults = false;
	
	/** 
	 * Defines the maximum time spend on the current group.
	 */
	private static int maxGroupTime = Integer.MAX_VALUE;
	
	public static GRANULARITY getGranularity() {
		return granularity;
	}
	
	public static boolean isCoverage() {
		return coverage;
	}
	
	public static boolean isJunit() {
		return junit;
	}
	
	public static boolean saveTestResults() {
		return saveTestResults;
	}
	
	public static int getMaxGroupTime() {
		return maxGroupTime;
	}
	
	private Flags() {}
	
}
