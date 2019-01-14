package solver.sat;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;
import org.sat4j.tools.SolverDecorator;

/**
 * This class allows to iterate through all the models (implicants) of a formula. 
 * In contrast to {@link ModelIterator}, it only considers the first n variables
 * 
 * <pre>
 * ISolver solver = new BoundedModelIterator(SolverFactory.newDefault());
 * while (solver.isSatisfiable()) {
 *     int[] model = solver.model();
 *     // do something with model
 * }
 * </pre>
 * 
 * @see ModelIterator
 * @author Jens Meinicke
 *
 */
public class BoundedModelIterator extends SolverDecorator {

	private static final long serialVersionUID = 1L;

	private boolean trivialfalsity = false;

	private final int numVariables;

    public BoundedModelIterator(ISolver solver, int numVariables) {
        super(solver);
        this.numVariables = numVariables;
    }

    @Override
    public int[] model() {
        int[] last = super.model();
        IVecInt clause = new VecInt(last.length);
        for (int i = 0; i < numVariables; i++) {
        	clause.push(-last[i]);
        }
        try {
            addClause(clause);
        } catch (ContradictionException e) {
            trivialfalsity = true;
        }
        return last;
    }

    @Override
    public boolean isSatisfiable() throws TimeoutException {
        if (trivialfalsity) {
            return false;
        }
        trivialfalsity = false;
        return super.isSatisfiable(true);
    }

    @Override
    public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
        if (trivialfalsity) {
            return false;
        }
        trivialfalsity = false;
        return super.isSatisfiable(assumps,true);
    }

    @Override
    public void reset() {
        trivialfalsity = false;
        super.reset();
    }
}
