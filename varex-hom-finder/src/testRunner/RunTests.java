package testRunner;

import org.junit.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RunTests {

	public static boolean print = false;
	public static void runTests(Class<?> testClass) {
		runTests(new Class[] { testClass });
	}

	public static void runTests(Class<?>[] testClasses, String testName) {
		for (Class<?> c : testClasses) {
			runTestAnnotations(c, testName);
		}
	}

	public static Optional<Boolean> runTest(Class<?> testClass, String testName) {
		if (!Modifier.isAbstract(testClass.getModifiers())) {
				return Optional.of(runTestAnnotations(testClass, testName));
		}
		return Optional.empty();
	}

	public static void runTests(Class<?>[] testClasses) {
		for (Class<?> c : testClasses) {
			List<Method> methods = Arrays.asList(c.getMethods());
			methods.stream()
					.filter(m -> m.getAnnotation(Test.class) != null)
					.sorted((x, y) -> x.getName().compareTo(y.getName()));

			for (Method m : methods) {
				runTestAnnotations(c, m.getName());
			}
		}
	}

	private static boolean runTestAnnotations(Class<?> c, String testName) {
		Optional<Method> beforeMethod = findOfAnnotation(c, Before.class);
		Optional<Method> beforeClassMethod = findOfAnnotation(c, BeforeClass.class);
		Optional<Method> afterMethod = findOfAnnotation(c, After.class);
		Optional<Method> afterClassMethod = findOfAnnotation(c, AfterClass.class);

		boolean passed = true;

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
			if (method.getName().equals(testName)) {
				if (method.getAnnotation(Ignore.class) != null) {
					break;
				}
				if (method.getAnnotation(Test.class) != null) {
					Test annotation = method.getAnnotation(org.junit.Test.class);
					Class<? extends Throwable> expected = annotation.expected();
					try {
						invokeIfNonempty(beforeMethod, instance);
						if (print) System.out.println("METHOD: " + method);
						method.invoke(instance);
						invokeIfNonempty(afterMethod, instance);
						if (!expected.equals(Test.None.class)) {
							passed = false;
							break;
						}
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (Throwable e) {
						if (e instanceof InvocationTargetException) {
							InvocationTargetException invokedException = (InvocationTargetException) e;
							Class<? extends Throwable> actual = invokedException.getTargetException().getClass();
							if (expected.isAssignableFrom(actual)) {
								System.out.println("EXPECTED 2");
								break;
							}
							if (print) System.out.println(getTestDesc(method));
						}
						e.printStackTrace(System.out);
						passed = false;
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

		return passed;
	}
	
	private static Optional<Method> findOfAnnotation(Class<?> c, Class<? extends Annotation> annotation) {
		for (Method method : c.getMethods()) {
			if (method.getAnnotation(annotation) != null) {
				return Optional.of(method);
			}
		}
		for (Method method : c.getSuperclass().getMethods()) {
			if (method.getAnnotation(annotation) != null) {
				return Optional.of(method);
			}
		}
		
		return Optional.empty();
	}

	private static <T> void invokeIfNonempty(Optional<Method> m, T instance) throws InvocationTargetException,
			IllegalAccessException {
		if (m.isPresent()) {
			m.get().invoke(instance);
		}
	}

	public static String getTestDesc(Method m) {
		return getTestDesc(m.getDeclaringClass(), m);
	}
	
	public static String getTestDesc(Class<?> c, Method m) {
		return "test." + c.getName() + "." + m.getName();
	}

}
