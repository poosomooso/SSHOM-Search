package triangle;

import gov.nasa.jpf.annotation.Conditional;

public class Triangle {
    final static int INVALID = 0;
    final static int ISOSCELES = 1;
    final static int SCALENE = 2;
    final static int EQUILATERAL = 3;

    @Conditional
    public static boolean m0 = false;
    @Conditional
    public static boolean m1 = false;
    @Conditional
    public static boolean m2 = false;
    @Conditional
    public static boolean m3 = false;
    @Conditional
    public static boolean m4 = false;
    @Conditional
    public static boolean m5 = false;
    @Conditional
    public static boolean m6 = false;
    @Conditional
    public static boolean m7 = false;
    @Conditional
    public static boolean m8 = false;
    @Conditional
    public static boolean m9 = false;
    @Conditional
    public static boolean m10 = false;
    @Conditional
    public static boolean m11 = false;
    @Conditional
    public static boolean m12 = false;
    @Conditional
    public static boolean m13 = false;
    @Conditional
    public static boolean m14 = false;
    @Conditional
    public static boolean m15 = false;
    @Conditional
    public static boolean m16 = false;
    @Conditional
    public static boolean m17 = false;
    @Conditional
    public static boolean m18 = false;
    @Conditional
    public static boolean m19 = false;
    @Conditional
    public static boolean m20 = false;
    @Conditional
    public static boolean m21 = false;
    @Conditional
    public static boolean m22 = false;
    @Conditional
    public static boolean m23 = false;
    @Conditional
    public static boolean m24 = false;
    @Conditional
    public static boolean m25 = false;

    public int triangle(int a, int b, int c) {
        if ((m0 ? a > 0 : a <= 0) ||
            (m1 ? b > 0 : b <= 0) ||
            (m2 ? c > 0 : c <= 0)) {
            return INVALID;
        }
        int trian = 0;
        if (m3 ? a != b : a == b) {
            trian = m4 ? trian - 1 : trian + 1;
        }
        if (m5 ? a != c : a == c) {
            trian = m6 ? trian - 2 : trian + 2;
        }
        if (m7 ? b != c : b == c) {
            trian = m8 ? trian - 3 : trian + 3;
        }
        if (m9 ? trian != 0 : trian == 0) {
            int ab = (m10 ? a - b : a + b);
            int ac = (m11 ? a - c : a + c);
            int bc = (m12 ? b - c : b + c);
            if ((m13 ? ab >= c : ab < c) || (m14 ? ac >= b : ac < b) || (m15 ? bc >= a : bc < a)) {
                return INVALID;
            } else {
                return SCALENE;
            }
        }
        if (m16 ? trian <= 3 : trian > 3) return EQUILATERAL;
        if ((m17 ? trian != 1 : trian == 1) &&
            (m19 ?
                (m18? a - b : a + b) <= c :
                (m18? a - b : a + b) > c)) return ISOSCELES;
        else if ((m20 ? trian != 2 : trian == 2) &&
            (m22 ?
                (m21 ? a -c : a + c) <= b :
                (m21 ? a -c : a + c) > b)) return ISOSCELES;
        else if ((m23 ? trian != 3 : trian == 3) &&
            (m25 ?
                (m24 ? b - c : b + c) <= a :
                (m24 ? b - c : b + c) > a)) return ISOSCELES;
        return INVALID;
    }

    public static void main(String[] args) {
        System.out.println(new Triangle().triangle(3, 4, 5));
    }
}
