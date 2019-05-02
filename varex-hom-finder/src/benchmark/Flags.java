package benchmark;

import java.lang.reflect.Field;

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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void initialize() {
		System.out.println("### initialize: " + Flags.class.getName());
		try {
			Field[] flags = Flags.class.getDeclaredFields();
			for (Field flag : flags) {
				final Object value;
				if (flag.getType() == boolean.class) {
					String valueString = System.getProperty(flag.getName(), Boolean.toString(flag.getBoolean(null)));
					value = Boolean.parseBoolean(valueString);
				} else if (flag.getType() == int.class) {
					String valueString = System.getProperty(flag.getName(), Integer.toString(flag.getInt(null)));
					value = Integer.parseInt(valueString);
				} else if (flag.getType().isEnum()) {
					String valueString = System.getProperty(flag.getName(), flag.get(null).toString());
					value = Enum.valueOf((Class<Enum>) flag.getType(), valueString);
				} else {
					System.err.println("Flag not supported: " + flag);
					continue;
				}
				flag.set(null, value);
				System.out.println("### " + flag.getName() + ": " + flag.get(null));
			}
				
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
}
