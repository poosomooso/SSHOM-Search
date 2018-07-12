package benchmark;

import geneticAlgorithm.MutationContainer;
import geneticAlgorithm.RandomUtils;
import util.SSHOMRunner;

import java.util.*;

public class BenchmarkedEvolutionarySSHOMFinder {
    private static final int MIN_ORDER = 2;
    private static final int MAX_ORDER = 33; //max mutations

    private Set<MutationContainer> seenMutations = new HashSet<>();
    private Benchmarker benchmarker;
    private String[]                       allFOMs;
    private SSHOMRunner                    testRunner;
    private Map<String, MutationContainer> fomFitness;

    public BenchmarkedEvolutionarySSHOMFinder() {
        this.benchmarker = new Benchmarker();
    }

    public void geneticAlgorithm(Class[] targetClasses, Class[] testClasses)
        throws NoSuchFieldException, IllegalAccessException {
        benchmarker.start();

        this.testRunner = new SSHOMRunner(targetClasses, testClasses);
        this.allFOMs = testRunner.getMutants().toArray(new String[0]);

        // doing this because I can't get stream to work?
        this.fomFitness = new HashMap<>();
        for (String m : testRunner.getMutants()) {
            this.fomFitness.put(m,
                new MutationContainer(new String[] { m }, testRunner, null));
        }

        System.out.println("Generated FOMs");

        // actual algorithm
        int populationSize = 10000;
        double percentDiscarded = 1.0 / 3.0; // TODO: properties file

        int numIters = 10;

        //generate some homs based on foms
        MutationContainer[] homPopulation = genHOMs(2, populationSize);

        System.out.println("Generated HOMs");

        for (int i = 0; i < numIters; i++) {
            Arrays.sort(homPopulation);

            int j = 0;
            while (j < homPopulation.length && homPopulation[j].getFitness() <= 1.0) {
                if (!seenMutations.contains(homPopulation[j])) {
                    benchmarker.timestamp(String.join(",", homPopulation[j].getMutation()));
                }
            }
            seenMutations.addAll(Arrays.asList(homPopulation));

            int numDiscarded = Math.max(
                arrLastIndexOf(homPopulation), (int) (populationSize * percentDiscarded));
            int numCrossovers = crossover(homPopulation, numDiscarded);
            mutate(homPopulation, numCrossovers, numDiscarded);
        }

    }

    private int arrLastIndexOf(MutationContainer[] homPopulation) {
        for (int i = homPopulation.length - 1; i >= 0; i--) {
            if (!homPopulation[i].hasValidFitness()) {
                return i;
            }
        }
        return 0;
    }

    public int crossover(MutationContainer[] sortedHOMS, int numDiscarded)
        throws NoSuchFieldException, IllegalAccessException {
        int i = 0;
        while (i < numDiscarded / 2) {
            int parentIndex1 = RandomUtils
                .randRange(numDiscarded, sortedHOMS.length);
            int parentIndex2 = RandomUtils
                    .randRange(numDiscarded, sortedHOMS.length);

            MutationContainer parent1 = sortedHOMS[parentIndex1];
            MutationContainer parent2 = sortedHOMS[parentIndex2];

            MutationContainer[] children = crossoverParents(parent1, parent2);
            if (!seenMutations.contains(children[0])) {
                sortedHOMS[i++] = children[0];
            }
            if (!seenMutations.contains(children[1])) {
                sortedHOMS[i++] = children[1];
            }
        }
        return i;
    }

    public void mutate(MutationContainer[] sortedHOMS, int startIndex, int numDiscarded)
        throws NoSuchFieldException, IllegalAccessException {
        int i = startIndex;
        while (i < numDiscarded) {
            int parentIndex = RandomUtils
                .randRange(numDiscarded, sortedHOMS.length);
            MutationContainer parent = sortedHOMS[parentIndex];
            MutationContainer m = randomlyMutate(parent);
            if (!seenMutations.contains(m)) {
                sortedHOMS[i++] = m;
            }
        }
    }


    public MutationContainer randomlyMutate(MutationContainer hom)
        throws NoSuchFieldException, IllegalAccessException {
        String[] newMutation;
        String[] oldMutation = hom.getMutation();
        if ((oldMutation.length >= MAX_ORDER) ||
            (oldMutation.length > MIN_ORDER && Math.random() < 0.5)) { //delete fom
            int deletedIndex = RandomUtils.randRange(0, oldMutation.length);
            newMutation = deleteFOM(hom, deletedIndex);

        } else { //add fom
            newMutation = addFOM(hom,
                generateRandomUnusedFOM(hom, this.allFOMs));
        }

        return new MutationContainer(newMutation, this.testRunner, this.fomFitness);
    }

    public MutationContainer[] crossoverParents(MutationContainer a, MutationContainer b)
        throws NoSuchFieldException, IllegalAccessException {
        int randIndex1;
        int randIndex2;
        try {
            randIndex1 = generateRandomUnusedFOM(b, a.getMutation());
            randIndex2 = generateRandomUnusedFOM(a, b.getMutation());
        } catch(RuntimeException e) {
            return new MutationContainer[] { randomlyMutate(a),
                randomlyMutate(b) };
        }

        String[] child1 = a.getMutation().clone();
        String[] child2 = b.getMutation().clone();

        child1[randIndex1] = b.getMutation()[randIndex2];
        child2[randIndex2] = a.getMutation()[randIndex1];

        return new MutationContainer[] {
            new MutationContainer(child1, this.testRunner, this.fomFitness),
            new MutationContainer(child2, this.testRunner, this.fomFitness) };
    }

    protected String[] deleteFOM(MutationContainer hom, int deletedIndex) {
        String[] newHom = new String[hom.getMutation().length-1];
        for (int i = 0; i < deletedIndex; i++) {
            newHom[i] = hom.getMutation()[i];
        }
        for (int i = deletedIndex; i < newHom.length; i++) {
            newHom[i] = hom.getMutation()[i + 1];
        }
        return newHom;
    }

    protected String[] addFOM(MutationContainer hom, int fomToAdd) {
        String[] newHom = Arrays.copyOf(hom.getMutation(), hom.getMutation().length+1);
        newHom[hom.getMutation().length] = allFOMs[fomToAdd];
        return newHom;
    }

    private int generateRandomUnusedFOM(MutationContainer hom, String[] fomList) {
        int newFOMIndex;
        int attempts = 0;
        do {
            if (attempts++ > fomList.length) {
                throw new RuntimeException(
                    "Took too many attempts to generate a random FOM. Try changing the max order of your HOMs to something less than "
                        + fomList.length);
            }
            newFOMIndex = RandomUtils.randRange(0, fomList.length);
        } while (hom.containsMutation(fomList[newFOMIndex]));
        return newFOMIndex;
    }

    protected MutationContainer[] genHOMs(int maxOrder, int numHOMs)
        throws NoSuchFieldException, IllegalAccessException {
        if (maxOrder <= 0) {
            throw new IllegalArgumentException(
                "The max order of higher order mutations must be greater than 0.");
        }
        MutationContainer[] homs = new MutationContainer[numHOMs];
        int i = 0;
        while (i < numHOMs) {
            int order = 3; //RandomUtils.randRange(MIN_ORDER, maxOrder+1);
            String[] newHOM = new String[order];

            for (int j = 0; j < order; j++) {
                int mutationIdx = generateRandomUnusedFOM(new MutationContainer(newHOM, null, null), allFOMs);
                newHOM[j] = this.allFOMs[mutationIdx];
            }

            MutationContainer container = new MutationContainer(newHOM,
                this.testRunner, this.fomFitness);
//            if (container.hasValidFitness() || i > numHOMs/2) {
//                homs[i++] = container;
//            }
            homs[i++] = container;
        }
        return homs;
    }
}
