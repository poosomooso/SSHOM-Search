package geneticAlgorithm;

import org.junit.runner.Description;
import util.SSHOMListener;
import util.SSHOMRunner;
import util.SetArithmetic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MutationContainer implements Comparable<MutationContainer>{
    private final String[] mutation;
    private final double           fitness;
    private final Set<Description> killedTests;

    public MutationContainer(String[] hom, SSHOMRunner runner,
        Map<String, MutationContainer> foms)
        throws NoSuchFieldException, IllegalAccessException {
        this.mutation = hom;
        if (runner != null) {
            SSHOMListener sshomListener = runner.runJunitOnHOM(hom);
            this.killedTests = sshomListener.getHomTests();
            this.fitness = mutationFitness(foms);
        } else {
            this.fitness = 0.0;
            this.killedTests = null;
        }
    }

    public boolean containsMutation(String s) {
        for (String m : mutation) {
            if (m != null && m.equals(s)) {
                return true;
            }
        }
        return false;
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
            return 0;
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
        return "MutationContainer [fitness=" + fitness + ", killedTests=" + killedTests.size() + ", hom=" + mutation + "]";
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

        return mutation == null ?
            that.mutation == null :
            new HashSet<>(Arrays.asList(this.mutation)).equals(new HashSet<>(Arrays.asList(that.mutation)));
    }

    @Override
    public int hashCode() {
        return new HashSet<>(Arrays.asList(this.mutation)).hashCode();
    }
}
