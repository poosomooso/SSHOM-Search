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


  private long startTime = Integer.MAX_VALUE;
  private final        List<SSHOMListener>   listeners   = new ArrayList<>();

  public InfLoopTestProcess(SSHOMListener... listeners) {
    this.listeners.addAll(Arrays.asList(listeners));

  }

  public void runTests(String[] mutants, Deque<String[]> testCases) {
    while(!testCases.isEmpty()) {
      final boolean[] testPassed = new boolean[1];
      String[] next = InfLoopTestProcess.testCases.pop();
      Thread t = new Thread("Test: " + Arrays.toString(mutants)) {
        @Override
        public void run() {
          while(!InfLoopTestProcess.testCases.isEmpty()) {
            startTime = System.currentTimeMillis();
            testPassed[0] = process.runSingleTest(next[0], next[1], mutants);
            if (!testPassed[0]) System.out.println("f");
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
              testPassed[0] = false;
              System.out.println("t");
            }
          } catch (InterruptedException e) {
            // nothing here
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
      if (!testPassed[0]) registerFailure(next[0], next[1]);

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
    System.out.println(testClass);
    System.out.println(testMethod);
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
  private static final int TIMEOUT = 50;
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
