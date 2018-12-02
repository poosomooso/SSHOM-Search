package solver.sat;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import solver.bdd.BDDSolver;

@RunWith(Parameterized.class)
public class DimacsWriterTest {

	private final static String[] features = new String[] {"A", "B", "C"};
	private final static FeatureExpr[] f = new FeatureExpr[features.length];
	
	static {
		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());
		for (int i = 0; i < f.length; i++) {
			f[i] = FeatureExprFactory.createDefinedExternal(features[i]);
		}
	}
	
	@Parameters(name = "{1}")
	public static Collection<Object[]> expressions() {
		Collection<Object[]> expressions = new ArrayList<>();
		expressions.add(new Object[] {(BDDFeatureExpr)f[0].implies(f[1].or(f[2])), "A->(B or C)", 3});
		expressions.add(new Object[] {(BDDFeatureExpr)f[0].and(f[1]), "A and B", 2});
		expressions.add(new Object[] {(BDDFeatureExpr)f[0].or(f[1]), "A or B", 2});
		expressions.add(new Object[] {(BDDFeatureExpr)f[0].implies(f[1]), "A -> B", 2});
		expressions.add(new Object[] {(BDDFeatureExpr)f[0].andNot(f[1]), "A and not B", 2});
		return expressions;
	}
	
	@Parameter
	public BDDFeatureExpr expr;
	@Parameter(1)
	public String description;
	@Parameter(2)
	public int length;
	
	@Test
	public void testName() throws Exception {
		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());
		checkExpression(expr);
	}

	private void checkExpression(BDDFeatureExpr expr) {
		String[] currentFeatures = new String[length];
		for (int i = 0; i < currentFeatures.length; i++) {
			currentFeatures[i] = features[i];
		}
		
		Set<Set<String>> bddSolutions = new BDDSolver(0).getSolutions(expr, currentFeatures);
		
		// TODO fix this
//		Set<Set<String>> satSolutions = new SATSolver(0).getSolutions(expr, "test", currentFeatures);
//		assertEquals(bddSolutions, satSolutions);
	}
	
	
	
}
