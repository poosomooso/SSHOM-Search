package util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import scala.Tuple2;
import scala.collection.mutable.HashSet;
import scala.collection.mutable.Set;

import java.util.HashMap;
import java.util.Map;

public class SSHOMExprFactory {
  private static Map<FomTestKey, Boolean> lambda = new HashMap<>();

  private static class FomTestKey {
    private String mutant;
    private String test;

    public FomTestKey(String m, String t) {
      this.mutant = m;
      this.test = t;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      FomTestKey that = (FomTestKey) o;

      if (mutant != null ? !mutant.equals(that.mutant) : that.mutant != null)
        return false;
      return test != null ? test.equals(that.test) : that.test == null;
    }

    @Override
    public int hashCode() {
      int result = mutant != null ? mutant.hashCode() : 0;
      result = 31 * result + (test != null ? test.hashCode() : 0);
      return result;
    }
  }

  /**
   *
   * @param tests Map of {"testName" : /expression for when test is killed/}
   * @param mutants Array of [m1, m2, ... ]
   * @param numMutants
   * @return
   */
  public static FeatureExpr getSSHOMExpr(Map<String, FeatureExpr> tests,
      SingleFeatureExpr[] mutants, int numMutants) {

    // check that at least one test fails
    FeatureExpr gt0 = FeatureExprFactory.False();
    for (FeatureExpr t : tests.values()) {
      gt0 = gt0.or(t);
    }

    // for each test, if it fails for the hom, then it must fail for each fom
    FeatureExpr lte1 = FeatureExprFactory.True();
    for (Map.Entry<String, FeatureExpr> testEntry : tests.entrySet()) {
      String testName = testEntry.getKey();
      FeatureExpr testExpr = testEntry.getValue();
      FeatureExpr implication = FeatureExprFactory.True();
      for (int i = 0; i < numMutants; i++) {

        FomTestKey currKey = new FomTestKey(mutants[i].feature(), testName);

        boolean fomKillsTest;
        if (lambda.containsKey(currKey)) {
          fomKillsTest = lambda.get(currKey);
        } else {
          fomKillsTest = fomKillsTest(mutants[i].feature(), testExpr);
          lambda.put(currKey, fomKillsTest);
        }

        if (!fomKillsTest) {
          implication = implication.and(mutants[i].not());
        }

      }
      lte1 = lte1.and(testExpr.implies(implication));
    }

    return gt0.and(lte1);
  }

  public static FeatureExpr getStrictSSHOMExpr(Map<String, FeatureExpr> tests,
      SingleFeatureExpr[] mutants, int numMutants) {

    FeatureExpr finalExpr = getSSHOMExpr(tests, mutants, numMutants);
    FeatureExpr lt1 = FeatureExprFactory.False();

    // exists a test that fails all foms but does not fail corresponding hom
    for (Map.Entry<String, FeatureExpr> testEntry : tests.entrySet()) {
      String testName = testEntry.getKey();
      FeatureExpr testExpr = testEntry.getValue();
      FeatureExpr killsMutant = FeatureExprFactory.True();

      for (int i = 0; i < numMutants; i++) {
        FomTestKey currKey = new FomTestKey(mutants[i].feature(), testName);

        boolean fomKillsTest;
        if (lambda.containsKey(currKey)) {
          fomKillsTest = lambda.get(currKey);
        } else {
          fomKillsTest = fomKillsTest(mutants[i].feature(), testExpr);
          lambda.put(currKey, fomKillsTest);
        }
        if (!fomKillsTest) {
          killsMutant = killsMutant.and(mutants[i].not());
        }
      }
      lt1 = lt1.or(killsMutant.andNot(testExpr));
    }

    finalExpr = finalExpr.and(lt1);
    return finalExpr;
  }

  /**
   * Currently unused; was meant to abstract the tests as variables
   * (caused out of memory errors; probably buggy)
   * @param tests
   * @return
   */
  private static Tuple2<Map<String, FeatureExpr>, FeatureExpr> testExprToVars(
      Map<String, FeatureExpr> tests) {
    Map<String, FeatureExpr> testVars = new HashMap<>();
    FeatureExpr equivClause = FeatureExprFactory.True();
    for (Map.Entry<String, FeatureExpr> e : tests.entrySet()) {
      String name = e.getKey();
      FeatureExpr expr = e.getValue();
      SingleFeatureExpr var = FeatureExprFactory.createDefinedExternal(name);
      FeatureExpr equiv = var.equiv(expr);
      equivClause = equivClause.and(equiv);
      testVars.put(name, var);
    }
    return new Tuple2<>(testVars, equivClause);
  }

  private static boolean fomKillsTest(String m,
      FeatureExpr test) {
    Set<String> fom = new HashSet<>();
    fom.add(m);
    return test.evaluate(fom.toSet());
  }

}
