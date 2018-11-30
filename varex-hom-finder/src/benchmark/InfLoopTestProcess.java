package benchmark;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import testRunner.SetHomTestRunner;
import util.SSHOMListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InfLoopTestProcess {

  private final List<SSHOMListener> listeners = new ArrayList<>();
  private String testClass;
  private String testMethod;
  private final long delayTime = 3 * 1000;

  public InfLoopTestProcess(SSHOMListener...listeners) {
    this.listeners.addAll(Arrays.asList(listeners));

  }

  public void process(String testClass, String testMethod, String... mutants) {
    try {
      this.testClass = testClass;
      this.testMethod = testMethod;
      String[] commandsPt1 = { "java", "-cp",
          System.getProperty("java.class.path"),
          SetHomTestRunner.class.getName(), this.testClass, this.testMethod };
      String[] allCommands = new String[commandsPt1.length + mutants.length];
      System.arraycopy(commandsPt1, 0, allCommands, 0, commandsPt1.length);
      System.arraycopy(mutants, 0, allCommands, commandsPt1.length, mutants.length);
      ProcessBuilder processBuilder = new ProcessBuilder(allCommands);
      Process process = processBuilder.start();
      long startTime = System.currentTimeMillis();
      try (BufferedReader input = new BufferedReader(new InputStreamReader(
          process.getInputStream()));
//           BufferedReader errInput = new BufferedReader(
//               new InputStreamReader(process.getErrorStream()))
      ) {
          String line;
          while (process.isAlive() && (line = input.readLine()) != null) {
            if (line.equals(testMethod)) {
              listeners.forEach(this::registerFailure);
            }
            if (System.currentTimeMillis() > startTime + delayTime) {
              listeners.forEach(this::registerFailure);
//                while ((line = errInput.readLine()) != null) {
//                  System.err.println(line);
//                }
              process.destroyForcibly();
              break;
            }
          }
    }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static SSHOMListener listener = new SSHOMListener();
  private static InfLoopTestProcess process = new InfLoopTestProcess(listener);
  public static SSHOMListener runTests(Class<?>[] testClasses, String[] mutants) {

    listener.signalHOMBegin();
    for (Class<?> c : testClasses) {
      for (Method m : c.getMethods()) {
        if (m.getAnnotation(Test.class) != null) {
          process.process(c.getName(), m.getName(), mutants);
        }
      }
    }
    listener.signalHOMEnd();
    return listener;
  }

  private void registerFailure(SSHOMListener listener) {
    try {
      listener.testFailure(
          new Failure(Description.createTestDescription(testClass, testMethod),
              null));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
