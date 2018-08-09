package varex;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.immutable.List;
import scala.collection.mutable.HashSet;
import scala.collection.mutable.Set;

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

  public static void printAssignments(FeatureExpr[] mutants,
      FeatureExpr finalExpr) {
    SatisfiableAssignmentIterator iterator = new SatisfiableAssignmentIterator(
        mutants, finalExpr);

    while (iterator.hasNext()) {
      System.out.println(parseAssignment(iterator.next()));
    }
  }

  public static StringBuffer parseAssignment(
      Tuple2<List<SingleFeatureExpr>, List<SingleFeatureExpr>> satisfiableAssignment) {
    StringBuffer sb = new StringBuffer();
    for (SingleFeatureExpr e : JavaConversions.asJavaIterable(satisfiableAssignment._1)) {
    	String name = e.feature();
		try {
			if (e.feature().startsWith("CONFIG_")) {
				name = e.feature().substring("CONFIG_".length());
			}
		} catch (Exception ex) {
			System.out.println(e.feature());
			throw ex;
		}
      sb.append(name);
      sb.append(",");
    }
    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    } else {
      sb.append("<empty assignment>");
    }
    return sb;
  }

  public static FeatureExpr getMutantExpr(Predicate<Integer> mutantEnabled,
      int numMutants, FeatureExpr[] mutants) {
    FeatureExpr mutantConfig = FeatureExprFactory.True();
    for (int i = 0; i < numMutants; i++) {
      if (mutantEnabled.test(i)) {
        mutantConfig = mutantConfig.and(mutants[i]);
      } else {
        mutantConfig = mutantConfig.andNot(mutants[i]);
      }
    }
    return mutantConfig;
  }

  public static FeatureExpr[] genFOMs(FeatureExpr[] individualMutants, int numMutants) {
    FeatureExpr[] mutantConfigurations = new FeatureExpr[numMutants];
    IntStream
        .range(0, numMutants)
        .forEach(x ->
            mutantConfigurations[x] = getMutantExpr(y -> y == x, numMutants, individualMutants));
    return mutantConfigurations;
  }


  private static boolean fomKillsTest(String m,
      FeatureExpr test) {
    Set<String> fom = new HashSet<>();
    fom.add(m);
    return test.evaluate(fom.toSet());
  }

}
