package analysis;

import java.util.Collection;

import benchmark.BenchmarkPrograms;
import util.SSHOMListener;
import util.SSHOMRunner;

public class FindEqFOMs {
  public static void main(String[] args)
      throws NoSuchFieldException, IllegalAccessException {
    SSHOMRunner sshomRunner = new SSHOMRunner(BenchmarkPrograms.getTargetClasses(), BenchmarkPrograms.getTestClasses());
    Collection<String> mutants = sshomRunner.getMutants();
    for (String m : mutants) {
      SSHOMListener sshomListener = sshomRunner.runJunitOnHOM(m);
      if (sshomListener.getHomTests().size() <= 0) {
        System.out.println(m);
      }
    }
  }
}
