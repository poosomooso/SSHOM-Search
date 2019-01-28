package solver.bdd;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import benchmark.Benchmarker;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;

/**
 * This class finds the solutions using BDDs.
 * 
 * @author Jens Meinicke
 *
 */
public class BDDSolver {
	
	public final int minSize;

	public BDDSolver(int minsize) {
		this.minSize = minsize;
	}

	public Set<Set<String>> getSolutions(BDDFeatureExpr expr, String[] features) {
		return getSolutions(expr, features, x -> true);
	}
	
	@SuppressWarnings("unchecked")
	public Set<Set<String>> getSolutions(BDDFeatureExpr expr, String[] features, Function<Collection<String>, Boolean> checkValid) {
		List<byte[]> solutions = (List<byte[]>)expr.bdd().allsat();
		Set<Set<String>> allSolutions = new LinkedHashSet<>();
		int counter = 0;
		for (byte[] s : solutions) {
			if (counter++ > 1000000) return allSolutions;

			getSolutions(s, features, allSolutions, new HashSet<>(), 1, checkValid);
		}
		return allSolutions;
	}

	/**
	 * In the byte arrays, -1 means dont-care, 0 means 0, and 1 means 1.
	 * So we need to expand the given solution.
	 * @param solutions
	 * @param features
	 */
	private void getSolutions(byte[] solutions, String[] features, Set<Set<String>> allSolutions, Set<String> selections, int start, Function<Collection<String>, Boolean> checkValid) {
		if (selections.size() >= minSize && !checkValid.apply(selections)) {
			return;
		}
		
		for (int i = start; i <= features.length; i++) {
			byte selection = solutions[i];
			if (selection != 0) {
				if (selection == -1) {
					byte[] copy = new byte[features.length + 1];
					for (int j = i + 1; j <= features.length; j++) {
						copy[j] = solutions[j];
					}
					getSolutions(copy, features, allSolutions, new HashSet<>(selections), i + 1, checkValid);
				}
				
				selections.add(features[i - 1]);
				if (selections.size() >= minSize && !checkValid.apply(selections)) {
					return;
				}
				solutions[i] = 1;
			}
		}
		if (selections.size() >= minSize) { 
			Benchmarker.instance.timestamp(selections.toString());
			allSolutions.add(selections);
		}
	}
}
