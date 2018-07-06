package varex;

import de.fosd.typechef.featureexpr.*;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureModel;
import de.fosd.typechef.featureexpr.sat.SATFeatureModel;
import gov.nasa.jpf.JPF;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.immutable.List;
import scala.util.matching.Regex;
import util.SSHOMExprFactory;
import util.SetArithmetic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Main {
  private final static int     RUN_FEATURE  = 0;
  private final static int     GET_2OMS     = 1;
  private final static int     SSHOM        = 2;
  private final static int     SSHOM_STRICT = 3;
  private static       int     mode         = SSHOM;
  private static       boolean SAT          = true;
  private final static int     NUM_MUTANTS  = 26;
  private static       String  fname        = "data/exhaustive-testdata.txt";

  private static FeatureModel featureModel;

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
      sshomSolver(fname);
      break;

    case SSHOM_STRICT:
      strictSSHOMSolver(fname);
      break;
    }
  }

  public static void runFeature(String fname) throws FileNotFoundException {
    try (PrintStream p = new PrintStream(fname)) {
      System.setOut(p);

      String paths = "+classpath="
          + "/home/serena/reuse/hom-generator/out/production/code-ut,"
          + "/home/serena/reuse/hom-generator/out/test/code-ut,"
          + "/home/serena/reuse/hom-generator/out/production/varex-hom-finder,"
          + "/home/serena/MiscCS/intellij/lib/junit-4.12.jar";

      JPF.main(new String[] { "+search.class=.search.RandomSearch", paths,
          "testRunner.RunTestsTriangleGenerated" });
    }
  }

  public static void sshomSolver(String fname) {
    Map<String, FeatureExpr> tests = readFile(fname);

    SingleFeatureExpr[] mutants = getEachMutant();
    FeatureExpr[] fomExprs = genFOMs(mutants);

    FeatureExpr finalExpr = SSHOMExprFactory
        .getSSHOMExpr(tests, mutants, NUM_MUTANTS);

    //exclude foms
    for (FeatureExpr m : fomExprs) {
      finalExpr = finalExpr.andNot(m);
    }

    printAssignments(mutants, finalExpr);
  }


  public static void strictSSHOMSolver(String fname) {
    Map<String, FeatureExpr> tests = readFile(fname);

    SingleFeatureExpr[] mutants = getEachMutant();
    FeatureExpr[] fomExprs = genFOMs(mutants);

    FeatureExpr finalExpr = SSHOMExprFactory
        .getStrictSSHOMExpr(tests, mutants, NUM_MUTANTS);

    //exclude foms
    for (FeatureExpr m : fomExprs) {
      finalExpr = finalExpr.andNot(m);
    }

    printAssignments(mutants, finalExpr);
  }

  public static void allSecondOrder(String fname) {
    Map<String, FeatureExpr> tests = readFile(fname);

    FeatureExpr[] mutants = getEachMutant();

    // [ m1&!m2&!m3... , !m1&m2&!m3... , ... ]
    FeatureExpr[] mutantConfigurations = genFOMs(mutants);

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
        mutantConfigurations2OM[i][j] = getMutantExpr(x -> x == fi || x == fj, NUM_MUTANTS, mutants);
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

  private static void printAssignments(FeatureExpr[] mutants,
      FeatureExpr finalExpr) {
    Option<Tuple2<List<SingleFeatureExpr>, List<SingleFeatureExpr>>> satisfiableAssignment;
    scala.collection.mutable.HashSet<FeatureExpr> interestingFeaturesMutable = new scala.collection.mutable.HashSet<>();
    for (FeatureExpr m : mutants) {
      interestingFeaturesMutable.add(m);
    }
    scala.collection.immutable.Set<SingleFeatureExpr> interestingFeatures = interestingFeaturesMutable
        .toSet();

    do {
      satisfiableAssignment = finalExpr.getSatisfiableAssignment(
          featureModel,
          interestingFeatures, true);

      if (satisfiableAssignment.nonEmpty()) {
        System.out.println(parseAssignment(satisfiableAssignment.get()));

        // exclude the new assignment
        FeatureExpr thisExpr = FeatureExprFactory.True();
        for (FeatureExpr e : JavaConversions.asJavaCollection(satisfiableAssignment.get()._1)) {
          thisExpr = thisExpr.and(e);
        }

        for (FeatureExpr e : JavaConversions.asJavaCollection(satisfiableAssignment.get()._2)) {
          thisExpr = thisExpr.andNot(e);
        }
        finalExpr = finalExpr.andNot(thisExpr);
      }
    } while(satisfiableAssignment.nonEmpty());
  }

  private static StringBuffer parseAssignment(
      Tuple2<List<SingleFeatureExpr>, List<SingleFeatureExpr>> satisfiableAssignment) {
    Pattern p = Pattern.compile("m[0-9]+");
    Matcher matcher = p.matcher(satisfiableAssignment._1.toString());
    StringBuffer sb = new StringBuffer();
    while(matcher.find()) {
      sb.append(matcher.group());
      sb.append(" ");
    }
    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    } else {
      sb.append("<empty assignment>");
    }
    return sb;
  }

  // [ m1, m2, m3 ... ]
  private static SingleFeatureExpr[] getEachMutant() {
    SingleFeatureExpr[] mutants = new SingleFeatureExpr[NUM_MUTANTS];
    for (int i = 0; i < NUM_MUTANTS; i++) {
      SingleFeatureExpr mutant = FeatureExprFactory.createDefinedExternal("m" + i);
      mutants[i] = mutant;
    }
    return mutants;
  }

  private static FeatureExpr getMutantExpr(Predicate<Integer> mutantEnabled, int numMutants, FeatureExpr[] mutants) {
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

  private static Map<String, FeatureExpr> readFile(String fname) {
    Map<String, FeatureExpr> tests = new HashMap<>();
    try (Scanner in = new Scanner(new File(fname))) {
      while (in.hasNextLine()) {
        String l = in.nextLine();
        if (l.startsWith("{test")) {
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

  private static FeatureExpr[] genFOMs(FeatureExpr[] individualMutants) {
    FeatureExpr[] mutantConfigurations = new FeatureExpr[NUM_MUTANTS];
    IntStream
        .range(0, NUM_MUTANTS)
        .forEach(x ->
            mutantConfigurations[x] = getMutantExpr(y -> y == x, NUM_MUTANTS, individualMutants));
    return mutantConfigurations;
  }

}
