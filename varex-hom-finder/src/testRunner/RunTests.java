package testRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RunTests {
	public static void runTests(Class testClass) {
		runTests(new Class[] { testClass });
	}

	public static void runTests(Class[] testClasses, String testName) {
		for (Class c : testClasses) {
			runTestAnnotations(c, testName);
		}
	}

	public static void runTests(Class testClass, String testName) {
		runTestAnnotations(testClass, testName);
	}

	public static void runTests(Class[] testClasses) {
		for (Class c : testClasses) {
			List<Method> methods = Arrays.asList(c.getMethods());
			methods.stream()
					.filter(m -> m.getAnnotation(Test.class) != null)
					.sorted((x, y) -> x.getName().compareTo(y.getName()));

			for (Method m : methods) {
				runTestAnnotations(c, m.getName());
			}
		}
	}

	private static void runTestAnnotations(Class c, String testName) {
		Optional<Method> beforeMethod = findOfAnnotation(c, Before.class);
		Optional<Method> beforeClassMethod = findOfAnnotation(c, BeforeClass.class);
		Optional<Method> afterMethod = findOfAnnotation(c, After.class);
		Optional<Method> afterClassMethod = findOfAnnotation(c, AfterClass.class);

		// creating test object
		Object instance;
		try {
			instance = c.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		// before class
		try {
			invokeIfNonempty(beforeClassMethod, instance);
		} catch (InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
		}

		// run all tests

		Method[] methods = c.getMethods();
		for (Method method : methods) {
			if (method.getName().equals(testName)) {// TODO needs to handle duplicate test names
				if (method.getAnnotation(Test.class) != null) {
					try {
						invokeIfNonempty(beforeMethod, instance);
						System.out.println("METHOD: " + method);
						method.invoke(instance);
						invokeIfNonempty(afterMethod, instance);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (Throwable e) {
						System.out.println(getTestDesc(method));
					}
					break;
				}
			}
		}

		// after class
		try {
			invokeIfNonempty(afterClassMethod, instance);
		} catch (InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private static Optional<Method> findOfAnnotation(Class c, Class annotation) {
		for (Method method : c.getMethods()) {
			if (method.getAnnotation(annotation) != null) {
				return Optional.of(method);
			}
		}
		return Optional.empty();
	}

	private static <T> void invokeIfNonempty(Optional<Method> m, T instance) throws InvocationTargetException,
			IllegalAccessException {
		if (m.isPresent()) {
			m.get().invoke(instance, null);
		}
	}

	public static String getTestDesc(Method m) {
		return "test." + m.getDeclaringClass().getName() + "." + m.getName();
	}

}
