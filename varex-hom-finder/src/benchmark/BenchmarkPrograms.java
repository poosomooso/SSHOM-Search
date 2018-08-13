package benchmark;

import mutated.triangleAll.Triangle;
import mutated.triangleAll.Triangle_ESTest_improved;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;

public class BenchmarkPrograms {
  public enum Program {
    TRIANGLE, MONOPOLY, VALIDATOR
  }
  public static final Program PROGRAM = Program.TRIANGLE;
  private static final String PATH_TO_RESOURCE = "out/production/varex-hom-finder/";
  private static Class[] targetClasses;
  private static Class[] testClasses;
  private static Map<String, Integer> makeshiftFeatureModel;


  public static Class[] getTargetClasses() {
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

  public static Class[] getTestClasses() {
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

  public static Map<String, Integer> getMakeshiftFeatureModel() {
    if (makeshiftFeatureModel == null) {
      Scanner in = new Scanner(RunBenchmarks.class.getClassLoader()
          .getResourceAsStream(getFeatureModelResource()));
      makeshiftFeatureModel = new HashMap<>();
      int id = 0;
      while (in.hasNextLine()) {
        String[] mutants = in.nextLine().split(" ");
        for (String m : mutants) {
          makeshiftFeatureModel.put(m, id);
        }
        id++;
      }
    }
    return makeshiftFeatureModel;
  }

  private static Class[] loadClasses(String resourceStr, String classDir, String classPackage) {
    ArrayList<Class> classes = new ArrayList<>();
    URL u = RunBenchmarks.class.getClassLoader()
        .getResource(resourceStr);

    try {
      if (u == null) {
        PrintWriter printWriter = new PrintWriter(PATH_TO_RESOURCE + resourceStr);
        Class[] targetClasses = BenchmarkPrograms
            .getAllJavaFilesInDir(classDir, classPackage)
            .toArray(new Class[0]);
        for (Class c : targetClasses) {
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
        Scanner in = new Scanner(RunBenchmarks.class.getClassLoader()
            .getResourceAsStream(resourceStr));
        while (in.hasNextLine()) {
          classes.add(Class.forName(in.nextLine()));
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

  private static String getSrcDir() {
    switch (PROGRAM) {
    case TRIANGLE:
      return localTriangleDir;
    case MONOPOLY:
      return localMonopolyDir;
    case VALIDATOR:
      return localValidatorDir;
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
    }
    return "";
  }

  private static String getTestResource() {
    switch (PROGRAM) {
    case TRIANGLE:
      return "classes/triangle-test.txt";
    case MONOPOLY:
      return "classes/monopoly-test.txt";
    case VALIDATOR:
      return "classes/validator-test.txt";
    }
    return "";
  }

  private static String getFeatureModelResource() {
    switch (PROGRAM) {
    case TRIANGLE:
      return "mutantgroups/triangle.txt";
    case MONOPOLY:
      return "mutantgroups/monopoly.txt";
    case VALIDATOR:
      return "mutantgroups/validator.txt";
    }
    return "";
  }

  private static List<Class> getAllJavaFilesInDir(String dirStr,
      String packageName) throws ClassNotFoundException {
    File directory = new File(dirStr);
    List<Class> allJavaFiles = new ArrayList<>();

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
