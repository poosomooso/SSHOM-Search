package benchmark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.junit.runner.Description;

import util.CheckStronglySubsuming;
import util.SSHOMListener;
import util.SSHOMRunner;

public class BenchmarkedNaiveSSHOMFinder {
	private SSHOMRunner runner;
	private Map<String, Set<Description>> foms = new HashMap<>();
	static long x = 0;
	private Class<?>[] testClasses;

	public void naiveSSHOMFinder() throws NoSuchFieldException, IllegalAccessException {
		Benchmarker.instance.start();

		Class<?>[] targetClasses = BenchmarkPrograms.getTargetClasses();
		this.testClasses = BenchmarkPrograms.getTestClasses();

		runner = new SSHOMRunner(targetClasses, testClasses);

		Map<String, Set<String>> groupMutants = BenchmarkPrograms.createMutationGroups();
		for (Entry<String, Set<String>> groupEntry : groupMutants.entrySet()) {
			long startTime = System.currentTimeMillis();
			String[] mutants = groupEntry.getValue().toArray(new String[0]);

			int maxOrder = 3;
			Benchmarker.instance.timestamp("start homs");
			for (int i = 2; i < maxOrder; i++) {
				if (System.currentTimeMillis() - startTime > Flags.getMaxGroupTime()) {
					return;
				}
				runOnNOrder(i, new ArrayList<>(), mutants, 0, startTime);
				Benchmarker.instance.timestamp("order " + i + " done");
			}
		}
	}

	private void lazyInitializeFoms(Collection<String> mutants) throws NoSuchFieldException, IllegalAccessException {
		for (String m : mutants) {
			if (!foms.containsKey(m)) {
				SSHOMListener listener;
				if (BenchmarkPrograms.programHasInfLoops()) {
					listener = InfLoopTestProcess.getFailedTests(testClasses, new String[] { m });
				} else {
					listener = runner.runJunitOnHOM(m);
				}
				foms.put(m, listener.getHomTests());
			}
		}
	}

	private void runOnNOrder(int order, List<String> selectedMutants, String[] allMutants, int mutantStart,
			final long start) throws NoSuchFieldException, IllegalAccessException {
		if (System.currentTimeMillis() - start > Flags.getMaxGroupTime()) {
			return;
		}
		if (order <= 0) {
			lazyInitializeFoms(selectedMutants);
			SSHOMListener listener;
			if (BenchmarkPrograms.programHasInfLoops()) {
				listener = InfLoopTestProcess.getFailedTests(testClasses, selectedMutants.toArray(new String[0]));
			} else {
				listener = runner.runJunitOnHOM(selectedMutants.toArray(new String[0]));
			}
			List<Set<Description>> currentFoms = selectedMutants.stream().map(m -> foms.get(m))
					.collect(Collectors.toList());

			if (CheckStronglySubsuming.isStronglySubsuming(listener.getHomTests(), currentFoms)) {
				Benchmarker.instance.timestamp(String.join(",", selectedMutants) + " is_strict_subsuming: "
						+ CheckStronglySubsuming.isStrictStronglySubsuming(listener.getHomTests(), currentFoms));
			}
		} else if (mutantStart < allMutants.length) {
			for (int i = mutantStart; i < allMutants.length; i++) {
				List<String> newSelected = new ArrayList<>(selectedMutants);
				newSelected.add(allMutants[i]);
				if (BenchmarkPrograms.homIsValid(newSelected)) {
					runOnNOrder(order - 1, newSelected, allMutants, i + 1, start);
				}
			}
		}
	}

}
