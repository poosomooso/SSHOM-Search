package benchmark;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RunBenchmarks {
  public static final boolean RUNNING_LOCALLY = true;
  //  public static final Class[] TARGET_CLASSES = { Triangle.class };
  //  public static final Class[] TEST_CLASSES   = { Triangle_ESTest_improved.class };

  public static final Class[] TARGET_CLASSES = {
      edu.uclm.esi.iso5.juegos.monopoly.dominio.Barrio.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.CajaDeComunidad.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.Calle.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.Carcel.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.Casilla.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.CasillaConPrecio.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.Compania.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.Dados.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.Deuda.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.Estacion.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.ICtes.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.Impuesto.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.Jugador.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.Parking.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.Salida.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.Suerte.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.Tablero.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.VayaALaCarcel.class
  };

  //  static {
  //    Class[] targetClasses;
  //    try {
  //      targetClasses = getAllJavaFilesInDir(
  //          "/home/serena/reuse/mutated-commons-validator/src/main/java",
  //          "").toArray(new Class[0]);
  //    } catch (ClassNotFoundException e) {
  //      throw new RuntimeException(e);
  //    }
  //    TARGET_CLASSES = targetClasses;
  //  }
  public static final Class[] TEST_CLASSES = {
      edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.DadosTest.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTest.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTestPartida1.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTestPartida2.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestCubrirTodos.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestEjerciciosMuchasTarjetas.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestEjerciciosYo.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestPartida3.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestsMatarCubiertos.class,
      edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestsParaCubrirMutantes.class };

  public static void main(String[] args)
      throws NoSuchFieldException, IllegalAccessException {
    if (args.length < 1) {
      System.err
          .println("A mode must be specified. One of : naive, ga, or varex");
      System.exit(0);
    }
    String whichProgram = args[0];
    if (whichProgram.equalsIgnoreCase("naive")) {
      runNaive(TARGET_CLASSES, TEST_CLASSES);
    } else if (whichProgram.equalsIgnoreCase("ga")) {
      runEvolutionary(TARGET_CLASSES, TEST_CLASSES);
    } else if (whichProgram.equalsIgnoreCase("varex")) {
      runVarex(TARGET_CLASSES, TEST_CLASSES);
    } else {
      System.err
          .println("A mode must be specified. One of : naive, ga, or varex");
      System.exit(0);
    }
  }

  private static void runNaive(Class[] targets, Class[] tests)
      throws NoSuchFieldException, IllegalAccessException {
    BenchmarkedNaiveSSHOMFinder naiveSSHOMFinder = new BenchmarkedNaiveSSHOMFinder();
    naiveSSHOMFinder.naiveSSHOMFinder(targets, tests);
  }

  private static void runEvolutionary(Class[] targets, Class[] tests)
      throws NoSuchFieldException, IllegalAccessException {
    BenchmarkedEvolutionarySSHOMFinder evolutionarySSHOMFinder = new BenchmarkedEvolutionarySSHOMFinder();
    evolutionarySSHOMFinder.geneticAlgorithm(targets, tests);
  }

  private static void runVarex(Class[] targets, Class[] tests)
      throws NoSuchFieldException, IllegalAccessException {
    BenchmarkedVarexSSHOMFinder varexSSHOMFinder = new BenchmarkedVarexSSHOMFinder();
    varexSSHOMFinder.varexSSHOMFinder(targets, tests);
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
        //        try {
        if (packageName.length() > 0) {
          System.out.println((packageName + "." + className));
          //            Class.forName(packageName + "." + className);
        } else {
          System.out.println(className);
          //            Class.forName(className);
        }
        //        } catch (ClassNotFoundException e) {
        //          System.out.println(e);
        //          System.out.println(e.getMessage());
        //        }
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
