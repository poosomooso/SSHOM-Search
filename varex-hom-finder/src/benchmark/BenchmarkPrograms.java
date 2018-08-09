package benchmark;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BenchmarkPrograms {

  public enum Program {
    TRIANGLE, MONOPOLY
  }
  public static final Program PROGRAM = Program.TRIANGLE;
  private static final String PATH_TO_RESOURCE = "varex-hom-finder/resources/";

  public static Class[] getSrcClasses() {
    ArrayList<Class> classes = new ArrayList<>();
    URL u = RunBenchmarks.class.getClassLoader()
        .getResource(getSrcResource());

    try {
      if (u == null) {
        PrintWriter printWriter = new PrintWriter(PATH_TO_RESOURCE+getSrcResource());
        Class[] targetClasses = BenchmarkPrograms
            .getAllJavaFilesInDir(getSrcDir(), getSrcPackage())
            .toArray(new Class[0]);
        for (Class c : targetClasses) {
          printWriter.println(c.getName());
        }
        printWriter.close();
        return targetClasses;

      } else {
        Scanner in = new Scanner(u.openStream());
        while (in.hasNextLine()) {
          classes.add(Class.forName(in.nextLine()));
        }
        return classes.toArray(new Class[0]);
      }
    } catch (ClassNotFoundException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Class[] getTestClasses() {
    ArrayList<Class> classes = new ArrayList<>();
    URL u = RunBenchmarks.class.getClassLoader()
        .getResource(getTestResource());

    try {
      if (u == null) {
        PrintWriter printWriter = new PrintWriter(PATH_TO_RESOURCE + getTestResource());
        Class[] targetClasses = BenchmarkPrograms
            .getAllJavaFilesInDir(getTestDir(), getTestPackage())
            .toArray(new Class[0]);
        for (Class c : targetClasses) {
          printWriter.println(c.getName());
        }
        printWriter.close();
        return targetClasses;

      } else {
        Scanner in = new Scanner(u.openStream());
        while (in.hasNextLine()) {
          classes.add(Class.forName(in.nextLine()));
        }
        return classes.toArray(new Class[0]);
      }
    } catch (ClassNotFoundException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static String localMonopolyTestDir     = "/home/serena/reuse/mutated-monopoly/src/edu/uclm/esi/iso5/juegos/monopoly/dominio/tests";
  private static String localMonopolyDir         = "/home/serena/reuse/mutated-monopoly/src/edu/uclm/esi/iso5/juegos/monopoly/dominio";
  private static String localMonopolyPackage     = "edu.uclm.esi.iso5.juegos.monopoly.dominio";
  private static String localMonopolyTestPackage = "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests";

  private static String localTriangleDir         = "/home/serena/reuse/hom-generator/code-ut/src/mutated/triangle";
  private static String localTrianglePackage     = "mutated.triangle";
  private static String localTriangleTestDir     = "/home/serena/reuse/hom-generator/code-ut/test/mutated/triangle/improved";
  private static String localTriangleTestPackage = "mutated.triangle.improved";

  private static String getSrcDir() {
    switch (PROGRAM) {
    case TRIANGLE:
      return localTriangleDir;
    case MONOPOLY:
      return localMonopolyDir;
    }
    return "";
  }

  private static String getSrcPackage() {
    switch (PROGRAM) {
    case TRIANGLE:
      return localTrianglePackage;
    case MONOPOLY:
      return localMonopolyPackage;
    }
    return "";
  }

  private static String getTestDir() {
    switch (PROGRAM) {
    case TRIANGLE:
      return localTriangleTestDir;
    case MONOPOLY:
      return localMonopolyTestDir;
    }
    return "";
  }

  private static String getTestPackage() {
    switch (PROGRAM) {
    case TRIANGLE:
      return localTriangleTestPackage;
    case MONOPOLY:
      return localMonopolyTestPackage;
    }
    return "";
  }

  private static String getSrcResource() {
    switch (PROGRAM) {
    case TRIANGLE:
      return "classes/triangle-src.txt";
    case MONOPOLY:
      return "classes/monopoly-src.txt";
    }
    return "";
  }

  private static String getTestResource() {
    switch (PROGRAM) {
    case TRIANGLE:
      return "classes/triangle-test.txt";
    case MONOPOLY:
      return "classes/monopoly-test.txt";
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
