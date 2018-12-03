package solver.sat;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import benchmark.Benchmarker;

/**
 * This class finds the solutions using a SAT solver.
 * 
 * @author Jens Meinicke
 *
 */
public final class SATSolver {

	/**
	 * The minimum number of enables variables.
	 */
	public final int minSize;

	public SATSolver(int minsize) {
		this.minSize = minsize;
	}

	/**
	 * returns all solutions for the given dimacs file.
	 * 
	 * @param fileName The name of the dimacs file.
	 * @param features The feature names. Variables after the given names will be ignored.
	 * @return The solutions.
	 */
	public Set<Set<String>> getSolutions(String fileName, String[] features) {
		/*
		 * SolverFactory.newDefault() is suitable to solve huge SAT benchmarks. 
		 * It should reflect state-of-the-art SAT technologies.
		 * 
		 * @see http://www.sat4j.org/maven23/org.sat4j.core/apidocs/org/sat4j/minisat/SolverFactory.html#newDefault()
		 */
		ISolver solver = new BoundedModelIterator(SolverFactory.newDefault(), features.length);
		solver.setTimeout(Integer.MAX_VALUE);
		DimacsReader reader = new DimacsReader(solver);
		try {
			reader.parseInstance(fileName);
		} catch (ParseFormatException | IOException | ContradictionException e) {
			throw new RuntimeException(e);
		}
		
		Set<Set<String>> solutions = new HashSet<>();
		try {
			while (solver.isSatisfiable()) {
				int[] model = solver.model();
				Set<String> solution = new HashSet<>();
				for (int i = 0; i < features.length; i++) {
					if (model[i] > 0) {
						solution.add(features[i]);
					}
				}
				
				if (solution.size() >= minSize) {
					Benchmarker.instance.timestamp(solution.toString());
					solutions.add(solution);
				}
			}
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}
		return solutions;
	}
}
