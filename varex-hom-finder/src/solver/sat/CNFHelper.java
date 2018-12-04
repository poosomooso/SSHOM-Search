package solver.sat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureModel;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import de.fosd.typechef.featureexpr.sat.SATFeatureModel;
import net.sf.javabdd.BDD;

/**
 * Translates {@link BDDFeatureExpr} to CNF clauses
 * 
 * @author Jens Meinicke
 *
 */
public class CNFHelper {

	public static final CNFHelper instance = new CNFHelper();

	private CNFHelper() {
		// private constructor
	}

	public List<Collection<Integer>> getClauses(Collection<FeatureExpr> expressions) {
		List<Collection<Integer>> clauses = new ArrayList<>();
		for (FeatureExpr featureExpr : expressions) {
			clauses.addAll(getClauses(featureExpr));
		}
		return clauses;
	}

	/**
	 * Extract all the paths to 0. Each of them must be translated into a clause.
	 * Let ¬A, B, C be one of your 0-paths. The relative clause will be (A ∨ ¬B ∨ ¬C).
	 * Once you have all the clauses, simply put an ∧ between them!
	 * @ see https://stackoverflow.com/questions/19488478/convert-bdd-to-cnf
	 */
	public List<Collection<Integer>> getClauses(FeatureExpr featureExpr) {
		List<Collection<Integer>> list = new ArrayList<>();
		if (featureExpr instanceof SingleFeatureExpr) {
			Collection<Integer> singleSolution = new ArrayList<>(1);
			singleSolution.add(((BDDFeatureExpr)featureExpr).bdd().var());
			list.add(singleSolution);
			return list;
		}
		return getZeroPaths(((BDDFeatureExpr) featureExpr).bdd());
	}
	
	private List<Collection<Integer>> getZeroPaths(BDD bdd) {
		List<Collection<Integer>> zeroPaths = new ArrayList<>();
		getZeroPaths(bdd, new ArrayList<>(), zeroPaths);
		return zeroPaths;
		
	}

	private void getZeroPaths(BDD bdd, List<Integer> currentPath, List<Collection<Integer>> zeroPaths) {
		int var = bdd.var();
		final BDD low = bdd.low();
		if (low.isOne()) {
			// nothing
		} else if (low.isZero()) {
			currentPath.add(var);
			zeroPaths.add(new ArrayList<>(currentPath));
			currentPath.remove(currentPath.size() - 1);
		} else {
			currentPath.add(var);
			getZeroPaths(low, currentPath, zeroPaths);
			currentPath.remove(currentPath.size() - 1);
		}
		
		final BDD high = bdd.high();
		if (high.isOne()) {
			// nothing
		} else if (high.isZero()) {
			currentPath.add(-var);
			zeroPaths.add(new ArrayList<>(currentPath));
			currentPath.remove(currentPath.size() - 1);
		} else {
			currentPath.add(-var);
			getZeroPaths(high, currentPath, zeroPaths);
			currentPath.remove(currentPath.size() - 1);
		}
	}

	public FeatureModel getModel(String fileName) {
		return SATFeatureModel.createFromDimacsFilePrefix(fileName, "");
	}

}
