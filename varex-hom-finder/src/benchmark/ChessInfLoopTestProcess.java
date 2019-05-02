package benchmark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import br.ufmg.labsoft.mutvariants.schematalib.ISchemataLibMethodsListener;
import br.ufmg.labsoft.mutvariants.schematalib.SchemataLibMethods;
import util.SSHOMListener;

public class ChessInfLoopTestProcess {

  private final List<SSHOMListener> listeners = new ArrayList<>();
  private static Map<Thread, Long> threadStart = new HashMap<>();
  private static Map<String, TestRunner> testMap = new HashMap<>();

  public ChessInfLoopTestProcess(SSHOMListener... listeners) {
    this.listeners.addAll(Arrays.asList(listeners));
  }

  public void process(Class<?>[] tests, String... mutants) {
    String mutantStr =
        String.join(",", mutants);
    String testClassStr = String.join(",",
        Arrays.asList(tests).stream().map(Class::getName)
            .collect(Collectors.toList()));
    String[] commands = { "java", "-noverify", "-cp",
        System.getProperty("java.class.path"), this.getClass().getName(),
        mutantStr, testClassStr };

    TestRunner tr = new TestRunner(mutants, commands);
    Thread t = new Thread(tr, mutantStr);
    t.start();
    testMap.put(mutantStr, tr);
    threadStart.put(t, System.currentTimeMillis());

    while (threadStart.size() > 0) {
      try {
        removeFinishedThreads();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    try {
		t.join();
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
  }

  private void removeFinishedThreads() throws InterruptedException {
    Set<Thread> remove = new HashSet<>();
    for (Thread t : threadStart.keySet()) {
      TestRunner tr = testMap.get(t.getName());

      if (tr.finished) {
        for (String failedTest : tr.failedTests) {
          String[] names = failedTest.split(",");
          registerFailure(names[0], names[1]);
        }
        remove.add(t);

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
        System.out.println(testClass+" "+testMethod);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


  public static SSHOMListener runTests(Class<?>[] testClasses,
      String[] mutants) {
    SSHOMListener listener = new SSHOMListener();
    ChessInfLoopTestProcess process = new ChessInfLoopTestProcess(listener);

    listener.signalHOMBegin();
    process.process(testClasses, mutants);
    listener.signalHOMEnd();

    return listener;
  }

  private static class TestRunner implements Runnable {

    public final String[] commands;
    public       String[] activeMutants;
    public List<String> failedTests = new ArrayList<>();
    public boolean finished = false;

    public TestRunner(String[] mutants,
        String[] commands){
      this.activeMutants = mutants;
      this.commands = commands;
    }

    @Override
    public void run() {
      try {
        ProcessBuilder processBuilder = new ProcessBuilder(this.commands);
        Process process = processBuilder.start();
        try (BufferedReader input = new BufferedReader(
            new InputStreamReader(process.getInputStream()));
            //                        BufferedReader errInput = new BufferedReader(
            //                            new InputStreamReader(process.getErrorStream()))
        ) {
          String line;
          while (process.isAlive()) {
            if (((line = input.readLine()) != null)) {
                            System.out.println(line);
              if (line.startsWith("failtest ")) {
                this.failedTests.add(line.substring("failtest ".length()));
              }
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      this.finished = true;
    }
  }

  public static void main(String[] args) throws ClassNotFoundException {
    BenchmarkPrograms.PROGRAM = BenchmarkPrograms.Program.CHESS;
    SchemataLibMethods.listener = new ISchemataLibMethodsListener() {

		@Override
		public void listen() {
			if (InfLoopTestProcess.timedOut) {
				throw new Error("TIMEOUT");
			}
		}

		@Override
		public void listen(String methodName) {
			if (InfLoopTestProcess.timedOut) {
				throw new Error("TIMEOUT");
			}
		}
		
	};    
    String[] mutants = args[0].length() == 0 ? new String[0] : args[0].split(",");
   	String[] classStr = args[1].split(",");    	

    Deque<String[]> testCases = new ArrayDeque<>();

    for (String c : classStr) {
      Class<?> testClass = Class.forName(c);
      for (Method m : testClass.getMethods()) {
        if (m.getAnnotation(Test.class) != null) {
          testCases.add(new String[]{c, m.getName()});
        }
      }
    }

    InfLoopTestProcess process = new InfLoopTestProcess();
    process.runTests(mutants, testCases);
    System.exit(0);
  }
}