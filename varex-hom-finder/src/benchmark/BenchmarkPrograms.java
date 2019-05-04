package benchmark;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.junit.Test;

import benchmark.heuristics.FirstOrderMutant;
import evaluation.analysis.Mutation;
import evaluation.io.MutationParser;
import mutated.triangleAll.Triangle;
import mutated.triangleAll.Triangle_ESTest_improved;

public class BenchmarkPrograms {
  public enum Program {
    TRIANGLE, MONOPOLY, VALIDATOR, CLI, CHESS, MATH, ANT
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
        targetClasses = loadClasses(getSrcResource(), getSrcDir(), getSrcPackage(), false);
      }
//      System.out.println("TARGET CLASSES : " + Arrays.toString(targetClasses));
    }
    return targetClasses;
  }

  public static Class<?>[] getTestClasses() {
    if (testClasses == null) {
      if (PROGRAM == Program.TRIANGLE) {
        testClasses = new Class[] { Triangle_ESTest_improved.class };
      } else {
        testClasses = loadClasses(getTestResource(), getTestDir(), getTestPackage(), true);
      }
//      System.out.println("TEST CLASSES : " + Arrays.toString(testClasses));
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
  
	private static final BitSet bitSet = new BitSet();
	
	public static synchronized boolean homIsValidFOM(Collection<FirstOrderMutant> hom) {
		bitSet.clear();
		for (FirstOrderMutant mutation : hom) {
			if (!check(bitSet, mutation.getMutant())) {
				return false;
			}
		}
		return true;
	}

  public static boolean homIsValid(String... hom) {
    return homIsValid(Arrays.asList(hom));
  }
  
	private static boolean check(BitSet check, String mutant) {
		int id = BenchmarkPrograms.getMakeshiftFeatureModel().get(mutant);
		if (check.get(id)) {
			return false;
		} else {
			check.set(id);
		}
		return true;
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

  private static Class<?>[] loadClasses(String resourceStr, String classDir, String classPackage, boolean filterTestClasses) {
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
	          Class<?> klass = Class.forName(in.nextLine());
	          if (filterTestClasses) {
	        	  if (isTestClass(klass)) {
	        		  classes.add(klass);  
	        	  }
	          } else {
	        	  classes.add(klass);
	          }
	        }
        }
        return classes.toArray(new Class[0]);
      }
    } catch (ClassNotFoundException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean isTestClass(Class<?> klass) {
	if (Modifier.isAbstract(klass.getModifiers())) {
		return false;
	}
	Method[] methods = klass.getDeclaredMethods();
	for (Method method : methods) {
		if (Modifier.isPublic(method.getModifiers()) && method.isAnnotationPresent(Test.class)) {
			return true;
		}
	}
	return false;
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
	case ANT:
		return "classes/ant-src.txt";
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
	case ANT:
		return "classes/ant-test.txt";
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
    case ANT:
		return "mutantgroups/ant.txt";
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
  
  	private static Map<String, Mutation> mutationsInfos = null;
  
	public static Map<String, Mutation> getMuationInfo() {
		if (mutationsInfos == null) {
			InputStream resourceURL = RunBenchmarks.class.getClassLoader().getResourceAsStream("evaluationfiles/" + BenchmarkPrograms.PROGRAM.toString().toLowerCase() + "/mapping.txt");
			mutationsInfos = MutationParser.instance.getMutations(resourceURL);
		}
		return mutationsInfos;
	}
  
	public static Map<String, Set<String>> createMutationGroups() {
		Map<String, Set<String>> groupMutants = new LinkedHashMap<>();
		String[] mutants = BenchmarkPrograms.getMutantNames();
		if (Flags.getGranularity() == Flags.GRANULARITY.ALL) {
			Set<String> allMutants = new HashSet<>();
			for (String m : mutants) {
				allMutants.add(m);
			}
			groupMutants.put("", allMutants);
			return groupMutants;
		}
		
		// group mutations
		Map<String, Mutation> mutations = getMuationInfo();
		for (String m : mutants) {
			Mutation mutation = mutations.get(m);
			if (mutation == null) {
				throw new RuntimeException("Mutation not found: " + m);
			}
			final String groupIdenntifyer = getGroupIdentifyer(mutation);
			groupMutants.putIfAbsent(groupIdenntifyer, new HashSet<>());
			groupMutants.get(groupIdenntifyer).add(m);
		}
		return groupMutants;
	}
	
	private static String getGroupIdentifyer(Mutation mutation) {
		final  String groupIdenntifyer;
		
		switch (Flags.getGranularity()) {
		case ALL:
			groupIdenntifyer = "";
			break;
		case PACKAGE:
			groupIdenntifyer = mutation.className.substring(0, mutation.className.lastIndexOf('.'));
			break;
		case CLASS:
			groupIdenntifyer = mutation.className;
			break;
		case METHOD:
			groupIdenntifyer = mutation.className + "." + mutation.methodName;
			break;
		default:
			throw new RuntimeException("granularity not implemented: " + Flags.getGranularity());
		}
		return groupIdenntifyer;
	}
}
