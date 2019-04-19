package benchmark.heuristics;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.runner.Description;

public final class TestRunListener {

	private Map<String, Set<String>> testMap;
	
	private Set<String> currentEntry = new HashSet<>();

	public TestRunListener(Map<String, Set<String>> testMap) {
		this.testMap = testMap;
		this.testMap.put(null, currentEntry);
	}
	

	public void testStarted(Description description) {
		testStarted(description.getClassName(), description.getMethodName());
	}

	public void testStarted(String testClass, String testName) {
		currentEntry = new HashSet<>();
		this.testMap.put(testClass + "." + testName, currentEntry);
	}

	public void methodExecuted(String className, String methodName) {
		currentEntry.add(className + "." + methodName);
	}
	
	public void methodExecuted(StackTraceElement method) {
		currentEntry.add(method.getClassName() + "." + method.getMethodName());
	}
	
	public void methodExecuted(String className) {
		currentEntry.add(className);
	}

}
