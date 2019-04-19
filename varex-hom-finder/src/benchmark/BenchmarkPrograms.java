package benchmark;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import benchmark.heuristics.FirstOrderMutant;
import mutated.triangleAll.Triangle;
import mutated.triangleAll.Triangle_ESTest_improved;

public class BenchmarkPrograms {
  public enum Program {
    TRIANGLE, MONOPOLY, VALIDATOR, CLI, CHESS, MATH
  }
  public static Program PROGRAM = Program.CHESS;
  private static final String PATH_TO_RESOURCE = "out/production/varex-hom-finder/";
  private static Class<?>[] targetClasses;
  private static Class<?>[] testClasses;
  private static Map<String, Integer> makeshiftFeatureModel;
  private static String[] mutantNames;

  public static Class<?>[] getTargetClasses() {
    if (targetClasses == null) {
      if (PROGRAM == Program.TRIANGLE) {
        targetClasses = new Class[] { Triangle.class };
      } else {
        targetClasses = loadClasses(getSrcResource(), getSrcDir(), getSrcPackage());
      }
      System.out.println("TARGET CLASSES : " + Arrays.toString(targetClasses));
    }
    return targetClasses;
  }

  public static Class<?>[] getTestClasses() {
    if (testClasses == null) {
      if (PROGRAM == Program.TRIANGLE) {
        testClasses = new Class[] { Triangle_ESTest_improved.class };
      } else {
        testClasses = loadClasses(getTestResource(), getTestDir(), getTestPackage());
      }
      System.out.println("TEST CLASSES : " + Arrays.toString(testClasses));
    }
    return testClasses;
  }

  public static boolean programHasInfLoops() {
    switch (PROGRAM) {
    case TRIANGLE: return false;
    default: return true;
    }
  }

  /**
   * Returns a map of {mutantName : mutantGroupID} where mutants that conflict
   * with each other (m0 ? a+b : m1 ? a*b : ...) share an id.
   * @return
   */
  public static Map<String, Integer> getMakeshiftFeatureModel() {
    if (makeshiftFeatureModel == null) {
      loadMutantsAndFM();
    }
    return makeshiftFeatureModel;
  }

  public static String[] getMutantNames() {
    if (mutantNames == null) {
      loadMutantsAndFM();
    }
    return mutantNames;
  }

	private static void loadMutantsAndFM() {
		try (Scanner in = new Scanner(RunBenchmarks.class.getClassLoader().getResourceAsStream(getFeatureModelResource()))) {
			makeshiftFeatureModel = new HashMap<>();
			ArrayList<String> mutantList = new ArrayList<>();
			int id = 0;
			while (in.hasNextLine()) {
				String[] mutants = in.nextLine().split(" ");
				for (String m : mutants) {
					makeshiftFeatureModel.put(m, id);
					mutantList.add(m);
				}
				id++;
			}
			mutantNames = mutantList.toArray(new String[0]);
		}
	}

  public static boolean homIsValid(Collection<String> hom) {
    int numMutantGroups = BenchmarkPrograms.getMakeshiftFeatureModel().size();
    boolean[] check = new boolean[numMutantGroups];
    for (String mutation : hom) {
      if (!check(check, mutation)) {
        return false;
      }
    }
    return true;
  }
  
	public static boolean homIsValidFOM(Collection<FirstOrderMutant> hom) {
		int numMutantGroups = BenchmarkPrograms.getMakeshiftFeatureModel().size();
		boolean[] check = new boolean[numMutantGroups];
		for (FirstOrderMutant mutation : hom) {
			if (!check(check, mutation.getMutant())) {
				return false;
			}
		}
		return true;
	}

  public static boolean homIsValid(String... hom) {
    return homIsValid(Arrays.asList(hom));
  }

  private static boolean check(boolean[] check, String mutant) {
    int id = BenchmarkPrograms.getMakeshiftFeatureModel().get(mutant);
    if (check[id]) {
      return false;
    } else {
      check[id] = true;
    }
    return true;
  }

  private static Class<?>[] loadClasses(String resourceStr, String classDir, String classPackage) {
    ArrayList<Class<?>> classes = new ArrayList<>();
    URL u = RunBenchmarks.class.getClassLoader()
        .getResource(resourceStr);

    try {
      if (u == null) {
        PrintWriter printWriter = new PrintWriter(PATH_TO_RESOURCE + resourceStr);
        Class<?>[] targetClasses = BenchmarkPrograms
            .getAllJavaFilesInDir(classDir, classPackage)
            .toArray(new Class[0]);
        for (Class<?> c : targetClasses) {
          System.out.println(c.getName());
          printWriter.println(c.getName());
        }
        printWriter.flush();
        printWriter.close();
        System.out.println(PATH_TO_RESOURCE + resourceStr);
        System.out.println("printwriter flushed and closed");
        System.exit(0); // this is the only reliable way to make sure the file is written to?

        return targetClasses;

      } else {
        try (Scanner in = new Scanner(RunBenchmarks.class.getClassLoader().getResourceAsStream(resourceStr))) {
	        while (in.hasNextLine()) {
	          classes.add(Class.forName(in.nextLine()));
	        }
        }
        return classes.toArray(new Class[0]);
      }
    } catch (ClassNotFoundException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  // abs paths because these should only be needed once
  private static String localMonopolyTestDir     = "/home/serena/reuse/mutated-monopoly/src/edu/uclm/esi/iso5/juegos/monopoly/dominio/tests";
  private static String localMonopolyDir         = "/home/serena/reuse/mutated-monopoly/src/edu/uclm/esi/iso5/juegos/monopoly/dominio";
  private static String localMonopolyPackage     = "edu.uclm.esi.iso5.juegos.monopoly.dominio";
  private static String localMonopolyTestPackage = "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests";

  private static String localTriangleDir         = "/home/serena/reuse/hom-generator/code-ut/src/mutated/triangle";
  private static String localTrianglePackage     = "mutated.triangle";
  private static String localTriangleTestDir     = "/home/serena/reuse/hom-generator/code-ut/test/mutated/triangle/improved";
  private static String localTriangleTestPackage = "mutated.triangle.improved";

  private static String localValidatorDir         = "/home/serena/reuse/mutated-commons-validator/src/main/java/org/apache/commons/validator";
  private static String localValidatorPackage     = "org.apache.commons.validator";
  private static String localValidatorTestDir     = "/home/serena/reuse/mutated-commons-validator/src/test/java/org/apache/commons/validator";
  private static String localValidatorTestPackage = "org.apache.commons.validator";

  private static String localCliDir         = "/home/serena/reuse/mutated-cli/src/org/apache/commons/cli";
  private static String localCliPackage     = "org.apache.commons.cli";
  private static String localCliTestDir     = "/home/serena/reuse/mutated-cli/test/org/apache/commons/cli";
  private static String localCliTestPackage = "org.apache.commons.cli";

  private static String localChessDir     = "/home/serena/reuse/mutated-chess/src/ajedrez/server";
  private static String localChessPackage = "ajedrez.server";

  private static String getSrcDir() {
    switch (PROGRAM) {
    case TRIANGLE:
      return localTriangleDir;
    case MONOPOLY:
      return localMonopolyDir;
    case VALIDATOR:
      return localValidatorDir;
    case CLI:
      return localCliDir;
    case CHESS:
      return localChessDir;
    }
    return "";
  }

  private static String getSrcPackage() {
    switch (PROGRAM) {
    case TRIANGLE:
      return localTrianglePackage;
    case MONOPOLY:
      return localMonopolyPackage;
    case VALIDATOR:
      return localValidatorPackage;
    case CLI:
      return localCliPackage;
    case CHESS:
      return localChessPackage;
    }
    return "";
  }

  private static String getTestDir() {
    switch (PROGRAM) {
    case TRIANGLE:
      return localTriangleTestDir;
    case MONOPOLY:
      return localMonopolyTestDir;
    case VALIDATOR:
      return localValidatorTestDir;
    case CLI:
      return localCliTestDir;
    }
    return "";
  }

  private static String getTestPackage() {
    switch (PROGRAM) {
    case TRIANGLE:
      return localTriangleTestPackage;
    case MONOPOLY:
      return localMonopolyTestPackage;
    case VALIDATOR:
      return localValidatorTestPackage;
    case CLI:
      return localCliTestPackage;
    }
    return "";
  }

  private static String getSrcResource() {
    switch (PROGRAM) {
    case TRIANGLE:
      return "classes/triangle-src.txt";
    case MONOPOLY:
      return "classes/monopoly-src.txt";
    case VALIDATOR:
      return "classes/validator-src.txt";
    case CLI:
      return "classes/cli-src.txt";
    case CHESS:
      return "classes/chess-src.txt";
	case MATH:
		return "classes/math-src.txt";
	default:
		throw new RuntimeException("case missing for: " + PROGRAM);
    }
  }

  private static String getTestResource() {
    switch (PROGRAM) {
    case TRIANGLE:
      return "classes/triangle-test.txt";
    case MONOPOLY:
      return "classes/monopoly-test.txt";
    case VALIDATOR:
      return "classes/validator-test.txt";
    case CLI:
      return "classes/cli-test.txt";
    case CHESS:
      return "classes/chess-test.txt";
    case MATH:
		return "classes/math-test.txt";
	default:
		throw new RuntimeException("case missing for: " + PROGRAM);
    }
  }

  public static String getFeatureModelResource() {
    switch (PROGRAM) {
    case TRIANGLE:
      return "mutantgroups/triangle.txt";
    case MONOPOLY:
      return "mutantgroups/monopoly.txt";
    case VALIDATOR:
      return "mutantgroups/validator.txt";
    case CLI:
      return "mutantgroups/cli.txt";
    case CHESS:
      return "mutantgroups/chess.txt";
    case MATH:
		return "mutantgroups/math.txt";
	default:
		throw new RuntimeException("case missing for: " + PROGRAM);
    }
  }

  private static List<Class<?>> getAllJavaFilesInDir(String dirStr,
      String packageName) throws ClassNotFoundException {
    File directory = new File(dirStr);
    List<Class<?>> allJavaFiles = new ArrayList<>();

    for (File path : directory.listFiles()) {
      if (path.isFile() && path.getName().endsWith(".java") && !path.getName()
          .equals("package-info.java")) {
        String className = path.getName()
            .substring(0, path.getName().length() - ".java".length());
        try {
          if (packageName.length() > 0) {
            allJavaFiles.add(Class.forName(packageName + "." + className));
          } else {
            allJavaFiles.add(Class.forName(className));
          }
        } catch (ClassNotFoundException e) {
          System.out.println(e);
          System.out.println(e.getMessage());
        }
      } else if (path.isDirectory()) {
        if (packageName.length() > 0) {
          allJavaFiles.addAll(
              getAllJavaFilesInDir(dirStr + "/" + path.getName(),
                  packageName + "." + path.getName()));
        } else {
          allJavaFiles.addAll(
              getAllJavaFilesInDir(dirStr + "/" + path.getName(),
                  path.getName()));
        }
      }
    }
    return allJavaFiles;
  }
}
