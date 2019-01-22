package evaluation.analysis;
import java.util.Collection;
import java.util.HashSet;

public class HOM {
	
	public final Collection<Mutation> foms = new HashSet<>();
	
	public void addFOM(Mutation m) {
		foms.add(m);
	}

	@Override
	public String toString() {
		return "HOM " + foms;
	}
	
	public int getOrder() {
		return foms.size();
	}
	
	public boolean containsFom(Mutation m) {
		return foms.contains(m);
	}
	
}
