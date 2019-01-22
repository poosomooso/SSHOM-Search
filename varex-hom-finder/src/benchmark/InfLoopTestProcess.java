package benchmark;

import java.lang.reflect.Method;
import java.util.*;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import testRunner.RunTests;
import util.ConditionalMutationWrapper;
import util.SSHOMListener;

public class InfLoopTestProcess {

  private              long                startTime = Integer.MAX_VALUE;
  private final        List<SSHOMListener> listeners = new ArrayList<>();
  private static final int                 TIMEOUT   = 1000;


  public InfLoopTestProcess(SSHOMListener... listeners) {
    this.listeners.addAll(Arrays.asList(listeners));

  }

  public void runTests(String[] mutants, Deque<String[]> testCases) {
    List<String[]> failedTests = new ArrayList<>();
    while(!testCases.isEmpty()) {
      startTime = System.currentTimeMillis();
      Thread t = new Thread("Test: " + Arrays.toString(mutants)) {
        @Override
        public void run() {
          while(!testCases.isEmpty()) {
            startTime = System.currentTimeMillis();
            String[] next = testCases.peek(); // don't remove yet, so the other thread can see it if it loops infinitely
            boolean passed = process.runSingleTest(next[0], next[1], mutants);
            if (!passed) {
              System.out.println("f");
              failedTests.add(next);
            }
            testCases.pop(); // we are done, now remove it
          }
        }
      };
      t.start();
      Thread kill = new Thread("kill " + t.getName()) {
        @SuppressWarnings("deprecation")
        @Override
        public void run() {
          try {
            while (System.currentTimeMillis() - startTime < TIMEOUT) {
              sleep(TIMEOUT);
            }
            if (t.isAlive()) {
              t.stop();
              failedTests.add(testCases.pop());
              System.out.println("t");
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
    Optional<Boolean> testPassed = RunTests.runTest(tClass, testName);
		if (testPassed.isPresent() && !testPassed.get()) {
		  return false;
    }
    return true;
	}

  private void registerFailure(String testClass, String testMethod) {
    for (SSHOMListener listener : this.listeners) {
      try {
        listener.testFailure(new Failure(
            Description.createTestDescription(testClass, testMethod), null));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


  private static SSHOMListener      listener = new SSHOMListener();

  private static InfLoopTestProcess process  = new InfLoopTestProcess(listener);
  private static final Deque<String[]> testCases = new ArrayDeque<>();



  public static SSHOMListener getFailedTests(Class<?>[] testClasses,
      String[] mutants) {
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
}
