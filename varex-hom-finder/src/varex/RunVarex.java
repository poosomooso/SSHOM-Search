package varex;

import cmu.conditional.Conditional;
import de.fosd.typechef.featureexpr.*;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureModel;
import de.fosd.typechef.featureexpr.sat.SATFeatureModel;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.vm.JPF_gov_nasa_jpf_ConsoleOutputStream;
import scala.Option;
import scala.util.matching.Regex;
import util.SetArithmetic;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RunVarex {
  private final static int     RUN_FEATURE  = 0;
  private final static int     GET_2OMS     = 1;
  private final static int     SSHOM        = 2;
  private final static int     SSHOM_STRICT = 3;
  private static       int     mode         = SSHOM;
  private static       boolean SAT          = false;
  private final static int     NUM_MUTANTS  = 33;
  private static       String  fname        = "data/automutants/testdata.txt";

  static FeatureModel featureModel;

  static {
    if (SAT) {
      FeatureExprFactory.setDefault(FeatureExprFactory.sat());
      featureModel = SATFeatureModel.create(FeatureExprFactory.True());
    } else {
      FeatureExprFactory.setDefault(FeatureExprFactory.bdd());
      featureModel = BDDFeatureModel.create(FeatureExprFactory.True());
    }
  }
  public static void main(String[] args) throws FileNotFoundException {

    switch (mode) {
    case RUN_FEATURE:
      runFeature(fname);
      break;
    case GET_2OMS:
      allSecondOrder(fname);
      break;
    case SSHOM:
      runFeature(fname);
      sshomSolver(fname);
      break;

    case SSHOM_STRICT:
      strictSSHOMSolver(fname);
      break;
    }
  }

  public static void runFeature(String fname) throws FileNotFoundException {
    long t0 = System.currentTimeMillis();

    String paths = "+classpath="
        + "/home/serena/reuse/hom-generator/out/production/code-ut,"
        + "/home/serena/reuse/hom-generator/out/test/code-ut,"
        + "/home/serena/reuse/hom-generator/out/production/varex-hom-finder,"
        + "/home/serena/MiscCS/intellij/lib/junit-4.12.jar";

    JPF.main(new String[] { "+search.class=.search.RandomSearch", paths,
        "testRunner.RunTestsTriangleImproved" });

    System.out.printf("%,d ms\n", System.currentTimeMillis() - t0);
  }

  public static void sshomSolver(String fname) {
    long t0 = System.currentTimeMillis();
    Map<String, FeatureExpr> tests = JPF_gov_nasa_jpf_ConsoleOutputStream.testExpressions;

    SingleFeatureExpr[] mutants = getEachMutant();
    FeatureExpr[] fomExprs = SSHOMExprFactory.genFOMs(mutants, NUM_MUTANTS);

    FeatureExpr finalExpr = SSHOMExprFactory
        .getSSHOMExpr(tests, mutants, NUM_MUTANTS);

    //exclude foms
    for (FeatureExpr m : fomExprs) {
      finalExpr = finalExpr.andNot(m);
    }

    SSHOMExprFactory.printAssignments(mutants, finalExpr);

    System.out.printf("%,d ms\n", System.currentTimeMillis() - t0);
  }


  public static void strictSSHOMSolver(String fname) {
    long t0 = System.currentTimeMillis();
    Map<String, FeatureExpr> tests = readFile(fname);

    SingleFeatureExpr[] mutants = getEachMutant();
    FeatureExpr[] fomExprs = SSHOMExprFactory.genFOMs(mutants, NUM_MUTANTS);

    FeatureExpr finalExpr = SSHOMExprFactory
        .getStrictSSHOMExpr(tests, mutants, NUM_MUTANTS);

    //exclude foms
    for (FeatureExpr m : fomExprs) {
      finalExpr = finalExpr.andNot(m);
    }

    SSHOMExprFactory.printAssignments(mutants, finalExpr);
    System.out.printf("%,d ms\n", System.currentTimeMillis() - t0);
  }

  public static void allSecondOrder(String fname) {
    Map<String, FeatureExpr> tests = readFile(fname);

    FeatureExpr[] mutants = getEachMutant();

    // [ m1&!m2&!m3... , !m1&m2&!m3... , ... ]
    FeatureExpr[] mutantConfigurations = SSHOMExprFactory.genFOMs(mutants,
        NUM_MUTANTS);

    // [ {test1, test2, test4...}, {test2, test3, test4...}, ... ]
    Set<String>[] allFailedTestsFOM = new Set[NUM_MUTANTS];
    for (int i = 0; i < NUM_MUTANTS; i++) {
      Set<String> testsFailed = new HashSet<>();
      for (Map.Entry<String, FeatureExpr> entry : tests.entrySet()) {
        String testName = entry.getKey();
        FeatureExpr testExpr = entry.getValue();
        if (testExpr.and(mutantConfigurations[i]).isSatisfiable()) {
          testsFailed.add(testName);
        }
      }
      allFailedTestsFOM[i] = testsFailed;
    }

    // [m1&m2&!m3... , m1&!m2&m3... , ... ]
    FeatureExpr[][] mutantConfigurations2OM = new FeatureExpr[NUM_MUTANTS][NUM_MUTANTS];
    for (int i = 0; i < NUM_MUTANTS; i++) {
      for (int j = i+1; j < NUM_MUTANTS; j++) {
        final int fi = i, fj = j;
        mutantConfigurations2OM[i][j] = SSHOMExprFactory
            .getMutantExpr(x -> x == fi || x == fj, NUM_MUTANTS, mutants);
      }
    }

    // [ {test1, test2, test4...}, {test2, test3, test4...}, ... ]
    Set<String>[][] allFailedTests2OM = new Set[NUM_MUTANTS][NUM_MUTANTS];

    for (int i = 0; i < NUM_MUTANTS; i++) {
      for (int j = i+1; j < NUM_MUTANTS; j++) {
        Set<String> testsFailed = new HashSet<>();
        for (Map.Entry<String, FeatureExpr> entry : tests.entrySet()) {
          String testName = entry.getKey();
          FeatureExpr testExpr = entry.getValue();
          if (testExpr.and(mutantConfigurations2OM[i][j]).isSatisfiable()) {
            testsFailed.add(testName);
          }
        }
        allFailedTests2OM[i][j] = testsFailed;
      }
    }

    int numSSHOM = 0, numInOverlap = 0, numOthers = 0;
    for (int i = 0; i < NUM_MUTANTS; i++) {
      for (int j = i+1; j < NUM_MUTANTS; j++) {
        System.out.println("--------------------------------------------------------------------");
        Set<String> homFailedTests = allFailedTests2OM[i][j];
        Set<String> fomOverlap = SetArithmetic.getIntersection(allFailedTestsFOM[i],
            allFailedTestsFOM[j]);
        Set<String> allFomTests = SetArithmetic.getUnion(allFailedTestsFOM[i],
            allFailedTestsFOM[j]);

        if (fomOverlap.containsAll(homFailedTests)) {
          //is sshom
          numSSHOM++;
          System.out.println("SSHOM m" + i + " m" + j);
          System.out.println("HOM: " + homFailedTests.size());
          System.out.println("FOM1 & FOM2 & !HOM: " + SetArithmetic.getDifferenceSize(fomOverlap, homFailedTests));
          System.out.println("(FOM1 | FOM2) & !(FOM1 & FOM2): " + SetArithmetic.getDifferenceSize(allFomTests, fomOverlap));
        } else if (allFomTests.containsAll(homFailedTests)) {
          numInOverlap++;
          System.out.println("HOM within union m" + i + " m" + j);
          // print hom tests that are in the intersection
          System.out.println("HOM & FOM1 & FOM2: " + SetArithmetic.getIntersectionSize(homFailedTests, fomOverlap));
          // print hom tests that are not in the intersection
          System.out.println("HOM & (FOM1 | FOM2) & !(FOM1 & FOM2): " + SetArithmetic.getDifferenceSize(homFailedTests, fomOverlap));
          // print remaining fom tests that are in the intersection
          System.out.println("FOM1 & FOM2 & !HOM: " + SetArithmetic.getDifferenceSize(fomOverlap, homFailedTests));
          // print remaining fom tests that are not in the intersection
          System.out.println("(FOM1 | FOM2) & !HOM & !(FOM1 & FOM2): " + SetArithmetic.getDifferenceSize(allFomTests, homFailedTests, fomOverlap));
        } else {
          numOthers++;
          // hom has some tests that the allSecondOrder don't cover; might be interesting?
          System.out.println("SOMETHING ELSE m" + i + " m" + j);
          // print hom tests in the intersection
          System.out.println("HOM & FOM1 & FOM2: " + SetArithmetic.getIntersectionSize(homFailedTests, fomOverlap));
          // print remaining hom tests that are in the union
          System.out.println("HOM & (FOM1 | FOM2) & !(FOM1 & FOM2): " + (
              SetArithmetic.getIntersectionSize(homFailedTests, allFomTests)
                  - SetArithmetic.getIntersectionSize(homFailedTests, fomOverlap)));
          // print remaining hom tests that are outside the union
          System.out.println("HOM & !(FOM1 | FOM2): " + SetArithmetic.getDifferenceSize(homFailedTests, allFomTests));
          // print remaining fom tests in the intersection
          System.out.println("FOM1 & FOM2 & !HOM: " + SetArithmetic.getDifferenceSize(fomOverlap, homFailedTests));
          // print remaining fom tests
          System.out.println("(FOM1 | FOM2) & !HOM & ! (FOM1 & FOM2): " + SetArithmetic.getDifferenceSize(allFomTests, homFailedTests, fomOverlap));
        }
      }
    }
    System.out.println("Number of subsuming higher order mutants: " + numSSHOM);
    System.out.println("Number of HOMs that overlap with the union of FOMs: " + numInOverlap);
    System.out.println("Number of other HOMs: " + numOthers);
  }

  // helper methods

  // [ m1, m2, m3 ... ]
  private static SingleFeatureExpr[] getEachMutant() {
    SingleFeatureExpr[] mutants = new SingleFeatureExpr[NUM_MUTANTS];
    for (int i = 0; i < NUM_MUTANTS; i++) {
      SingleFeatureExpr mutant = Conditional.createFeature("m" + i);
      mutants[i] = mutant;
    }
    return mutants;
  }

  private static Map<String, FeatureExpr> readFile(String fname) {
    Map<String, FeatureExpr> tests = new HashMap<>();
    try (Scanner in = new Scanner(new File(fname))) {
      while (in.hasNextLine()) {
        String l = in.nextLine();
        if (l.startsWith("{")) {
          String[] split = l.split(" : ");
          FeatureExpr testFeatureExpr = new FeatureExprParser(FeatureExprFactory.dflt(),
              new Regex("[A-Za-z0-9_]*", null), Option.empty()).parse(split[1]);
          tests.put(split[0], testFeatureExpr);
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(-1);
    }
    return tests;
  }

}
