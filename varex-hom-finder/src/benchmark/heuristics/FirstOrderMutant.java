package benchmark.heuristics;

import java.util.Collections;
import java.util.Set;

import org.junit.runner.Description;

public final class FirstOrderMutant {
	final String mutant;
	final Set<Description> tests;

	public FirstOrderMutant(String mutant, Set<Description> tests) {
		this.mutant = mutant;
		this.tests = Collections.unmodifiableSet(tests);
	}
	
	public String getMutant() {
		return mutant;
	}
	
	public Set<Description> getTests() {
		return tests;
	}
}