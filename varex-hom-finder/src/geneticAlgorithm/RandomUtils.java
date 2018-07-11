package geneticAlgorithm;

public class RandomUtils {
  public static int randRange(int startInc, int endExc) {
    int range = endExc - startInc;
    return ((int) (Math.random() * range)) + startInc;
  }
}
