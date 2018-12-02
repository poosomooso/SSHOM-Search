package solver.sat;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import benchmark.Benchmarker;
import cmu.conditional.Conditional;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;

/**
 * This class finds the solutions using a SAT solver.
 * 
 * @author Jens Meinicke
 *
 */
public final class SATSolver {

	public final int minSize;

	public SATSolver(int minsize) {
		this.minSize = minsize;
	}

	// TODO change features to String[]
	public Set<Set<String>> getSolutions(String fileName, SingleFeatureExpr[] features,
			Function<Collection<String>, Boolean> checkValid) {
		Set<Set<String>> solutions = new HashSet<>();
		
		ISolver solver = new BoundedModelIterator(SolverFactory.newDefault(), features.length);
		DimacsReader reader = new DimacsReader(solver);
		try {
			reader.parseInstance(fileName);
		} catch (ParseFormatException | IOException | ContradictionException e) {
			e.printStackTrace();
		}
		
		try {
			while (solver.isSatisfiable()) {
				int[] model = solver.model();
				Set<String> solution = new HashSet<>();
				for (int i = 0; i < features.length; i++) {
					if (model[i] > 0) {
						solution.add(Conditional.getCTXString(features[i]));
					}
				}
				
				if (solution.size() >= minSize) {
					Benchmarker.instance.timestamp(solution.toString());
					solutions.add(solution);
				}
			}
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return solutions;
	}
}
