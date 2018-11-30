package solver.sat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import benchmark.Benchmarker;
import cmu.conditional.Conditional;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.FeatureModel;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import de.fosd.typechef.featureexpr.sat.SATFeatureExprFactory;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;

/**
 * This class finds the solutions using a SAT solver.
 * 
 * @author Jens Meinicke
 *
 */
public class SATSolver {

	public final int minSize;

	public SATSolver(int minsize) {
		this.minSize = minsize;
	}

	public Set<Set<String>> getSolutions(BDDFeatureExpr expr, String fileName) {
		return getSolutions(expr, fileName, x -> true);
	}

	public Set<Set<String>> getSolutions(BDDFeatureExpr expr, String fileName, Function<Collection<String>, Boolean> valid) {
		File dimacsFile = DimacsWriter.instance.bddToDimacsTseytinTransformation(expr, fileName);
		FeatureModel model = SatHelper.instance.getModel(dimacsFile.getName());
		Set<SingleFeatureExpr> distinctFeatureObjects = JavaConversions.setAsJavaSet(expr.collectDistinctFeatureObjects());
		return getSolutions(model, distinctFeatureObjects, valid);
	}

	public Set<Set<String>> getSolutions(FeatureModel model, SingleFeatureExpr[] features,
			Function<Collection<String>, Boolean> checkValid) {
		Set<SingleFeatureExpr> featuresSet = new HashSet<>();
		for (SingleFeatureExpr singleFeatureExpr : features) {
			featuresSet.add(FeatureExprFactory.createDefinedExternal(Conditional.getCTXString(singleFeatureExpr)));
		}
		return getSolutions(model, featuresSet, checkValid);
	}

	public Set<Set<String>> getSolutions(FeatureModel model, Set<SingleFeatureExpr> distinctFeatureObjects,
			Function<Collection<String>, Boolean> checkValid) {
		HashSet<Set<String>> solutions = new HashSet<>();
		Set<SingleFeatureExpr> mutants = new HashSet<>();
		for (SingleFeatureExpr singleFeatureExpr : distinctFeatureObjects) {
			mutants.add(SATFeatureExprFactory.createDefinedExternal(Conditional.getCTXString(singleFeatureExpr)));
		}
		final scala.collection.immutable.Set<SingleFeatureExpr> features = JavaConversions.asScalaSet(mutants).toSet();
		FeatureExprFactory.setDefault(FeatureExprFactory.sat());

		FeatureExpr sat = FeatureExprFactory.True();

		while (true) {
			// TODO use sat4j directly
			Option<Tuple2<scala.collection.immutable.List<SingleFeatureExpr>, scala.collection.immutable.List<SingleFeatureExpr>>> assignment = sat
					.getSatisfiableAssignment(model, features, true/*false does not work*/);
			if (assignment.isEmpty()) {
				break;
			}
			List<SingleFeatureExpr> selectedFeatures = new ArrayList<>(JavaConversions.asJavaCollection(assignment.get()._1));
			FeatureExpr configuration = FeatureExprFactory.True();
			Set<String> solution = new HashSet<>();
			for (SingleFeatureExpr singleFeatureExpr : selectedFeatures) {
				configuration = configuration.and(singleFeatureExpr);
				solution.add(Conditional.getCTXString(singleFeatureExpr));
			}
			if (solution.size() >= minSize && checkValid.apply(solution)) {
				Benchmarker.instance.timestamp(solution.toString());
				solutions.add(solution);
			}

			Collection<SingleFeatureExpr> deselectedFeatures = JavaConversions.asJavaCollection(assignment.get()._2);
			for (SingleFeatureExpr singleFeatureExpr : deselectedFeatures) {
				configuration = configuration.andNot(singleFeatureExpr);
			}

			sat = sat.andNot(configuration);
		}
		return solutions;
	}
}
