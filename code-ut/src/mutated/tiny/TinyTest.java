package mutated.tiny;

import gov.nasa.jpf.annotation.Conditional;

public class TinyTest {
  @Conditional
  public static boolean m0 = true, m1 = false;

  public boolean tiny(int a, int b, int c) {
    boolean clause0 = b == c;
    boolean clause1 = m1 ? b + c <= a : b + c > a;
    if (m0 ? (clause0 || clause1) : (clause0 && clause1)) {
//    if (b == c && b+c > a) {
      return true;
    }
    return false;
  }
}
