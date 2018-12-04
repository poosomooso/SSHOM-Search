package solver.sat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cmu.conditional.Conditional;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.FExprBuilder;
import de.fosd.typechef.featureexpr.sat.Not;
import de.fosd.typechef.featureexpr.sat.Or;
import de.fosd.typechef.featureexpr.sat.SATFeatureExpr;
import de.fosd.typechef.featureexpr.sat.SATFeatureModel;
import net.sf.javabdd.BDD;
import scala.collection.Iterator;
import scala.collection.JavaConversions;

/**
 * This class prints {@link BDDFeatureExpr} to dimacs files using Tseytin Transformations.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Tseytin_transformation">Wikipedia: Tseytin Transformation</a>
 * 
 * @author Jens Meinicke
 *
 */
public class DimacsWriter {

	public static final DimacsWriter instance = new DimacsWriter();

	private DimacsWriter() {
		// private constructor
	}

	/**
	 * Uses the implementation in {@link SATFeatureModel} to create a dimacs file.<br>
	 * 
	 * <b>Do not use for large BDDs</b>
	 */
	public void printToDimacsDirect(FeatureExpr expr, String fileName) {
		if (expr instanceof BDDFeatureExpr) {
			expr = ((BDDFeatureExpr) expr).toSATFeatureExpr();
		}
		SATFeatureModel fm = (SATFeatureModel) SATFeatureModel.create(expr);
		fm.writeToDimacsFile(new File(fileName + ".dimacs"));
	}
	
	/**
	 * Uses the Tseytin Transformation to create a dimacs file.<br>
	 */
	public File bddToDimacsTseytinTransformation(BDDFeatureExpr expr, String fileName) {
		File file = new File(fileName + ".dimacs");
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
			instance.bddToDimacsTseytinTransformation(expr, out);
			return file;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Uses the Tseytin Transformation to create a dimacs file.<br>
	 */
	public void bddToDimacsTseytinTransformation(BDDFeatureExpr expr, OutputStream out) {
		Collection<FeatureExpr> expressions = new ArrayList<>();
		Collection<SingleFeatureExpr> variables = createExpressions(expr, expressions);

		Set<SingleFeatureExpr> features = JavaConversions.setAsJavaSet(expr.collectDistinctFeatureObjects());
		printToDimacs(features, variables, expressions, out);
	}

	private Collection<SingleFeatureExpr> createExpressions(BDDFeatureExpr expr, Collection<FeatureExpr> expressions) {
		final Map<BDD, SingleFeatureExpr> variables = new HashMap<>();
		final Deque<BDD> stack = new ArrayDeque<>();
		if (!expr.isTautology() && !expr.isContradiction()) {
			stack.add(expr.bdd());
		}
		FeatureExpr rootVariable = getOrCreateVariable(expr.bdd(), variables);
		
		if (expr.isContradiction()) {
			expressions.add(rootVariable.not());
			expressions.add(rootVariable);
		} else if (expr.isTautology()) {
			expressions.add(rootVariable.or(rootVariable).not());
		} else {
			expressions.add(rootVariable);
		}
		Set<BDD> covered = new HashSet<>();
		while (!stack.isEmpty()) {
			BDD bdd = stack.pop();
			BDD low = bdd.low();
			BDD high = bdd.high();
			
			SingleFeatureExpr feature = FeatureExprFactory.createDefinedExternal(FExprBuilder.lookupFeatureName(bdd.var()));
			FeatureExpr lowExpr = createExpr(feature.not(), low, variables);
			FeatureExpr highExpr = createExpr(feature, high, variables);

			FeatureExpr nodeExpr = getOrCreateVariable(bdd, variables);
			FeatureExpr current;
			if (lowExpr == null) {
				current = nodeExpr.equiv(highExpr);
			} else if (highExpr == null) {
				current = nodeExpr.equiv(lowExpr);
			} else {
				current = nodeExpr.equiv(lowExpr.or(highExpr));
			}
			expressions.add(current);

			if (!low.isOne() && !low.isZero() && !covered.contains(low)) {
				stack.push(low);
				covered.add(low);
			}
			if (!high.isOne() && !high.isZero() && !covered.contains(high)) {
				stack.push(high);
				covered.add(high);
			}
		}
		return variables.values();
	}

	private FeatureExpr createExpr(FeatureExpr feature, BDD next, Map<BDD, SingleFeatureExpr> nodeVariables) {
		FeatureExpr highExpr;
		if (next.isZero()) {
			highExpr = null;
		} else {
			if (next.isOne()) {
				highExpr = feature;
			} else {
				highExpr = feature.and(getOrCreateVariable(next, nodeVariables));
			}
		}
		return highExpr;
	}

	private SingleFeatureExpr getOrCreateVariable(BDD bdd, Map<BDD, SingleFeatureExpr> nodeVariables) {
		return nodeVariables.computeIfAbsent(bdd, k -> FeatureExprFactory.createDefinedExternal("nodevar" + bdd.hashCode()));
	}

	private void printToDimacs(Collection<SingleFeatureExpr> features, Collection<SingleFeatureExpr> nodeVariables,
			Collection<FeatureExpr> expressions, OutputStream stream) {
		Map<String, Integer> featureMap = new HashMap<>();
		
		// TODO add featureNames[]
		List<SingleFeatureExpr> sortedFeatures = new ArrayList<>(features);
		Collections.sort(sortedFeatures, (f1, f2) -> f1.feature().compareTo(f2.feature()));
		
		try (PrintWriter out = new PrintWriter(stream)) {
			// print features
			int index = printVariables(sortedFeatures, featureMap, out, 1);
			// print node variables
			printVariables(nodeVariables, featureMap, out, index);
			// expressions need to be transformed to CNF
			List<Collection<Integer>> clauses = CNFHelper.instance.getClauses(expressions);
			out.print("p cnf ");
			out.print(featureMap.size());
			out.print(' ');
			out.println(clauses.size());

			printClauses(clauses, featureMap, out);
		}
	}

	private void printClauses(List<Collection<Integer>> clauses, Map<String, Integer> featureMap, PrintWriter out) {
		for (Collection<Integer> c : clauses) {
			String line = expressionToClause(c, featureMap);
			out.println(line);
		}
	}

	private String expressionToClause(Collection<Integer> c, Map<String, Integer> featureMap) {
		return exprToString(c,featureMap);
	}
	
	public String exprToString(final Collection<Integer> c, Map<String, Integer> featureMap) {
		StringBuilder sb = new StringBuilder();
		
		for (int var : c) {
			if (var < 0) {
				sb.append('-');
			}
			sb.append(getFeatureID(FExprBuilder.lookupFeatureName(Math.abs(var)), featureMap));
			sb.append(' ');
		}
		sb.append(0);
		return sb.toString();
	}
	
	public String exprToString(final FeatureExpr ctx, Map<String, Integer> featureMap) {
		StringBuilder sb = new StringBuilder();
		if (ctx instanceof SingleFeatureExpr) {
			return getFeatureID(((SingleFeatureExpr) ctx).feature(), featureMap) + " 0";
		}
		if (ctx instanceof Or) {
			scala.collection.immutable.Set<SATFeatureExpr> clauses = ((Or) ctx).clauses();
			Iterator<SATFeatureExpr> it = clauses.iterator();
			while (it.hasNext()) {
				SATFeatureExpr next = it.next();
				String feature;
				if (next instanceof Not) {
					sb.append('-');
					SingleFeatureExpr e = (SingleFeatureExpr)((Not)next).expr();
					feature = e.feature();
				} else {
					feature = ((SingleFeatureExpr)next).feature();
				}
				sb.append(getFeatureID(feature, featureMap));
				sb.append(' ');
			 }
			sb.append('0');
			return sb.toString();
		}
		throw new RuntimeException(Conditional.getCTXString(ctx));
	}

	private int getFeatureID(String feature, Map<String, Integer> featureMap) {
		if (!featureMap.containsKey(feature)) {
			throw new RuntimeException("Feature not found: " + feature);
		}
		return featureMap.get(feature);
	}

	private int printVariables(Collection<SingleFeatureExpr> nodes, Map<String, Integer> featureMap, PrintWriter out, int index) {
		for (SingleFeatureExpr featureExpr : nodes) {
			final String variableName = featureExpr.feature();
			printNextVariable(out, index, variableName);
			featureMap.put(variableName, index);
			index++;
		}
		return index;
	}

	private void printNextVariable(PrintWriter out, int index, String variableName) {
		out.print("c ");
		out.print(index);
		out.print(' ');
		out.println(variableName);
	}

}
