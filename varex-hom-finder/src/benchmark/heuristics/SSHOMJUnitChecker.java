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
 * TODO description
 * 
 * @author Jens Meinicke
 *
 */
public class SSHOMJUnitChecker implements ISSHOMChecker {

	private final SSHOMRunner runner;
	private final Class<?>[] testClasses;
	private Map<String, Set<Description>> foms;
	
	public SSHOMJUnitChecker(SSHOMRunner runner, Class<?>[] testClasses,Map<String, Set<Description>> foms) {
		this.runner = runner;
		this.testClasses = testClasses;
		this.foms = foms;
	}
	
	@Override
	public HOM_TYPE getHOMType(Map<String, Set<String>> testMap, HigherOrderMutant homCandidate) {
		Collection<String> selectedMutants = new ArrayList<>();
		for (FirstOrderMutant mutant : homCandidate) {
			selectedMutants.add(mutant.getMutant());
		}
		final SSHOMListener listener;
		if (BenchmarkPrograms.programHasInfLoops()) {
			listener = InfLoopTestProcess.getFailedTests(testClasses, testMap, selectedMutants.toArray(new String[selectedMutants.size()]));
		} else {
			try {
				listener = runner.runJunitOnHOM(selectedMutants.toArray(new String[selectedMutants.size()]));
			} catch (IllegalAccessException | NoSuchFieldException e) {
				e.printStackTrace();
				return ISSHOMChecker.HOM_TYPE.NONE;
			}
		}
		List<Set<Description>> currentFoms = selectedMutants.stream().map(m -> foms.get(m))
				.collect(Collectors.toList());

		boolean isStronglySubsuming = CheckStronglySubsuming.isStronglySubsuming(listener.getHomTests(), currentFoms);
		boolean isStrict =  CheckStronglySubsuming.isStrictStronglySubsuming(listener.getHomTests(), currentFoms);
		if (isStronglySubsuming) {
			if (isStrict) {
				return HOM_TYPE.STRICT_STRONGLY_SUBSUMING;
			} else {
				return HOM_TYPE.STRONGLY_SUBSUMING;
			}
		} 
		return HOM_TYPE.NONE;
	}

}
