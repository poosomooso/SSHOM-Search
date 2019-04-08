package benchmark.heuristics;

import java.util.Collections;
import java.util.Set;

import org.junit.runner.Description;

public final class FirstOrderMutant {
	private final String mutant;
	private final Set<Description> tests;
	private final int hash;

	public FirstOrderMutant(String mutant, Set<Description> tests) {
		this.mutant = mutant;
		this.tests = Collections.unmodifiableSet(tests);
		this.hash = mutant.hashCode();
	}
	
	public String getMutant() {
		return mutant;
	}
	
	public Set<Description> getTests() {
		return tests;
	}
	
	@Override
	public boolean equals(Object other) {
		return this == other;
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
}