package benchmark.heuristics;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * TODO description
 * 
 * @author Jens Meinicke
 *
 */
public class HigherOrderMutant implements Comparable<HigherOrderMutant>, Iterable<FirstOrderMutant> {

	private final Set<FirstOrderMutant> foms;
	private final int hash;
	
	public HigherOrderMutant(Collection<FirstOrderMutant> foms) {
		this.foms = Collections.unmodifiableSet(new HashSet<>(foms));
		this.hash = createHashCode(foms);
		assert size() >= 2;
	}

	private int createHashCode(Collection<FirstOrderMutant> foms) {
		int hashCode = 31;
		for (FirstOrderMutant firstOrderMutant : foms) {
			hashCode *= firstOrderMutant.hashCode();
		}
		return hashCode;
	}

	@Override
	public int compareTo(HigherOrderMutant other) {
		return other.hash - this.hash;
	}
	
	public Set<FirstOrderMutant> getFoms() {
		return foms;
	}
	
	public int size() {
		return foms.size();
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HigherOrderMutant other = (HigherOrderMutant) obj;
		if (hash != other.hash)
			return false;
		return foms.equals(other.foms);
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public Iterator<FirstOrderMutant> iterator() {
		return foms.iterator();
	}
	
	@Override
	public String toString() {
		return foms.toString();
	}
	
}
