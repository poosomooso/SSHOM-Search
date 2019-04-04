package benchmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.runner.Description;

import util.CheckStronglySubsuming;
import util.SSHOMListener;
import util.SSHOMRunner;

public class BenchmarkedHeuristicsSSHOMFinder {

	//heuristics parameters

	private int maxSSHOMsOrder;
	private boolean runOverlappedTests;
	private boolean runNPlusOne;
	private boolean runEqualSets;

	//class attributes

	private SSHOMRunner runner;
	private Map<String, Set<Description>> foms;
	private Map<Integer, List<Set<String>>> sshomsByOrder; //aux for n + 1 heuristic
	
	private long globalCount = 0;
	private Class<?>[] testClasses;
	private String[] allMutants;

	public static void main(String[] args) {
		//TODO after having all heuristics implemented, move this initialization to 'RunBenchmarks'
		try {
			BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.TRIANGLE; //temporary

			BenchmarkedHeuristicsSSHOMFinder heuristics = new BenchmarkedHeuristicsSSHOMFinder();
			heuristics.maxSSHOMsOrder = 4;
			heuristics.runOverlappedTests = true;
			heuristics.runNPlusOne = true;
			heuristics.runEqualSets = true;
			heuristics.heuristicsSSHOMFinder();
		} catch (IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	public void heuristicsSSHOMFinder() throws IllegalAccessException, NoSuchFieldException {
		
		if (this.runNPlusOne && !this.runEqualSets && !this.runOverlappedTests) {
		    System.err.println("N + 1 heuristic requires another heuristic to search for 2nd order HOMs.");
		    System.exit(0);
		}
		if (this.runEqualSets && this.runOverlappedTests) {
		    System.err.println("Equal Sets heuristic takes high priority over Overlapped Tests.");
		    System.exit(0);
		}

		Benchmarker.instance.start();

		Class<?>[] targetClasses = BenchmarkPrograms.getTargetClasses();
		this.allMutants = BenchmarkPrograms.getMutantNames();
		this.testClasses = BenchmarkPrograms.getTestClasses();

		this.foms = new HashMap<>();
		this.sshomsByOrder = new HashMap<>(); //n + 1 heuristic
		this.runner = new SSHOMRunner(targetClasses, testClasses);
		this.populateFoms();

		Benchmarker.instance.timestamp("start homs");
		for (int currOrder = 2; currOrder < this.maxSSHOMsOrder; currOrder++) {

			if (this.runNPlusOne) {
				this.sshomsByOrder.put(currOrder, new ArrayList<>());
			}

			this.searchWithHeuristics(currOrder, new ArrayList<String>(), 0);
			
			if (this.runNPlusOne && currOrder > 2) {
				this.sshomsByOrder.remove(currOrder - 1); //saving memory
			}

			Benchmarker.instance.timestamp("order " + currOrder + " done");
		}
	}

	/*
	 * based on BenchmarkedNaiveSSHOMFinder
	 */
	private void populateFoms()
			throws NoSuchFieldException, IllegalAccessException {

		for (String m : this.allMutants) {
			SSHOMListener listener = runner.runJunitOnHOM(m);
			this.foms.put(m, listener.getHomTests());
		}
	}

	/**
	 * recursive
	 * @param order
	 * @param selectedMutants
	 * @param mutantStart
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	private void searchWithHeuristics(int order, List<String> selectedMutants, int mutantStart)
			throws IllegalAccessException, NoSuchFieldException {

		if (order <= 0) {
			SSHOMListener listener = runner.runJunitOnHOM(selectedMutants.toArray(new String[0]));
			List<Set<Description>> currentFoms = selectedMutants.stream()
					.map(m -> foms.get(m))
					.collect(Collectors.toList());

			System.out.println(this.globalCount++);

			if (CheckStronglySubsuming.isStronglySubsuming(listener.getHomTests(), currentFoms)) {
				
				if (this.runNPlusOne) {
					this.sshomsByOrder.get(selectedMutants.size()).add(new HashSet<>(selectedMutants));
				}
				
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
						(this.runEqualSets && this.checkEqualSets(newSelected) ||
						this.runOverlappedTests && this.checkOverlappedTests(newSelected)) && 
						this.runNPlusOne && this.checkNPlusOne(newSelected)) {

					searchWithHeuristics(order - 1, newSelected, i + 1);
				}
			}
		}
	}

	/**
	 * 
	 * @param hom
	 * @return
	 */
	private boolean checkNPlusOne(List<String> hom) {

		if (hom.size() <= 2) return true;  
		
		List<Set<String>> sshomsN = this.sshomsByOrder.get(hom.size() - 1);
		
		for (String fom : hom) {
			Set<String> homN = new HashSet<String>(hom); 
			homN.remove(fom);

			if (sshomsN.contains(homN)) return true;
		}
		
		return false;
	}

	private boolean checkOverlappedTests(List<String> hom) {

		Set<Description> intersection = new HashSet<>(this.foms.get(hom.get(0)));

		for (int i=1; i<hom.size(); i++) {
			intersection.retainAll(this.foms.get(hom.get(i)));
			if (intersection.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private boolean checkEqualSets(List<String> hom) {

		Set<Description> baseSet = new HashSet<>(this.foms.get(hom.get(0)));

		for (int i=1; i<hom.size(); i++) {
			if (!baseSet.containsAll(this.foms.get(hom.get(i)))) {
				return false;
			}
		}
		return true;
	}
}
