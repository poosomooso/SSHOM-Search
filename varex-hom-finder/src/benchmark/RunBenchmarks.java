package benchmark;

import java.io.IOException;
import java.util.Arrays;

public class RunBenchmarks {
  public static final boolean RUNNING_LOCALLY = true;

  public static final Class[] TARGET_CLASSES;
  public static final Class[] TEST_CLASSES;
  static {
    TARGET_CLASSES = BenchmarkPrograms.getSrcClasses();
    TEST_CLASSES = BenchmarkPrograms.getTestClasses();
    System.out.println("TARGET CLASSES : " + Arrays.toString(TARGET_CLASSES));
    System.out.println("TEST CLASSES : " + Arrays.toString(TEST_CLASSES));
  }
//  public static final Class[] TARGET_CLASSES = new Class[] {
//      Salida.class,
//      Tablero.class,
//      Estacion.class,
//      Impuesto.class,
//      Barrio.class,
//      Dados.class,
//      CasillaConPrecio.class,
//      Suerte.class,
//      Calle.class,
//      Compania.class,
//      YaEstaHipotecadaException.class,
//      DemasiadasCasasException.class,
//      CapitalInsuficienteException.class,
//      EstaEdificadaException.class,
//      NoPoseeTodoElBarrioException.class,
//      DebeSaldarLaDeudaException.class,
//      DemasiadosJugadoresException.class,
//      PocosJugadoresException.class,
//      NoEstaEnLaCarcelException.class,
//      PartidaYaEmpezadaException.class,
//      BarrioConHipotecaException.class,
//      HayMenosCasasException.class,
//      NoEsElPropietarioException.class,
//      CasillaNoVendibleException.class,
//      NoTieneElTurnoException.class,
//      NoEsEdificableException.class,
//      NoTieneOpcionDeCompraException.class,
//      NoEstaHipotecadaException.class,
//      TVayaALaCarcel.class,
//      Cobrar.class,
//      Tarjeta.class,
//      QuedaLibreDeLaCarcel.class,
//      Pagar.class,
//      CajaDeComunidad.class,
//      Carcel.class,
//      Jugador.class,
//      Deuda.class,
//      Parking.class,
//      Casilla.class,
//      testMain.class,
//      ICtes.class,
//      VayaALaCarcel.class }; // required to find all conditionals
//  public static final Class[] TEST_CLASSES   = new Class[] {
//      TestsMatarCubiertos.class,
//      TableroTest.class,
//      TableroTestPartida1.class,
//      TestPartida3.class,
//      DadosTest.class,
//      TestEjerciciosYo.class,
//      TestEjerciciosMuchasTarjetas.class,
//      GrupoATests.class,
//      TableroTestPartida2.class,
//      TestsParaCubrirMutantes.class,
//      TestCubrirTodos.class };

  public static void main(String[] args)
      throws NoSuchFieldException, IOException, IllegalAccessException {
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
      throws IOException {
    BenchmarkedVarexSSHOMFinder varexSSHOMFinder = new BenchmarkedVarexSSHOMFinder();
    varexSSHOMFinder.varexSSHOMFinder(targets, tests);
  }

}
