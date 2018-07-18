package benchmark;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.uclm.esi.iso5.juegos.monopoly.dominio.*;
import edu.uclm.esi.iso5.juegos.monopoly.dominio.tarjetas.*;
import edu.uclm.esi.iso5.juegos.monopoly.dominio.excepciones.*;
import edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.*;
import edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.grupos.*;

public class RunBenchmarks {
  public static final boolean RUNNING_LOCALLY = true;

  public static final Class[] TARGET_CLASSES = new Class[] {
      Salida.class,
      Tablero.class,
      Estacion.class,
      Impuesto.class,
      Barrio.class,
      Dados.class,
      CasillaConPrecio.class,
      Suerte.class,
      Calle.class,
      Compania.class,
      YaEstaHipotecadaException.class,
      DemasiadasCasasException.class,
      CapitalInsuficienteException.class,
      EstaEdificadaException.class,
      NoPoseeTodoElBarrioException.class,
      DebeSaldarLaDeudaException.class,
      DemasiadosJugadoresException.class,
      PocosJugadoresException.class,
      NoEstaEnLaCarcelException.class,
      PartidaYaEmpezadaException.class,
      BarrioConHipotecaException.class,
      HayMenosCasasException.class,
      NoEsElPropietarioException.class,
      CasillaNoVendibleException.class,
      NoTieneElTurnoException.class,
      NoEsEdificableException.class,
      NoTieneOpcionDeCompraException.class,
      NoEstaHipotecadaException.class,
      TVayaALaCarcel.class,
      Cobrar.class,
      Tarjeta.class,
      QuedaLibreDeLaCarcel.class,
      Pagar.class,
      CajaDeComunidad.class,
      Carcel.class,
      Jugador.class,
      Deuda.class,
      Parking.class,
      Casilla.class,
      testMain.class,
      ICtes.class,
      VayaALaCarcel.class }; // required to find all conditionals
  public static final Class[] TEST_CLASSES   = new Class[] {
      TestsMatarCubiertos.class,
      TableroTest.class,
      TableroTestPartida1.class,
      TestPartida3.class,
      DadosTest.class,
      TestEjerciciosYo.class,
      TestEjerciciosMuchasTarjetas.class,
      GrupoATests.class,
      TableroTestPartida2.class,
      TestsParaCubrirMutantes.class,
      TestCubrirTodos.class };

  private static String monopolyDir         = "/home/serena/reuse/mutated-monopoly/src/edu/uclm/esi/iso5/juegos/monopoly/dominio";
  private static String monopolyPackage     = "edu.uclm.esi.iso5.juegos.monopoly.dominio";
  private static String monopolyTestDir     = "/home/serena/reuse/mutated-monopoly/src/edu/uclm/esi/iso5/juegos/monopoly/dominio/tests";
  private static String monopolyTestPackage = "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests";

  private static String triangleDir         = "/home/serena/reuse/hom-generator/code-ut/src/mutated/triangle";
  private static String trianglePackage     = "mutated.triangle";
  private static String triangleTestDir     = "/home/serena/reuse/hom-generator/code-ut/test/mutated/triangle/improved";
  private static String triangleTestPackage = "mutated.triangle.improved";

  //  static {
  //    Class[] targetClasses, testClasses;
  //
  //    try {
  //      targetClasses = getAllJavaFilesInDir(monopolyDir, monopolyPackage)
  //          .toArray(new Class[0]);
  //    } catch (ClassNotFoundException e) {
  //      throw new RuntimeException(e);
  //    }
  //
  //    try {
  //      testClasses = getAllJavaFilesInDir(monopolyTestDir, monopolyTestPackage)
  //          .toArray(new Class[0]);
  //    } catch (ClassNotFoundException e) {
  //      throw new RuntimeException(e);
  //    }
  //
  //    TARGET_CLASSES = targetClasses;
  //    TEST_CLASSES = testClasses;
  //    System.out.println("TARGET CLASSES : " + Arrays.toString(TARGET_CLASSES));
  //    System.out.println("TEST CLASSES : " + Arrays.toString(TEST_CLASSES));
  //  }

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
