package util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import gov.nasa.jpf.annotation.Conditional;

public class ConditionalMutationWrapper {
  Map<String, Field> conditionalFields = new HashMap<>();
  Class<?>[] testClasses;

  public ConditionalMutationWrapper(Class<?>... targetClasses) {
    for (Class<?> c : targetClasses) {
      for (Field f : c.getFields()) {
        if (f.getAnnotation(Conditional.class) != null) {
          f.setAccessible(true);
          conditionalFields.put(f.getName(), f);
        }
      }
    }
    testClasses = Arrays.copyOf(targetClasses, targetClasses.length);
  }

  public void resetMutants() {
    for (Field f : conditionalFields.values()) {
      try {
        f.setBoolean(null, false );
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(
            "Each conditional must be a public static boolean field", e);
      }

    }
  }

  public void setMutant(String mutant) {
    Field f = conditionalFields.get(mutant);
    if (f != null) {
      try {
        f.setBoolean(null, true);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(
            "Each conditional must be a public static boolean field", e);
      }
    } else {
      throw new IllegalArgumentException(
          "Mutant " + mutant + " is not a valid mutant");
    }
  }

  public Collection<String> getMutants() {
    return conditionalFields.keySet();
  }

  public Class<?>[] getTestClasses() {
    return testClasses;
  }
}
