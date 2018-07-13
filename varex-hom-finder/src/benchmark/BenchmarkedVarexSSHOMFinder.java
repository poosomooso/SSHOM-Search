package benchmark;

import cmu.conditional.Conditional;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.vm.JPF_gov_nasa_jpf_ConsoleOutputStream;
import testRunner.RunTests;
import varex.SSHOMExprFactory;
import util.SSHOMRunner;
import varex.SatisfiableAssignmentIterator;

import java.util.Map;

public class BenchmarkedVarexSSHOMFinder {
  private Benchmarker benchmarker;
  private SSHOMRunner runner;

  public static class TestRunner {
    public static void main(String[] args) {
      RunTests.runTests(RunBenchmarks.TEST_CLASSES);
    }
  }

  public BenchmarkedVarexSSHOMFinder() {
    benchmarker = new Benchmarker();
  }

  public void varexSSHOMFinder(Class[] targetClasses, Class[] testClasses)
      throws NoSuchFieldException, IllegalAccessException {
    benchmarker.start();
    runner = new SSHOMRunner(targetClasses, testClasses);
    String[] mutants = runner.getMutants().toArray(new String[0]);
    String paths;
    if (RunBenchmarks.RUNNING_LOCALLY) {
      paths = "+classpath="
          + "/home/serena/reuse/hom-generator/out/production/code-ut,"
          + "/home/serena/reuse/hom-generator/out/test/code-ut,"
          + "/home/serena/reuse/hom-generator/out/production/varex-hom-finder,"
          + "/home/serena/MiscCS/intellij/lib/junit-4.12.jar";
    } else {

      paths = "+classpath=" + "/home/feature/serena/varex-hom-finder.jar,"
          + "/home/feature/serena/junit-4.12.jar";
    }

    JPF.main(new String[] { "+search.class=.search.RandomSearch", paths,
        TestRunner.class.getName() });

    Map<String, FeatureExpr> tests = JPF_gov_nasa_jpf_ConsoleOutputStream.testExpressions;

    SingleFeatureExpr[] mutantExprs = mutantNamesToFeatures(mutants);
    FeatureExpr[] fomExprs = SSHOMExprFactory
        .genFOMs(mutantExprs, mutants.length);

    FeatureExpr finalExpr = SSHOMExprFactory
        .getSSHOMExpr(tests, mutantExprs, mutantExprs.length);

    //exclude foms
    for (FeatureExpr m : fomExprs) {
      finalExpr = finalExpr.andNot(m);
    }

    SatisfiableAssignmentIterator iterator = new SatisfiableAssignmentIterator(
        mutantExprs, finalExpr);
    while (iterator.hasNext()) {
      benchmarker.timestamp(
          SSHOMExprFactory.parseAssignment(iterator.next()).toString());
    }
  }

  private SingleFeatureExpr[] mutantNamesToFeatures(String[] mutants) {
    SingleFeatureExpr[] mutantExprs = new SingleFeatureExpr[mutants.length];
    for (int i = 0; i < mutants.length; i++) {
      mutantExprs[i] = Conditional.createFeature(mutants[i]);
    }
    return mutantExprs;
  }
}
