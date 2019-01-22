package evaluation.analysis;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import evaluation.io.HOMParser;
import evaluation.io.MutationParser;
import evaluation.io.ResultsWriter;
import evaluation.io.StatsParser;
import evaluation.io.TestParser;

public class QuantitativeEvaluation {

	public static void main(String[] args) throws URISyntaxException, IOException {
		URL resource = QuantitativeEvaluation.class.getClassLoader().getResource("evaluationfiles");
		File[] files = new File(resource.toURI()).listFiles();
		List<Result> results = new ArrayList<>();
		for (File folder : files) {
//			if (folder.getName().equals("Monopoly")) {
				results.add(new QuantitativeEvaluation(folder).evaluate());
//			}
		}
		ResultsWriter.instance.write(results, "results");
	
	}
	private final File folder;
	
	public QuantitativeEvaluation(File folder) {
		this.folder = folder;
	}
	
	private Result evaluate() {
		Map<String, Mutation> mutations = MutationParser.instance.getMutations(new File(folder, "mapping.txt"));
		Collection<HOM> homs = HOMParser.instance.getHOMS(mutations, new File(folder, "homs.txt"));
		if (new File(folder, "failedtests.txt").exists()) {
			TestParser.instance.getTestResults(mutations, new File(folder, "failedtests.txt"));
		}
		Result result = evaluate(mutations, homs);
		if (new File(folder, "stats.txt").exists()) {
			Statistics stats = StatsParser.instance.readStats(new File(folder, "stats.txt"));
			result.setStatistics(stats);
		} else {
			result.setStatistics(new Statistics(-1, -1, -1));
		}
		return result;
	}

	/**
	 * 	Quantitative part (How do SSHOMs look like?)
	 */
	private Result evaluate(Map<String, Mutation> mutations, Collection<HOM> homs) {
		Result result = new Result(folder.getName());
		computeDistributionOfOrders(result, homs);
		computeDistribution(result, homs);
		computeN1OrderRule(homs);
//		computeOperatorUsage(homs);
//		computeCategories(homs);
		computeStatsTable(result, mutations, homs);
		result.setHOMs(homs);
		return result;
	}

	private void computeStatsTable(Result result, Map<String, Mutation> mutations, Collection<HOM> homs) {
		System.out.println("Compute stats table");
		
		System.out.println("FOMs:" + mutations.size());
		System.out.println("SSHOMs:" + homs.size());
		Collection<HOM> homsRed = computeSSHOMsRed(homs);
		System.out.println("SHOM Red: " + homsRed.size());
		
		Map<String, Mutation> mutationsCopy = new HashMap<>(mutations);
		for (HOM hom : homs) {
			for (Mutation m : hom.foms) {
				mutationsCopy.remove(m.name);
			}
		}
		int subsumedFoms = mutations.size() - mutationsCopy.size();
		System.out.println("Subsumed FOMs: " + subsumedFoms);
		System.out.println();
		result.setStats(mutations.size(), homs.size(), homsRed.size(), subsumedFoms);
	}


	private Collection<HOM> computeSSHOMsRed(Collection<HOM> homs) {
		Collection<HOM> copy = new ArrayList<>(homs);
		Collection<HOM> homsRed = new HashSet<>();
		Collection<Mutation> coveredFOMs = new HashSet<>();
		while(!copy.isEmpty()) {
			getMostCoveringHom(homsRed, coveredFOMs, copy);
		}
		return homsRed;
	}


	private void getMostCoveringHom(Collection<HOM> homsRed, Collection<Mutation> coveredFOMs, Collection<HOM> copy) {
		int mostCovered = 0;
		HOM nextHOM = null;
		Iterator<HOM> it = copy.iterator();
		while (it.hasNext()) {
			HOM next = it.next();
			int coverage = 0;
			for (Mutation hom : next.foms) {
				if (!coveredFOMs.contains(hom)) {
					coverage++;
				}
			}
			if (coverage == 0) {
				it.remove();
			} else if (coverage > mostCovered) {
				mostCovered = coverage;
				nextHOM = next;
			}
		}
		
		if (nextHOM != null) {
			homsRed.add(nextHOM);
			coveredFOMs.addAll(nextHOM.foms);
			copy.remove(nextHOM);
		}
	}


	/**
	 * What categories are the homs
	 */
	private void computeCategories(Collection<HOM> homs) {
		System.out.println("Compute categories");
		for (HOM hom : homs) {
			boolean hasZero = false;
			boolean hasNonZero = false;
			int[] succeedingTest = new int[hom.foms.size()];
			int i = 0;
			for (Mutation m : hom.foms) {
				HashSet<String> failedTests = new HashSet<>(m.failedTests);
				for (Mutation m2 : hom.foms) {
					if (m != m2) {
						failedTests.removeAll(m2.failedTests);
					}
				}
				if (failedTests.isEmpty()) {
					hasZero = true;
				} else {
					hasNonZero = true;
				}
				succeedingTest[i++] = failedTests.size();
			}
			if (hasNonZero && !hasZero) {
				System.out.println("TYPE I:   " + hom);
			} else if (!hasNonZero && hasZero) {
				System.out.println("TYPE II:  " + hom);
			} else {
				System.out.println("TYPE III: " + hom);
			}
			System.out.print(Arrays.toString(succeedingTest));
			
			Collection<String> intersection = new HashSet<>();
			for (Mutation m : hom.foms) {
				if (intersection.isEmpty()) {
					intersection.addAll(m.failedTests);
				} else {
					intersection.retainAll(m.failedTests);
				}
			}
			System.out.println("Intersection " + intersection.size());
		}
		System.out.println();
	}


	/**
	 * What mutation operators are more useful in HOMs? Or what combinations generate more HOMs? (frequent item set mining)
	 */
	private void computeOperatorUsage(Collection<HOM> homs) {
		Map<Change, Integer> fomUsage = new HashMap<>();
		
		for (HOM hom : homs) {
			for (Mutation m : hom.foms) {
				fomUsage.compute(m.change, (k,v) -> v == null ? 1 : v + 1);
			}
		}
		
		List<Entry<Change, Integer>> sortedDistribution = new ArrayList<>();
		sortedDistribution.addAll(fomUsage.entrySet());
		Collections.sort(sortedDistribution, (e1, e2) -> e2.getValue() - e1.getValue());
		
		for (Entry<Change, Integer> change : sortedDistribution) {
			System.out.println(change);
		}
		System.out.println();
		System.out.println("compute operator usage combinations");
		Map<Change, Map<Change, Integer>> combinations = new HashMap<>();
		
		for (HOM hom : homs) {
			for (Mutation m1 : hom.foms) {
				for (Mutation m2 : hom.foms) {
					if (m1 != m2) {
						Map<Change, Integer> map;
						if (m1.change.id < m2.change.id) {
							map = combinations.computeIfAbsent(m1.change, v -> new HashMap<>());
							map.compute(m2.change, (k,v) -> v == null ? 1 : v + 1);
						} else {
							map = combinations.computeIfAbsent(m2.change, v -> new HashMap<>());
							map.compute(m1.change, (k,v) -> v == null ? 1 : v + 1);
						}
					}
				}
			}
		}
		
		List<Object[]> combinations2 = new ArrayList<>();
		
		for (Entry<Change, Map<Change, Integer>> change1 : combinations.entrySet()) {
			for (Entry<Change, Integer> change2 : change1.getValue().entrySet()) {
				
				combinations2.add(new Object[]{change1.getKey(), change2.getKey(), change2.getValue()});
			}
		}
		Collections.sort(combinations2, (e1, e2) -> (Integer)e2[2] - (Integer)e1[2]);
		for (Object[] combination : combinations2) {
			System.out.println(combination[0] + "   &   " + combination[1] + " : " + combination[2]);
		}
		System.out.println();
	}


	/**
	 * Is n-order an extension of n-1-order HOM? 
	 * -- or combinations of 2 homs TODO 
	 */
	private void computeN1OrderRule(Collection<HOM> homs) {
		System.out.println("Compute n - 1 rule");
		for (HOM hom : homs) {
			boolean result = containsSubHom(hom, homs);
			if (!result) {
				System.out.println("n - 1 mutant not found for: " + hom);
			}
		}
		System.out.println();
	}


	private boolean containsSubHom(HOM hom, Collection<HOM> homs) {
		if (hom.getOrder() == 2) {
			return true;
		}
		for (HOM other : homs) {
			if (other.getOrder() == hom.getOrder() - 1) {
				int sameMutants = 0;
				for (Mutation fom : hom.foms) {
					if (other.containsFom(fom)) {
						sameMutants++;
					}
				}
				if (sameMutants == hom.getOrder() - 1) {
					return true;					
				}
			}
		}
		
		return false;
	}


	/**
	 * Different class? Different method? (divided by groups and report statistics separately)
	 */
	private void computeDistribution(Result result, Collection<HOM> homs) {
		int sameMethod = 0;
		int differentClass = 0;
		int differentMethod = 0;
		int[] methodDistribution = new int[10];
		int[] classDistribution = new int[10];
		for (HOM hom : homs) {
			Set<String> methods = new HashSet<>();
			Set<String> classes = new HashSet<>();
			
			for (Mutation m : hom.foms) {
				methods.add(m.methodName);
				classes.add(m.className);
			}
			
			if (methods.size() == 1) {
				sameMethod++;
			} else {
				differentMethod++;
			}
			if (classes.size() > 1) {
				differentClass++;
			}
			
			methodDistribution[methods.size()]++;
			classDistribution[classes.size()]++;
		}
		
		System.out.println("Distribution:");
		System.out.println("sameMethod: " + sameMethod);
		System.out.println("differentMethod: " + differentMethod);
		System.out.println("differentClass: " + differentClass);
		System.out.println("Method distribution" + Arrays.toString(methodDistribution));
		System.out.println("Class distribution" + Arrays.toString(classDistribution));
		
		System.out.println();
		
		result.setDistirbutions(methodDistribution, classDistribution);
	}


	/**
	 * What is the distribution of orders?
	 * @param result 
	 */
	private void computeDistributionOfOrders(Result result, Collection<HOM> homs) {
		int maxOrder = 0;
		for (HOM hom : homs) {
			if (hom.getOrder() > maxOrder) {
				maxOrder = hom.getOrder();
			}
		}
		
		int[] distribution = new int[maxOrder +1];
		for (HOM hom : homs) {
			distribution[hom.getOrder()]++;
		}
		result.setOrderDistribution(distribution);
		System.out.println("Distribution or orders: " + Arrays.toString(distribution));
		System.out.println();
	}
}
