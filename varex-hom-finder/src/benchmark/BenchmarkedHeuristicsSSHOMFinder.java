package benchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.junit.runner.Description;

import util.CheckStronglySubsuming;
import util.SSHOMListener;
import util.SSHOMRunner;

public class BenchmarkedHeuristicsSSHOMFinder {

	//heuristics parameters

	private int maxOrderSSHOMs = 4;
	private boolean runOverlappedTests = false;
	private boolean runNPlusOne = false; //not implemented
	private boolean runEqualSets = false; //not implemented

	//class attributes

	private SSHOMRunner runner;
	private Map<String, Set<Description>> foms;
	private long count = 0;
	private Class<?>[] testClasses;
	private String[] allMutants;

	public static void main(String[] args) {
		
		try {
			BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.TRIANGLE; //temporary

			BenchmarkedHeuristicsSSHOMFinder heuristics = new BenchmarkedHeuristicsSSHOMFinder();
			heuristics.runOverlappedTests = true;
			heuristics.heuristicsSSHOMFinder();
		} catch (IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	public void heuristicsSSHOMFinder() throws IllegalAccessException, NoSuchFieldException {
		
		Benchmarker.instance.start();

		Class<?>[] targetClasses = BenchmarkPrograms.getTargetClasses();
		this.allMutants = BenchmarkPrograms.getMutantNames();
		this.testClasses = BenchmarkPrograms.getTestClasses();

		this.foms = new HashMap<>();
		this.runner = new SSHOMRunner(targetClasses, testClasses);
		this.populateFoms(allMutants);

		Benchmarker.instance.timestamp("start homs");
		for (int currOrder = 2; currOrder < this.maxOrderSSHOMs; currOrder++) {

			if (runOverlappedTests) {
				overlappedTestsHeuristic(currOrder, new ArrayList<String>(), 0);
			}

			if (runNPlusOne && currOrder > 2) {
				nPlusOneHeuristic(currOrder);
			}

			if (runEqualSets) {
				equalSetsHeuristic(currOrder);
			}

			Benchmarker.instance.timestamp("order " + currOrder + " done");
		}
	}

	/*
	 * copied from BenchmarkedNaiveSSHOMFinder
	 */
	private void populateFoms(String[] mutants)
			throws NoSuchFieldException, IllegalAccessException {
		//		    int i = 0;
		for (String m : mutants) {
			SSHOMListener listener = runner.runJunitOnHOM(m);
			this.foms.put(m, listener.getHomTests());
		}
	}

	private void equalSetsHeuristic(int order) {
		// TODO Auto-generated method stub

	}

	private void nPlusOneHeuristic(int order) {
		// TODO Auto-generated method stub

	}

	private void overlappedTestsHeuristic(int order, List<String> selectedMutants, int mutantStart)
			throws IllegalAccessException, NoSuchFieldException {

		if (order <= 0) {
			SSHOMListener listener = runner.runJunitOnHOM(selectedMutants.toArray(new String[0]));
			List<Set<Description>> currentFoms = selectedMutants.stream()
					.map(m -> foms.get(m))
					.collect(Collectors.toList());

			System.out.println(this.count++);

			if (CheckStronglySubsuming.isStronglySubsuming(listener.getHomTests(), currentFoms)) {
				Benchmarker.instance.timestamp(
						String.join(",", selectedMutants) + " is_strict_subsuming: "
								+ CheckStronglySubsuming.isStrictStronglySubsuming(listener.getHomTests(), currentFoms));
			}
		}
		else if (mutantStart < allMutants.length) {
			for (int i = mutantStart; i < allMutants.length; i++) {
				List<String> newSelected = new ArrayList<>(selectedMutants);
				newSelected.add(allMutants[i]);
				if (BenchmarkPrograms.homIsValid(newSelected) &&
						overlappedTests(newSelected)) {
					System.out.println("HOM with overlapped tests: " + newSelected); //DEBUGGING
					overlappedTestsHeuristic(order - 1, newSelected, i + 1);
				}
			}
		}
	}

	private boolean overlappedTests(List<String> hom) {

		Set<Description> intersection = new HashSet<>(foms.get(hom.get(0)));

		for (int i=1; i<hom.size(); i++) {
			intersection.retainAll(foms.get(hom.get(i)));
			if (intersection.isEmpty()) {
				return false;
			}
		}
		return true;
	}
}
