package benchmark;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;
import org.junit.runner.Computer;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import evaluation.analysis.Mutation;
import evaluation.io.MutationParser;
import testRunner.RunTests;
import util.ConditionalMutationWrapper;
import util.SSHOMListener;

public class InfLoopTestProcess {

  private static final long TEST_INIT_TIMEOUT = 100;
  private static final int TIME_OUT_MULTIPLIER = 2;
  private static final long MIN_TIMEOUT = 100;
  
  private              long                startTime = Integer.MAX_VALUE;
  private final        List<SSHOMListener> listeners = new ArrayList<>();
  private static final int                 TIMEOUT   = 30 * 1000;
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
//                 System.out.println("f");
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
        @Override
        public void run() {
          try {
            while (true) {
              String[] last = testCases.peek();
              while (System.currentTimeMillis() - startTime < TIMEOUT) {
                sleep(TIMEOUT);
                InfLoopTestProcess.timedOut = true;
              }
              if (t.isAlive()) {
                String[] current = testCases.peek();
                if (last != null && current != null && current[0].equals(last[0]) && current[1].equals(last[1])) {
                  t.interrupt();
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
          } catch (NoSuchElementException e) {
              // nothing here
          } finally {
        	  InfLoopTestProcess.timedOut = false;
		}
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
		if (Modifier.isAbstract(tClass.getModifiers()) || tClass.getAnnotation(Deprecated.class) != null) {
			return true;
		}
		
		String testName = testMethod;
		ConditionalMutationWrapper cmw = new ConditionalMutationWrapper(BenchmarkPrograms.getTargetClasses());
		cmw.resetMutants();
		for (int i = 0; i < mutants.length; i++) {
			cmw.setMutant(mutants[i]);
		}
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

  private static final Map<Description, Long> testTimes = new HashMap<>();
  
	public static SSHOMListener getFailedTests(Class<?>[] testClasses, String[] mutants) {
		listener.signalHOMBegin();
		if (Flags.JUNIT_CORE) {
			JUnitCore junitCore = new JUnitCore();
			junitCore.addListener(listener);
			junitCore.addListener(timeRecorder(testTimes));
			junitCore.run(Request.classes(Computer.serial(), testClasses));
		} else {
			// very jank check to use another class for chess
			// since chess leaks memory
			if (BenchmarkPrograms.PROGRAM == BenchmarkPrograms.PROGRAM.CHESS) {
				SSHOMListener l = ChessInfLoopTestProcess.runTests(testClasses, mutants);
				return l;
			}
			for (Class<?> c : testClasses) {
				for (Method m : c.getMethods()) {
					if (m.getAnnotation(Test.class) != null) {
						testCases.add(new String[] { c.getName(), m.getName() });
					}
				}
			}
			process.runTests(mutants, testCases);
		}

		listener.signalHOMEnd();
		return listener;
	}

	private static RunListener timeRecorder(Map<Description, Long> times) {
		return new RunListener() {
			int testCount = 0;
			long startTime = 0;

			@Override
			public void testFinished(Description description) throws Exception {
				long runtime = System.currentTimeMillis() - startTime;
				System.out.println(" " + runtime + "ms");
				times.put(description, runtime);
			}

			@Override
			public void testStarted(Description description) throws Exception {
				startTime = System.currentTimeMillis();
				System.out.print(testCount++ + " " + description.getClassName() + " " + description.getMethodName());
			}
			
		};
	}
  
	public static volatile boolean timedOut = false;
	private static final Map<String, Mutation> mutations = MutationParser.instance.getMutations(new File("bin/evaluationfiles/" + BenchmarkPrograms.PROGRAM.toString().toLowerCase(), "mapping.txt"));
	
	public static SSHOMListener getFailedTests(Class<?>[] testClasses, Map<String, Set<String>> testMap,
			String[] mutants) {
		listener.signalHOMBegin();
		final Set<String> testsClassesToRun = new HashSet<>();
		final Set<String> testsToRun = new HashSet<>();
		if (Flags.COVERAGE) {
			for (String mName : mutants) {
				Mutation mutation = mutations.get(mName);
				for (Entry<String, Set<String>> entry : testMap.entrySet()) {
					if (entry.getKey() != null && entry.getValue().contains(mutation.className + "." + mutation.methodName)) {
						testsToRun.add(entry.getKey());
						testsClassesToRun.add(entry.getKey().substring(0, entry.getKey().lastIndexOf(".")));
					}
				}
			}
		}
		
		if (Flags.JUNIT_CORE) {
			runJWithUnit(testClasses, mutants, testsClassesToRun, testsToRun);
		} else {
			// very jank check to use another class for chess
			// since chess leaks memory
			if (BenchmarkPrograms.PROGRAM == BenchmarkPrograms.PROGRAM.CHESS) {
				SSHOMListener l = ChessInfLoopTestProcess.runTests(testClasses, mutants);
				return l;
			}
		    // testname, methodName
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
		    if (!testCases.isEmpty()) {
			    System.out.println(ignoredTests + " ignored " + testCases.size() + " run");
			    process.runTests(mutants, testCases);
		    }
		}
	    listener.signalHOMEnd();
	    return listener;
	}
	
	private static Thread mainThread;
	private static Thread timeoutThread;
	
	private static void runJWithUnit(Class<?>[] testClasses, String[] mutants, final Set<String> testsClassesToRun,
			final Set<String> testsToRun) {
		if (!Flags.COVERAGE || !testsToRun.isEmpty()) {
			System.out.print(Arrays.toString(mutants) + " " + testsToRun.size() + " tests ");
			System.out.flush();
			ConditionalMutationWrapper cmw = new ConditionalMutationWrapper(BenchmarkPrograms.getTargetClasses());
			cmw.resetMutants();
			for (int i = 0; i < mutants.length; i++) {
				cmw.setMutant(mutants[i]);
			}
			mainThread = Thread.currentThread();
		    JUnitCore junitCore = new JUnitCore();
		    junitCore.addListener(listener);
			junitCore.addListener(new TimeOutListener(testTimes));
		    
		    Request request = Request.classes(Computer.serial(), testClasses).filterWith(new Filter() {

				@Override
				public boolean shouldRun(Description description) {
					if (!Flags.COVERAGE) {
						return true;
					}
					if (description.getMethodName() == null) {
						return testsClassesToRun.contains(description.getClassName());
					}
					return testsToRun.contains(description.getClassName() + "." + description.getMethodName());
				}

				@Override
				public String describe() {
					return "HOM filter";
				}
				
			});
		    
		    long start = System.currentTimeMillis();
		    junitCore.run(request);
		    timeoutThread.interrupt();
		    try {
				timeoutThread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		    timeoutThread = null;
		    long end = System.currentTimeMillis();
		    System.out.println(end - start + "ms");
		}
	}
	
	 private static class TimeOutListener extends RunListener {
	    	private final Map<Description, Long> testTimes;

			public TimeOutListener(Map<Description, Long> testTimes) {
				this.testTimes = testTimes;
			}

			@Override
	    	public void testRunStarted(Description description) throws Exception {
	    		if (timeoutThread == null) {
	    			timedOut = false;
		    		timeoutThread = new TimeOutThread(TEST_INIT_TIMEOUT);
					timeoutThread.start();
	    		}
	    	}
	    	
			@Override
			public void testStarted(Description description) throws Exception {
				timeoutThread.interrupt();
				timedOut = false;
				timeoutThread = new TimeOutThread(computeTimeout(description));
				timeoutThread.start();
			}
			
			@Override
			public void testFailure(Failure failure) throws Exception {
				timeoutThread.interrupt();
				timedOut = false;
			}
			
			@Override
			public void testFinished(Description description) throws Exception {
				timeoutThread.interrupt();
				timedOut = false;
				timeoutThread = new TimeOutThread(TEST_INIT_TIMEOUT);
	    		timeoutThread.start();
			}
	    	
			private long computeTimeout(Description description) {
				return testTimes.get(description) * TIME_OUT_MULTIPLIER + MIN_TIMEOUT;
			}
	    }
	
	private static class TimeOutThread extends Thread {

		private final long timeout;

		public TimeOutThread(long timeout) {
			this.timeout = timeout;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(timeout);
				timedOut = true;
				mainThread.interrupt();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
