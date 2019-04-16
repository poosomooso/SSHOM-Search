package benchmark;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import evaluation.analysis.Mutation;
import evaluation.io.MutationParser;
import testRunner.RunTests;
import util.ConditionalMutationWrapper;
import util.SSHOMListener;

public class InfLoopTestProcess {

  private              long                startTime = Integer.MAX_VALUE;
  private final        List<SSHOMListener> listeners = new ArrayList<>();
  private static final int                 TIMEOUT   = 2000;
  private static final int WAIT_FOR_KILL = 500;


  public InfLoopTestProcess(SSHOMListener... listeners) {
    this.listeners.addAll(Arrays.asList(listeners));

  }

  public void runTests(String[] mutants, Deque<String[]> testCases) {
    List<String[]> failedTests = new ArrayList<>();
    ConcurrentLinkedQueue<String[]> testUnderExecution = new ConcurrentLinkedQueue<>();
    while(!testCases.isEmpty()) {
      startTime = System.currentTimeMillis();
      Thread t = new Thread("Test: " + Arrays.toString(mutants)) {
        @Override
        public void run() {
          while(!testCases.isEmpty()) {
            startTime = System.currentTimeMillis();
            String[] next = testCases.peek(); // don't remove yet, so the other thread can see it if it loops infinitely
            testUnderExecution.add(next);
//            System.out.println("Running "+ Arrays.toString(next));
            try {
              boolean passed = process.runSingleTest(next[0], next[1], mutants);
              if (testUnderExecution.size() > 0 && !passed) {
                // System.out.println("f");
                failedTests.add(next);
              }
              if (testUnderExecution.size() > 0 && testUnderExecution.peek() == testCases.peek()) {
                String[] test = testCases.pop(); // we are done, now remove it
//                System.out.println("popped : " + Arrays.toString(test));
                testUnderExecution.poll();
              }
            } catch (ThreadDeath e) {
              // this try catch statement somehow fixed
              // the NoSuchElementException in testCases.pop()
              System.out.println("thread death");
//              System.out.println("popped : "+Arrays.toString(testCases.pop())); // we are done, now remove it

            }
          }
        }
      };
      t.start();
      Thread kill = new Thread("kill " + t.getName()) {
        @SuppressWarnings("deprecation")
        @Override
        public void run() {
          try {
            while (true) {
              String[] last = testCases.peek();
              while (System.currentTimeMillis() - startTime < TIMEOUT) {
                sleep(TIMEOUT);
              }
              if (t.isAlive()) {
                String[] current = testCases.peek();
                if (last != null && current != null && current[0].equals(last[0]) && current[1].equals(last[1])) {
                  t.stop();
                  failedTests.add(testCases.peek()); // avoiding the case where it pops twice; should be ok if it registers as a failure twice
                  System.out.println("t");
                  String[] killed = testCases.pop();
                  testUnderExecution.poll();
                  System.out.println("popped in kill: "+Arrays.toString(killed)); // we are done, now remove it
                  while (testUnderExecution.size() == 0) {
                    sleep(WAIT_FOR_KILL);
                  }
                }
              }
            }
          } catch (InterruptedException e) {
            // nothing here
          } catch (NoSuchElementException e) {}
        }
      };
      kill.start();
      try {
        t.join();
        kill.interrupt();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    for (String[] test : new ArrayList<>(failedTests)) {
      registerFailure(test[0], test[1]);
    }
  }

	public boolean runSingleTest(String testClass, String testMethod, String... mutants) {
		Class<?> tClass = null;
		try {
			tClass = Class.forName(testClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		String testName = testMethod;
		ConditionalMutationWrapper cmw = new ConditionalMutationWrapper(BenchmarkPrograms.getTargetClasses());
		cmw.resetMutants();
		for (int i = 0; i < mutants.length; i++) {
			cmw.setMutant(mutants[i]);
		}
		RunTests.print = false;
		listener.testStarted(testClass, testName);
    Optional<Boolean> testPassed = RunTests.runTest(tClass, testName);
		if (testPassed.isPresent() && !testPassed.get()) {
		  return false;
    }
    return true;
	}

  private void registerFailure(String testClass, String testMethod) {
    for (SSHOMListener listener : this.listeners) {
      try {
        Failure failure = new Failure(
            Description.createTestDescription(testClass, testMethod), null);
        listener.testFailure(failure);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    System.out.println("failtest "+testClass+","+testMethod);
  }


  static SSHOMListener listener = new SSHOMListener();

  private static InfLoopTestProcess process  = new InfLoopTestProcess(listener);
  private static final Deque<String[]> testCases = new ArrayDeque<>();

  public static SSHOMListener getFailedTests(Class<?>[] testClasses,
      String[] mutants) {
    //very jank check to use another class for chess
    //since chess leaks memory
    if (BenchmarkPrograms.PROGRAM == BenchmarkPrograms.PROGRAM.CHESS) {
      SSHOMListener l = ChessInfLoopTestProcess.runTests(testClasses, mutants);
      return l;
    }

    listener.signalHOMBegin();

    for (Class<?> c : testClasses) {
      for (Method m : c.getMethods()) {
        if (m.getAnnotation(Test.class) != null) {
        	testCases.add(new String[]{c.getName(), m.getName()});
        }
      }
    }

    process.runTests(mutants, testCases);

    listener.signalHOMEnd();
    return listener;
  }
  
  private static Map<String, Mutation> mutations = MutationParser.instance.getMutations(new File("bin/evaluationfiles/" + BenchmarkPrograms.PROGRAM.toString().toLowerCase(), "mapping.txt"));

	public static SSHOMListener getFailedTests(Class<?>[] testClasses, Map<String, Set<String>> testMap, String[] mutants) {
		//very jank check to use another class for chess
	    //since chess leaks memory
	    if (BenchmarkPrograms.PROGRAM == BenchmarkPrograms.PROGRAM.CHESS) {
	      SSHOMListener l = ChessInfLoopTestProcess.runTests(testClasses, mutants);
	      return l;
	    }
	
	    listener.signalHOMBegin();
	
	    // testname, methodName
	    Set<String> testsToRun = new HashSet<>();
	    for (String mName : mutants) {
			Mutation mutation = mutations.get(mName);
			for (Entry<String, Set<String>> entry : testMap.entrySet()) {
				if (entry.getValue().contains(mutation.className + "." + mutation.methodName)) {
					testsToRun.add(entry.getKey());
				}
			}
		}
	    
	    int ignoredTests = 0;
	    for (Class<?> c : testClasses) {
	      for (Method m : c.getMethods()) {
	        if (m.getAnnotation(Test.class) != null) {
	        	if (testsToRun.contains(c.getName() + "." + m.getName())) {
	        		testCases.add(new String[]{c.getName(), m.getName()});
	        	} else {
	        		ignoredTests++;
	        	}
	        }
	      }
	    }
	    System.out.println(ignoredTests + " ignored " + testCases.size() + " run");
	    
	    process.runTests(mutants, testCases);
	
	    listener.signalHOMEnd();
	    return listener;
	}
}
