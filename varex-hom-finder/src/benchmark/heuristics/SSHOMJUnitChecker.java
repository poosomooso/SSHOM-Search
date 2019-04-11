package benchmark.heuristics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.runner.Description;

import benchmark.BenchmarkPrograms;
import benchmark.InfLoopTestProcess;
import util.CheckStronglySubsuming;
import util.SSHOMListener;
import util.SSHOMRunner;

/**
 * 
 * @author Jens Meinicke
 *
 */
public class SSHOMJUnitChecker implements ISSHOMChecker {

	private SSHOMRunner runner;
	private Class<?>[] testClasses;
	private Map<String, Set<Description>> foms;
	
	public SSHOMJUnitChecker(SSHOMRunner runner, Class<?>[] testClasses,Map<String, Set<Description>> foms) {
		this.runner = runner;
		this.testClasses = testClasses;
		this.foms = foms;
	}
	
	@Override
	public boolean isSSHOM(HigherOrderMutant homCandidate) {
		Collection<String> selectedMutants = new ArrayList<>();
		for (FirstOrderMutant mutant : homCandidate) {
			selectedMutants.add(mutant.getMutant());
		}
		final SSHOMListener listener;
		if (BenchmarkPrograms.programHasInfLoops()) {
			listener = InfLoopTestProcess.getFailedTests(testClasses, selectedMutants.toArray(new String[0]));
		} else {
			try {
				listener = runner.runJunitOnHOM(selectedMutants.toArray(new String[0]));
			} catch (IllegalAccessException | NoSuchFieldException e) {
				e.printStackTrace();
				return false;
			}
		}
		List<Set<Description>> currentFoms = selectedMutants.stream().map(m -> foms.get(m))
				.collect(Collectors.toList());

		return CheckStronglySubsuming.isStronglySubsuming(listener.getHomTests(), currentFoms);
	}

}
