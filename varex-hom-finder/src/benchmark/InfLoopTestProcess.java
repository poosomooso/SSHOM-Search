package benchmark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.zip.Inflater;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import org.junit.runners.model.TestClass;
import testRunner.SetHomTestRunner;
import util.SSHOMListener;

public class InfLoopTestProcess {

  private static final int                   MAX_THREADS = 3;
  private static final long                  delayTime   = 5 * 1000;
  private final        List<SSHOMListener>   listeners   = new ArrayList<>();
  private              Map<Thread, Long> threadStart = new HashMap<>(
      MAX_THREADS);
  private              Map<String, TestRunner> testMap = new HashMap<>(
      MAX_THREADS);

  public InfLoopTestProcess(SSHOMListener... listeners) {
    this.listeners.addAll(Arrays.asList(listeners));

  }

  public void process(String testClass, String testMethod, String... mutants) {
    String[] commandsPt1 = { "java", "-cp",
        System.getProperty("java.class.path"), SetHomTestRunner.class.getName(),
        testClass, testMethod };
    String[] allCommands = new String[commandsPt1.length + mutants.length];
    System.arraycopy(commandsPt1, 0, allCommands, 0, commandsPt1.length);
    System
        .arraycopy(mutants, 0, allCommands, commandsPt1.length, mutants.length);

    while (threadStart.size() >= MAX_THREADS) {
      try {
        Thread.sleep(100);
        removeFinishedThreads();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    String threadName =
        testClass + " " + testMethod + " " + String.join(" ", mutants);
    TestRunner tr = new TestRunner(testClass, testMethod, mutants, allCommands);
    Thread t = new Thread(tr, threadName);
    t.start();
    testMap.put(threadName, tr);
    threadStart.put(t, System.currentTimeMillis());
  }

  public void waitToFinish() {
    // let threads finish
    // if there are blocking threads, try 3x to run
    for (int i = 0; i < 3; i++) {
      if (threadStart.size() > 0) {
        try {
          Thread.sleep(2 * delayTime);
          removeFinishedThreads();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    // if there are still threads, record as blocking + failure, move on
    for (Thread t : threadStart.keySet()) {
      t.interrupt();
      threadStart.remove(t);
      TestRunner tr = testMap.get(t.getName());
      registerBlocked(tr.testClass, tr.testMethod, tr.activeMutants);
    }
  }

  private void removeFinishedThreads() throws InterruptedException {
    Set<Thread> remove = new HashSet<>();
    for (Thread t : threadStart.keySet()) {
      TestRunner tr = testMap.get(t.getName());

      if (tr.finished) {
        if (tr.failed) registerFailure(tr.testClass, tr.testMethod);
        remove.add(t);

      } else if ((threadStart.get(t) + (2 * delayTime)) < System
          .currentTimeMillis()) {
        System.out.println("restarting " + t.getName());
        // interrupt thread and retry
        t.interrupt();
        t.join();
        remove.add(t);
        Thread newThread = new Thread(tr, t.getName());
        newThread.start();
        threadStart.put(newThread, System.currentTimeMillis());
      }
    } for (Thread t : remove) {
      threadStart.remove(t);
    }
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

  /**
   * Should not happen due to tests; ironically, sometimes threads will block in InputStreamReader.ready()
   * Register as failure + record tests that blocked to manually check later
   *
   * @param testClass
   * @param testMethod
   * @param mutants
   */
  private void registerBlocked(String testClass, String testMethod,
      String[] mutants) {
    registerFailure(testClass, testMethod);
    Benchmarker.instance.timestamp(
        testClass + "." + testMethod + " blocked on mutants " + String
            .join(",", mutants));
  }

  private static SSHOMListener      listener = new SSHOMListener();
  private static InfLoopTestProcess process  = new InfLoopTestProcess(listener);

  public static SSHOMListener runTests(Class<?>[] testClasses,
      String[] mutants) {

    listener.signalHOMBegin();
    for (Class<?> c : testClasses) {
      for (Method m : c.getMethods()) {
        if (m.getAnnotation(Test.class) != null) {
          process.process(c.getName(), m.getName(), mutants);
        }
      }
    }
    process.waitToFinish();
    listener.signalHOMEnd();
    return listener;
  }

  private static class TestRunner implements Runnable {

    public final String[] commands;
    public       String[] activeMutants;
    public       String   testClass, testMethod;
    public boolean failed = false;
    public boolean finished = false;

    public TestRunner(String testClass, String testMethod, String[] mutants,
        String[] commands) {
      this.testClass = testClass;
      this.testMethod = testMethod;
      this.activeMutants = mutants;
      this.commands = commands;
    }

    @Override
    public void run() {
      try {
        ProcessBuilder processBuilder = new ProcessBuilder(this.commands);
        Process process = processBuilder.start();
        long startTime = System.currentTimeMillis();
        try (BufferedReader input = new BufferedReader(
            new InputStreamReader(process.getInputStream()));
//                        BufferedReader errInput = new BufferedReader(
//                            new InputStreamReader(process.getErrorStream()))
        ) {
          String line;
          while (process.isAlive()) {
            if (input.ready() && ((line = input.readLine()) != null)) {
//              System.out.println(line);
              if (line.startsWith("test.")) {
                System.out.println("f");
                this.failed = true;
                process.destroyForcibly();
                break;
              }
            }
            if (System.currentTimeMillis()
                > startTime + InfLoopTestProcess.delayTime) {
              System.out.println("t");
              this.failed = true;
//                              while ((line = errInput.readLine()) != null) {
//                                System.err.println(line);
//                              }
              process.destroyForcibly();
              break;
            }

          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      this.finished = true;
    }
  }
}
