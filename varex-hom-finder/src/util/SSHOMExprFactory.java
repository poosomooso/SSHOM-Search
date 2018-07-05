package util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import scala.collection.mutable.HashSet;
import scala.collection.mutable.Set;

import java.util.HashMap;
import java.util.Map;

public class SSHOMExprFactory {
  private static Map<FomTestKey, FeatureExpr> lambda = new HashMap<>();

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

        FeatureExpr fomIsSat;
        if (lambda.containsKey(currKey)) {
          fomIsSat = lambda.get(currKey);
        } else {
          fomIsSat = fomKillsTest(mutants[i].feature(), testExpr);
          lambda.put(currKey, fomIsSat);
        }

        implication = implication.and(fomIsSat.orNot(mutants[i]));
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

        FeatureExpr fomIsSat;
        if (lambda.containsKey(currKey)) {
          fomIsSat = lambda.get(currKey);
        } else {
          fomIsSat = fomKillsTest(mutants[i].feature(), testExpr);
          lambda.put(currKey, fomIsSat);
        }
        killsMutant = killsMutant.and(fomIsSat.orNot(mutants[i]));
      }
      lt1 = lt1.or(killsMutant.andNot(testExpr));
    }

    finalExpr = finalExpr.and(lt1);
    return finalExpr;
  }

  private static FeatureExpr fomKillsTest(String m,
      FeatureExpr test) {
    Set<String> fom = new HashSet<>();
    fom.add(m);
    return test.evaluate(fom.toSet()) ?
            FeatureExprFactory.True() :
            FeatureExprFactory.False();
  }

}
