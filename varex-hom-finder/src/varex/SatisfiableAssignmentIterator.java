package varex;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.FeatureModel;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureModel;
import de.fosd.typechef.featureexpr.sat.SATFeatureModel;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.immutable.List;
import scala.collection.immutable.Set;
import scala.collection.mutable.HashSet;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SatisfiableAssignmentIterator implements Iterator<Tuple2<List<SingleFeatureExpr>, List<SingleFeatureExpr>>>{
  private FeatureExpr                                                      expr;
  private Option<Tuple2<List<SingleFeatureExpr>, List<SingleFeatureExpr>>> nextSatisfiableAssignment;
  private Set<SingleFeatureExpr>                                           interestingFeatures;

  private FeatureModel featureModel;

  static {
    FeatureExprFactory.setDefault(FeatureExprFactory.bdd());
  }

  public SatisfiableAssignmentIterator(FeatureExpr[] mutants,
      FeatureExpr finalExpr, FeatureModel model) {
    expr = finalExpr;

    HashSet<FeatureExpr> interestingFeaturesMutable = new HashSet<>();
    for (FeatureExpr m : mutants) {
      interestingFeaturesMutable.add(m);
    }
    interestingFeatures = interestingFeaturesMutable.toSet();
    setNextSatisfiableAssignment();
  }

  @Override
  public boolean hasNext() {
    return nextSatisfiableAssignment.nonEmpty();
  }

  @Override
  public Tuple2<List<SingleFeatureExpr>, List<SingleFeatureExpr>> next() {
    Option<Tuple2<List<SingleFeatureExpr>, List<SingleFeatureExpr>>> nextVal = this.nextSatisfiableAssignment;
    if (nextVal.isEmpty()) {
      throw new NoSuchElementException();
    }
    setNextSatisfiableAssignment();
    return nextVal.get();
  }

  private void setNextSatisfiableAssignment() {
    nextSatisfiableAssignment = expr
        .getSatisfiableAssignment(featureModel, interestingFeatures, true);

    if (nextSatisfiableAssignment.nonEmpty()) {
      // exclude the new assignment
      FeatureExpr thisExpr = FeatureExprFactory.True();
      for (FeatureExpr e : JavaConversions.asJavaCollection(nextSatisfiableAssignment.get()._1)) {
        thisExpr = thisExpr.and(e);
      }

      for (FeatureExpr e : JavaConversions.asJavaCollection(nextSatisfiableAssignment.get()._2)) {
        thisExpr = thisExpr.andNot(e);
      }

      expr = expr.andNot(thisExpr);
    }
  }
}
