package solver.sat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import varex.SATSSHOMExprFactory;

public class SAT4JTest {
	private final static String[] features = new String[] { "A", "B", "C" };
	private final static FeatureExpr[] f = new FeatureExpr[features.length];

	static {
		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());
		for (int i = 0; i < f.length; i++) {
			f[i] = FeatureExprFactory.createDefinedExternal(features[i]);
		}
	}

	@Test
	public void test() throws Exception {
//		BDDFeatureExpr expr = (BDDFeatureExpr) f[0].or(f[1]).or(f[2]);
//		String fileName = "expr";
//		File dimacsFile = DimacsWriter.instance.bddToDimacsTseytinTransformation(expr, fileName);

		ISolver solver = new BoundedModelIterator(SolverFactory.newDefault(), 128);
		DimacsReader reader = new DimacsReader(solver);
		try {
			reader.parseInstance("fullModel.dimacs");
		} catch (ParseFormatException | IOException | ContradictionException e) {
			e.printStackTrace();
		}
		Set<List<Integer>> solutions = new HashSet<>();
		
		int i = 1;
		while (solver.isSatisfiable()) {
			int[] model = solver.model();
			List<Integer> list = new ArrayList<>(model.length); 
			for (int j = 0; j < model.length; j++) {
				list.add(model[j]);
			}
			
			if (solutions.contains(list)) {
				System.out.println("contains");
			} else {
				solutions.add(list);
			}
			
			
			System.out.print(i + " ");
			for (int j = 0; j < 6; j++) {
				String fname = j + "";
				if (model[j] < 0) {
					System.out.print('-');
				} else {
					System.out.print(' ');
				}
				System.out.print(fname + " ");
			}
			System.out.println();
			i++;
		}

	}
	
	@Test
	public void method() throws TimeoutException {
		Map<String, FeatureExpr> tests = new HashMap<>();
		SingleFeatureExpr f1 = FeatureExprFactory.createDefinedExternal("F1");
		SingleFeatureExpr f2 = FeatureExprFactory.createDefinedExternal("F2");
		
		tests.put("T1", f1);
		tests.put("T2", f2);
		
		List<File> expression1Files = new ArrayList<>(tests.size());
		for (Map.Entry<String, FeatureExpr> testEntry : tests.entrySet()) {
			if (!testEntry.getValue().isContradiction()) {
				String testName = testEntry.getKey();
				File file = new File("or_" + testName + ".dimacs");
				DimacsWriter.instance.bddToDimacsTseytinTransformation((BDDFeatureExpr) testEntry.getValue(), "or_" + testName);
				expression1Files.add(file);
			}
		}
		
		SingleFeatureExpr[] mutants = new SingleFeatureExpr[]{f1, f2};
		File orFile = SATSSHOMExprFactory.orDimacsFiles(expression1Files, mutants, "EXPRESION");
		
		ISolver solver = new BoundedModelIterator(SolverFactory.newDefault(), 2);			
		DimacsReader reader = new DimacsReader(solver);
		try {
			reader.parseInstance(orFile.getName());
		} catch (ParseFormatException | IOException | ContradictionException e) {
			e.printStackTrace();
		}
		
		while (solver.isSatisfiable()) {
			int[] model = solver.model();
			System.out.println(Arrays.toString(model));
		}
	}
}
