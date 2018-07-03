package util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

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

  public static FeatureExpr getSSHOMExpr(Map<String, FeatureExpr> tests,
      FeatureExpr[] mutants, FeatureExpr[] mutantConfigurations,
      int numMutants) {
    FeatureExpr gt0 = FeatureExprFactory.False();
    for (FeatureExpr t : tests.values()) {
      gt0 = gt0.or(t);
    }

    FeatureExpr lte1 = FeatureExprFactory.True();
    for (Map.Entry<String, FeatureExpr> testEntry : tests.entrySet()) {
      String testName = testEntry.getKey();
      FeatureExpr testExpr = testEntry.getValue();
      FeatureExpr implication = FeatureExprFactory.True();
      for (int i = 0; i < numMutants; i++) {

        FomTestKey currKey = new FomTestKey(mutants[i].toString(), testName);

        FeatureExpr fomIsSat;
        if (lambda.containsKey(currKey)) {
          fomIsSat = lambda.get(currKey);
        } else {
          fomIsSat = fomKillsTest(mutantConfigurations[i], testExpr);
          lambda.put(currKey, fomIsSat);
        }

        implication = implication.and(fomIsSat.orNot(mutants[i]));
      }
      lte1 = lte1.and(testExpr.implies(implication));
    }

    return gt0.and(lte1);
  }

  public static FeatureExpr getStrictSSHOMExpr(Map<String, FeatureExpr> tests,
      FeatureExpr[] mutants, FeatureExpr[] mutantConfigurations,
      int numMutants) {

    FeatureExpr finalExpr = getSSHOMExpr(tests, mutants, mutantConfigurations,
        numMutants);
    FeatureExpr lt1 = FeatureExprFactory.False();

    System.out.println("lt1 " + System.currentTimeMillis());

    for (Map.Entry<String, FeatureExpr> testEntry : tests.entrySet()) {
      String testName = testEntry.getKey();
      FeatureExpr testExpr = testEntry.getValue();
      FeatureExpr killsMutant = FeatureExprFactory.True();
      for (int i = 0; i < numMutants; i++) {
        FomTestKey currKey = new FomTestKey(mutants[i].toString(), testName);

        FeatureExpr fomIsSat;
        if (lambda.containsKey(currKey)) {
          fomIsSat = lambda.get(currKey);
        } else {
          fomIsSat = fomKillsTest(mutantConfigurations[i], testExpr);
          lambda.put(currKey, fomIsSat);
        }
        killsMutant = killsMutant.and(fomIsSat.orNot(mutants[i]));
      }
      lt1 = lt1.or(killsMutant.andNot(testExpr));
    }

    finalExpr = finalExpr.and(lt1);
    return finalExpr;
  }

  private static FeatureExpr fomKillsTest(FeatureExpr mConfig,
      FeatureExpr test) {
    return test.and(mConfig).isSatisfiable() ?
        FeatureExprFactory.True() :
        FeatureExprFactory.False();
  }

}
