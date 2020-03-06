package benchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import benchmark.Flags.GRANULARITY;

import java.util.Map.Entry;

import geneticAlgorithm.MutationContainer;
import geneticAlgorithm.RandomUtils;
import util.SSHOMRunner;

public class BenchmarkedEvolutionarySSHOMFinder {
    private static final int MIN_ORDER = 2;
    private static final int MAX_ORDER = 40; //max mutations

    public static final boolean DEBUG_GA = false;

    private Set<MutationContainer> seenMutations = new HashSet<>();
    private String[]                       allFOMs;
    private SSHOMRunner                    testRunner;
    @SuppressWarnings("serial")
	private Map<String, MutationContainer> fomFitness = new HashMap<String, MutationContainer>() {

    	/**
    	 * Lazy initialization of FOMS.
    	 */
		@Override
    	public MutationContainer get(Object o) {
			if (!(o instanceof String)) {
				throw new RuntimeException(o.toString());
			}
			String m = (String) o;
    		MutationContainer fomEntry = super.get(m);
    		if (fomEntry == null) {
    			try {
    				fomEntry = MutationContainer.create(new String[] { m }, testRunner, null, testClasses);
					put((String) m, fomEntry);
				} catch (NoSuchFieldException | IllegalAccessException e) {
					e.printStackTrace();
				}
    		}
    		return fomEntry;
    	}
    	
    };
    private Class<?>[] testClasses;


    public void evolutionarySSHOMFinder()
        throws NoSuchFieldException, IllegalAccessException {
    	Benchmarker.instance.start();
    	if (Flags.getGranularity() != GRANULARITY.ALL) {
    		throw new RuntimeException("granularity not implemented for GA: " + Flags.getGranularity());
    	}

        Class<?>[] targetClasses = BenchmarkPrograms.getTargetClasses();
        Class<?>[] testClasses = BenchmarkPrograms.getTestClasses();

		this.testClasses = testClasses;
        this.testRunner = new SSHOMRunner(targetClasses, testClasses);
        InfLoopTestProcess.createTestCovereage(testClasses);
        InfLoopTestProcess.setTimeOutListener();

        // actual algorithm
        int populationSize = 5000;
        double percentDiscarded = 1.0 / 3.0; // TODO: properties file
        int numIters = 10000;
        
        Map<String, Set<String>> groupMutants = BenchmarkPrograms.createMutationGroups();
        for (Entry<String, Set<String>> groupEntry : groupMutants.entrySet()) {
			long startTime = System.currentTimeMillis();
			this.allFOMs = groupEntry.getValue().toArray(new String[0]);
	        geneticAlgorithm(populationSize, percentDiscarded, numIters, startTime);
        }
    }

    private int geneticAlgorithm(int populationSize, double percentDiscarded,
        int numIters, long startTime) throws NoSuchFieldException, IllegalAccessException {
        int sshomsFound = 0;
        //generate some homs based on foms
        MutationContainer[] homPopulation = genHOMs(3, populationSize);

        Benchmarker.instance.timestamp("Generated HOMs");

        for (int i = 0; i < numIters; i++) {
            Benchmarker.instance.timestamp("GENERATION " + i);
            Arrays.sort(homPopulation);

            double epsilon = (populationSize * percentDiscarded);
            if (Math.abs(seenMutations.size() - (100 * 100)) < epsilon)
                Benchmarker.instance.timestamp(
                    "genetic algorithm has seen " + seenMutations.size()
                        + " mutations");

            int numDiscarded = Math.max(
                lastValidIndex(homPopulation), (int) (populationSize * percentDiscarded));
            int numCrossovers = crossover(homPopulation, numDiscarded);
            mutate(homPopulation, numCrossovers, numDiscarded);
        }
        return sshomsFound;
    }

    private int lastValidIndex(MutationContainer[] homPopulation) {
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

            List<MutationContainer> children = crossoverParents(parent1, parent2);
            for (MutationContainer child : children) {
            	 if (!seenMutations.contains(child)) {
                     sortedHOMS[i++] = child;
                     seenMutations.add(child);
                 } else if (DEBUG_GA) {
                     System.out.println("x-over: Generated duplicate or invalid mutant");
                 }
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
            if (!seenMutations.contains(m) && BenchmarkPrograms.homIsValid(m.getMutation())) {
                sortedHOMS[i++] = m;
                seenMutations.add(m);
            }  else if (DEBUG_GA) {
                System.out.println("mutate: Generated duplicate or invalid mutant");
            }
        }
    }

    public MutationContainer randomlyMutate(MutationContainer hom)
        throws NoSuchFieldException, IllegalAccessException {
    	while (true) {
	        String[] newMutation;
	        String[] oldMutation = hom.getMutation();
	        if ((oldMutation.length >= MAX_ORDER) ||
	            (oldMutation.length > MIN_ORDER && Math.random() < 0.5)) { //delete fom
	            int deletedIndex = RandomUtils.randRange(0, oldMutation.length);
	            newMutation = deleteFOM(hom, deletedIndex);
	
	        } else { //add fom
	            newMutation = addFOM(hom,
	                generateRandomUnusedFOM(hom.getMutation(), this.allFOMs));
	        }
	        if (BenchmarkPrograms.homIsValid(newMutation)) {
	        	return MutationContainer.create(newMutation, this.testRunner, this.fomFitness, this.testClasses);
	        }
    	}
    }
    	

    public List<MutationContainer> crossoverParents(MutationContainer a, MutationContainer b)
        throws NoSuchFieldException, IllegalAccessException {
    	List<MutationContainer> children = new ArrayList<>(2);
        int randIndex1;
        int randIndex2;
        try {
            randIndex1 = generateRandomUnusedFOM(b.getMutation(), a.getMutation());
            randIndex2 = generateRandomUnusedFOM(a.getMutation(), b.getMutation());
        } catch(RuntimeException e) {
        	children.add(randomlyMutate(a));
        	children.add(randomlyMutate(b));
        	return children;
        }

        String[] child1 = a.getMutation().clone();
        String[] child2 = b.getMutation().clone();

        child1[randIndex1] = b.getMutation()[randIndex2];
        child2[randIndex2] = a.getMutation()[randIndex1];

        
        if (BenchmarkPrograms.homIsValid(child1)) {
        	children.add(MutationContainer.create(child1, this.testRunner, this.fomFitness, this.testClasses));
        }
        if (BenchmarkPrograms.homIsValid(child2)) {
        	children.add(MutationContainer.create(child2, this.testRunner, this.fomFitness, this.testClasses));
        }
        return children;
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

    private int generateRandomUnusedFOM(String[] hom, String[] fomList) {
        int newFOMIndex;
        int attempts = 0;
        do {
            if (attempts++ > fomList.length) {
                throw new RuntimeException(
                    "Took too many attempts to generate a random FOM. Try changing the max order of your HOMs to something less than "
                        + fomList.length);
            }
            newFOMIndex = RandomUtils.randRange(0, fomList.length);
        } while (containsMutation(hom, fomList[newFOMIndex]));
        return newFOMIndex;
    }

    private boolean containsMutation(String[] mutList, String mutation) {
        for (String m : mutList) {
            if (m != null && m.equals(mutation)) {
                return true;
            }
        }
        return false;
    }

    protected MutationContainer[] genHOMs(int order, int numHOMs)
        throws NoSuchFieldException, IllegalAccessException {
        if (order <= 0) {
            throw new IllegalArgumentException(
                "The max order of higher order mutations must be greater than 0.");
        }

        Set<MutationContainer> homsSet = new HashSet<>();
        int i = 0;
        while (i < numHOMs) {
            String[] newHOM = new String[order];

            for (int j = 0; j < order; j++) {
                int mutationIdx = generateRandomUnusedFOM(newHOM, allFOMs);
                newHOM[j] = this.allFOMs[mutationIdx];
            }

            if (BenchmarkPrograms.homIsValid(newHOM)) {
                MutationContainer container = MutationContainer.create(newHOM,
                    this.testRunner, this.fomFitness, this.testClasses);

                if (homsSet.add(container)) {
                    i++;
//                    System.out.println(i);
                } else if (DEBUG_GA) {
                    System.out.println("init: Generated duplicate mutant");
                }
            } else if (DEBUG_GA) {
                System.out.println("init: Generated invalid mutant");
            }
        }
        seenMutations.addAll(homsSet);
        MutationContainer[] homs = homsSet.toArray(new MutationContainer[0]);
        return homs;
    }
}
