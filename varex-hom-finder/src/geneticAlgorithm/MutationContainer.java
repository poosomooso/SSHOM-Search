package geneticAlgorithm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.runner.Description;

import benchmark.BenchmarkPrograms;
import benchmark.Benchmarker;
import benchmark.InfLoopTestProcess;
import util.SSHOMListener;
import util.SSHOMRunner;
import util.SetArithmetic;

import static benchmark.BenchmarkedEvolutionarySSHOMFinder.DEBUG_GA;

public class MutationContainer implements Comparable<MutationContainer>{
    private final String[] mutation;
    private final double           fitness;
    private final Set<Description> killedTests;
    private int hashCode = Integer.MAX_VALUE;
    
    private static final Map<String, MutationContainer> mutationCache = new HashMap<>();
    
    public static MutationContainer create(String[] hom, SSHOMRunner runner,
            Map<String, MutationContainer> foms, Class<?>[] testClasses) throws NoSuchFieldException, IllegalAccessException {
    	Arrays.sort(hom);
    	
    	final String key = Arrays.toString(hom);
    	MutationContainer mutationContainter = mutationCache.get(key);
    	if (mutationContainter == null) {
    		mutationContainter =  new MutationContainer(hom, runner, foms, testClasses);
    		mutationCache.put(key, mutationContainter);
    	}
    	return mutationContainter;
    }
    
    private static int sshomsFound = 0;

    private MutationContainer(String[] hom, SSHOMRunner runner,
        Map<String, MutationContainer> foms, Class<?>[] testClasses)
        throws NoSuchFieldException, IllegalAccessException {
    	if (!BenchmarkPrograms.homIsValid(hom)) {
    		throw new RuntimeException("invlid");
    	}
        this.mutation = hom;
        Arrays.sort(this.mutation);
        if (runner != null) {
            SSHOMListener sshomListener;
            if (DEBUG_GA) System.out.println("Running tests on mutant " + Arrays.toString(hom));

            if (BenchmarkPrograms.programHasInfLoops()) {
                sshomListener = InfLoopTestProcess
                    .getFailedTests(testClasses, hom);
            } else  {
                sshomListener = runner.runJunitOnHOM(hom);
            }
            this.killedTests = sshomListener.getHomTests();
            this.fitness = mutationFitness(foms);
            
            if (this.fitness <= 1 && this.fitness > 0 && BenchmarkPrograms.homIsValid(hom)) {
	            Benchmarker.instance.timestamp(++sshomsFound + " " + String.join(",", getMutation()) + " fitness: " + this.fitness);
            }
        } else {
            this.fitness = 0.0;
            this.killedTests = null;
        }
    }

    public String[] getMutation() {
        return mutation;
    }

    public double getFitness() {
        return fitness;
    }

    public boolean hasValidFitness() {
        return !(Double.isInfinite(fitness) || Math.abs(fitness) < 1e-10);
    }

    /**
     * This is the reverse of the natural ordering for doubles, such that stronger
     * fitness values will be considered larger than wekaer fitness values.
     * Fitness values are stronger the closer to 0 it gets; however, 0 is the
     * weakest fitness value.
     * @param o
     * @return
     */
    @Override
    public int compareTo(MutationContainer o) {
        if (fitness < 0 || o.fitness < 0) {
            throw new IllegalStateException("Fitness values cannot be less than 0");
        }
        double epsilon = 1e-10;
        if (Math.abs(fitness - o.fitness) < epsilon) {
            // optimize for the smaller one
//            return 0;
            return Integer.compare(o.mutation.length, mutation.length);
        }
        else if (Math.abs(fitness) < epsilon) {
            return -1;
        } else if (Math.abs(o.fitness) < epsilon) {
            return 1;
        }
        return -(Double.compare(fitness, o.fitness));
    }

    @Override
    public String toString() {
        return "MutationContainer [fitness=" + fitness + ", killedTests=" + killedTests.size() + ", hom=" + Arrays.toString(mutation) + "]";
    }

    public double mutationFitness(Map<String, MutationContainer> foms) {
        if (mutation.length <= 1) {
            return 0.0;
        }
        Set<Description> fomKilledTests = null;
        for (String m : mutation) {
            if (fomKilledTests == null) {
                fomKilledTests = new HashSet<>(foms.get(m).killedTests);
            } else {
                // intersect
                fomKilledTests.retainAll(foms.get(m).killedTests);
            }
        }

        double fomNumKilled = fomKilledTests.size();
        double homNumOverlappingKilled = SetArithmetic.getIntersectionSize(this.killedTests, fomKilledTests);
        double homNumNonoverlappingKilled = SetArithmetic.getDifferenceSize(this.killedTests, fomKilledTests);

        if (homNumNonoverlappingKilled > 0) {
            double overlapRatio = homNumNonoverlappingKilled / homNumOverlappingKilled;
                //Math.max(homNumOverlappingKilled, 1);
            return 1.0 + overlapRatio;
        } else {
            return homNumOverlappingKilled / fomNumKilled;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MutationContainer that = (MutationContainer) o;

        return Arrays.equals(this.mutation, that.mutation);
    }

    @Override
    public int hashCode() {
        if (hashCode == Integer.MAX_VALUE) {
            hashCode = Arrays.hashCode(mutation);
        }
        return hashCode;
    }
}
