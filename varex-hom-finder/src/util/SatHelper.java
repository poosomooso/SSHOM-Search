package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureModel;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import de.fosd.typechef.featureexpr.sat.CNFHelper;
import de.fosd.typechef.featureexpr.sat.SATFeatureExpr;
import de.fosd.typechef.featureexpr.sat.SATFeatureModel;
import scala.collection.Iterator;
import scala.collection.Traversable;

/**
 * Helper class for the SAT package of the featureexprlib.
 * 
 * @author Jens Meinicke
 *
 */
public class SatHelper {

	public static final SatHelper instance = new SatHelper();

	private SatHelper() {
		// private constructor
	}

	public List<FeatureExpr> getClauses(Collection<FeatureExpr> expressions) {
		ArrayList<FeatureExpr> clauses = new ArrayList<>();
		for (FeatureExpr featureExpr : expressions) {
			clauses.addAll(getClauses(featureExpr));
		}
		return clauses;
	}

	public List<FeatureExpr> getClauses(FeatureExpr featureExpr) {
		SATFeatureExpr satExpr = (SATFeatureExpr)((BDDFeatureExpr)featureExpr).toSATFeatureExpr();
		satExpr = satExpr.toCNF();
		Traversable<SATFeatureExpr> clauses = CNFHelper.getCNFClauses(satExpr);
		Iterator<SATFeatureExpr> it = clauses.toIterable().iterator();
		List<FeatureExpr> list = new ArrayList<>();
		while (it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}

	public FeatureModel getModel(String fileName) {
		return SATFeatureModel.createFromDimacsFilePrefix(fileName, "");
	}
	
}
