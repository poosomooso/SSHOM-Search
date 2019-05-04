package benchmark.heuristics;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.runner.Description;

/**
 * TODO description
 * 
 * @author Jens Meinicke
 *
 */
public final class FirstOrderMutant implements Comparable<FirstOrderMutant> {
	
	private final String mutant;
	private final Set<Description> tests;
	private final int hash;

	public FirstOrderMutant(String mutant, Set<Description> tests) {
		this.mutant = mutant;
		this.tests = Collections.unmodifiableSet(new HashSet<>(tests));
		this.hash = mutant.hashCode();
	}
	
	public String getMutant() {
		return mutant;
	}
	
	public Set<Description> getTests() {
		return tests;
	}
	
	@Override
	public String toString() {
		return mutant;
	}

	
	@Override
	public boolean equals(Object other) {
		return this == other;
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	@Override
	public int compareTo(FirstOrderMutant other) {
		if (hashCode() != other.hashCode()) {
			return other.hashCode() - hashCode();
		}
		return mutant.compareTo(other.mutant);
	}
}