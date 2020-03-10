package solver.bdd;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import benchmark.BenchmarkPrograms;
import benchmark.Benchmarker;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;

/**
 * This class finds the solutions using BDDs.
 * 
 * @author Jens Meinicke
 *
 */
public class BDDSolver {
	
	public final int minSize;

	public BDDSolver(int minsize) {
		this.minSize = minsize;
	}

	public Set<Set<String>> getSolutions(BDDFeatureExpr expr, String[] features) {
		return getSolutions(expr, features, x -> true);
	}
	
	@SuppressWarnings("unchecked")
	public Set<Set<String>> getSolutions(BDDFeatureExpr expr, String[] features, Function<Collection<String>, Boolean> checkValid) {
		List<byte[]> solutions = (List<byte[]>)expr.bdd().allsat();
		Set<Set<String>> allSolutions = new LinkedHashSet<>();
		for (byte[] s : solutions) {
			getSolutions(s, features, allSolutions, new HashSet<>(), 1, checkValid);
		}
		return allSolutions;
	}

	/**
	 * In the byte arrays, -1 means dont-care, 0 means 0, and 1 means 1.
	 * So we need to expand the given solution.
	 * @param solutions
	 * @param features
	 */
	private void getSolutions(byte[] solutions, String[] features, Set<Set<String>> allSolutions, Set<String> selections, int start, Function<Collection<String>, Boolean> checkValid) {
		if (selections.size() >= minSize && !checkValid.apply(selections)) {
			return;
		}
		
		for (int i = start; i <= features.length; i++) {
			byte selection = solutions[i];
			if (selection != 0) {
				if (selection == -1) {
					byte[] copy = new byte[features.length + 1];
					for (int j = i + 1; j <= features.length; j++) {
						copy[j] = solutions[j];
					}
					getSolutions(copy, features, allSolutions, new HashSet<>(selections), i + 1, checkValid);
				}
				
				selections.add(features[i - 1]);
				if (selections.size() >= minSize && !checkValid.apply(selections)) {
					return;
				}
				solutions[i] = 1;
			}
		}
		if (selections.size() >= minSize) { 
			Benchmarker.instance.timestamp(selections.toString());
			allSolutions.add(selections);
		}
	}
	
	public BigInteger getSolutionsCount(BDDFeatureExpr expr, String[] mutants) {
		@SuppressWarnings("unchecked")
		List<byte[]> solutions = (List<byte[]>) expr.bdd().allsat();
		BigInteger count = BigInteger.valueOf(0);
		
		boolean[] subsumedFOMs = new boolean[mutants.length];
		for (byte[] bs : solutions) {
			long thisCount = countSolutions(mutants, bs, subsumedFOMs);
			count = count.add(BigInteger.valueOf(thisCount));
			
		}
		int subsumedCount = 0;
		for (boolean b : subsumedFOMs) {
			if (b) {
				subsumedCount++;
			}
		}
		Benchmarker.instance.timestamp("subsumed FOMs: " + subsumedCount + " / " + mutants.length);
		return count;
	}

	private void computeSubsumedFOMs(String[] mutants, boolean[] subsumedFOMs, byte[] bs) {
		for (int i = 0; i < subsumedFOMs.length; i++) {
			int group = BenchmarkPrograms.getMakeshiftFeatureModel().get(mutants[i]);
			if (!subsumedFOMs[i] && bs[i + 1] != 0 && otherNotZero(mutants, group, bs)) {
				if (mutants[i].equals("_mut64")) {
					System.out.println(mutants[i]);
					otherNotZero(mutants, group, bs);
				}
				subsumedFOMs[i] = true;
			}
		}
		
	}

	private boolean otherNotZero(String[] mutants, int group, byte[] bs) {
		for (int j = 0; j < mutants.length; j++) {
			int otherGroup = BenchmarkPrograms.getMakeshiftFeatureModel().get(mutants[j]);
			if (group == otherGroup) {
				continue;
			}
			if (bs[j+1] != 0) {
				return true;
			}
		}
		
		return false;
	}

	private long countSolutions(String[] mutants, byte[] bs, boolean[] subsumedFOMs) {
		int lastGroup = Integer.MIN_VALUE;
		int groupSize = 0;
		long thisCount = 1;
		boolean hasSelection = false;
		int countOnes = 0;
		int groupSelections = 0; 
		List<Integer> groups = new ArrayList<>();
		for (int i = 1; i < bs.length && i <= mutants.length; i++) {
			int group = BenchmarkPrograms.getMakeshiftFeatureModel().get(mutants[i - 1]);
			if (lastGroup != group) {
				if (!hasSelection && groupSize > 0) {
					thisCount = thisCount * (groupSize + 1);
					groups.add(groupSize);
				}
				groupSize = 0;
				lastGroup = group;
				hasSelection = false;
				groupSelections = 0;
			} 
			if (bs[i] < 0) {
				groupSize++;
			}
			if (bs[i] == 1) {
				hasSelection = true;
				countOnes++;
				groupSelections++;
			}
			if (groupSelections > 1) {
				return 0;
			}
		}
		
		computeSubsumedFOMs(mutants, subsumedFOMs, bs);
		
		if (!hasSelection && groupSize > 0) {
			thisCount = thisCount * (groupSize + 1);
			groups.add(groupSize);
		}
		if (countOnes < 2) {
			if (groups.isEmpty()) {
				return 0;
			}
			if (countOnes == 0) {
				throw new RuntimeException();
			}
			if (thisCount > 0) {
				thisCount = computeCombination(groups);
			}
		}
		return thisCount;
	}

	private long computeCombination(List<Integer> groups) {
		if (groups.size() == 1) {
			return groups.get(0);
		}
		int[] array = new int[groups.size()];
		int i = 0;
		for (int value : groups) {
			array[i++] = value;
		}
		
		long multAll = 0;
		for (int j = 0; j < array.length; j++) {
			int jValue = array[j];
			for (int k = j; k < array.length; k++) {
				if (j == k) {
					continue;
				}
				jValue *= array[k] + 1;
			}
			multAll += jValue;
		}
		return multAll;
	}

}
