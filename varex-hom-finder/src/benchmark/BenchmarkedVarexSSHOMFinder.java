package benchmark;

import cmu.conditional.Conditional;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.FeatureModel;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.vm.JPF_gov_nasa_jpf_ConsoleOutputStream;
import scala.Tuple2;
import scala.collection.immutable.List;
import testRunner.RunTests;
import varex.SSHOMExprFactory;
import util.SSHOMRunner;
import varex.SatisfiableAssignmentIterator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
      String baseDir = "/home/serena/reuse/hom-generator/";
      paths = "+classpath="
          + baseDir + "out/production/code-ut,"
          + baseDir + "code-ut/jars/monopoli100.jar,"
          + baseDir + "code-ut/jars/commons-validator.jar,"
          + baseDir + "lib/bcel-6.0.jar,"
          + baseDir + "out/test/code-ut,"
          + baseDir + "out/production/varex-hom-finder,"
          + "/home/serena/MiscCS/intellij/lib/junit-4.12.jar";
    } else {
      paths = "+classpath=" + "/home/feature/serena/varex-hom-finder.jar,"
          + "/home/feature/serena/junit-4.12.jar";
    }
    JPF.main(new String[] { "+search.class=.search.RandomSearch", /*"+featuremodel=featuremodel.dimacs",*/ "+choice=MapChoice", paths,
         TestRunner.class.getName()});

    Map<String, FeatureExpr> tests = JPF_gov_nasa_jpf_ConsoleOutputStream.testExpressions;

    SingleFeatureExpr[] mutantExprs = mutantNamesToFeatures(mutants);
    FeatureExpr[] fomExprs = SSHOMExprFactory.genFOMs(mutantExprs, mutants.length);

    FeatureExpr finalExpr = SSHOMExprFactory
        .getSSHOMExpr(tests, mutantExprs, mutantExprs.length);

    //exclude foms
    for (FeatureExpr m : fomExprs) {
      finalExpr = finalExpr.andNot(m);
    }

    FeatureModel fm = FeatureExprFactory.bdd()
        .featureModelFactory().createFromDimacsFile("featuremodel.dimacs");
    SatisfiableAssignmentIterator iterator = new SatisfiableAssignmentIterator(
        mutantExprs, finalExpr, fm);

    while (iterator.hasNext()) {
      Tuple2<List<SingleFeatureExpr>, List<SingleFeatureExpr>> next = iterator
          .next();
      benchmarker.timestamp(SSHOMExprFactory.parseAssignment(next).toString());
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
