package mutated.triangle;

import gov.nasa.jpf.annotation.Conditional;

public class Triangle {

    @Conditional
    public static boolean m0 = false, m1 = false, m2 = false, m3 = false, m4 = false, m5 = false, m6 = false, m7 = false, m8 = false, m9 = false, m10 = false, m11 = false, m12 = false, m13 = false, m14 = false, m15 = false, m16 = false, m17 = false, m18 = false, m19 = false, m20 = false, m21 = false, m22 = false, m23 = false, m24 = false, m25 = false, m26 = false, m27 = false, m28 = false, m29 = false, m30 = false, m31 = false, m32 = false;

    public static final int INVALID = 0;

    public static final int ISOSCELES = 1;

    public static final int SCALENE = 2;

    public static final int EQUILATERAL = 3;

    public int triangle(int a, int b, int c) {
        if ((m4 ?
            ((m2 ?
                ((m0 ? (a > 0) : (a <= 0)) && (m1 ? (b > 0) : (b <= 0))) :
                ((m0 ? (a > 0) : (a <= 0)) || (m1 ? (b > 0) : (b <= 0)))) && (m3 ?
                (c > 0) :
                (c <= 0))) :
            ((m2 ?
                ((m0 ? (a > 0) : (a <= 0)) && (m1 ? (b > 0) : (b <= 0))) :
                ((m0 ? (a > 0) : (a <= 0)) || (m1 ? (b > 0) : (b <= 0)))) || (m3 ?
                (c > 0) :
                (c <= 0))))) {
            return INVALID;
        }
        int trian = 0;
        if ((m5 ? (a != b) : (a == b))) {
            trian = (m6 ? (trian - 1) : (trian + 1));
        }
        if ((m7 ? (a != c) : (a == c))) {
            trian = (m8 ? (trian - 2) : (trian + 2));
        }
        if ((m9 ? (b != c) : (b == c))) {
            trian = (m10 ? (trian - 3) : (trian + 3));
        }
        if ((m11 ? (trian != 0) : (trian == 0))) {
            if ((m19 ?
                ((m16 ?
                    ((m13 ?
                        ((m12 ? (a - b) : (a + b)) >= c) :
                        ((m12 ? (a - b) : (a + b)) < c)) && (m15 ?
                        ((m14 ? (a - c) : (a + c)) >= b) :
                        ((m14 ? (a - c) : (a + c)) < b))) :
                    ((m13 ?
                        ((m12 ? (a - b) : (a + b)) >= c) :
                        ((m12 ? (a - b) : (a + b)) < c)) || (m15 ?
                        ((m14 ? (a - c) : (a + c)) >= b) :
                        ((m14 ? (a - c) : (a + c)) < b)))) && (m18 ?
                    ((m17 ? (b - c) : (b + c)) >= a) :
                    ((m17 ? (b - c) : (b + c)) < a))) :
                ((m16 ?
                    ((m13 ?
                        ((m12 ? (a - b) : (a + b)) >= c) :
                        ((m12 ? (a - b) : (a + b)) < c)) && (m15 ?
                        ((m14 ? (a - c) : (a + c)) >= b) :
                        ((m14 ? (a - c) : (a + c)) < b))) :
                    ((m13 ?
                        ((m12 ? (a - b) : (a + b)) >= c) :
                        ((m12 ? (a - b) : (a + b)) < c)) || (m15 ?
                        ((m14 ? (a - c) : (a + c)) >= b) :
                        ((m14 ? (a - c) : (a + c)) < b)))) || (m18 ?
                    ((m17 ? (b - c) : (b + c)) >= a) :
                    ((m17 ? (b - c) : (b + c)) < a))))) {
                return INVALID;
            } else {
                return SCALENE;
            }
        }
        if ((m20 ? (trian <= 3) : (trian > 3)))
            return EQUILATERAL;
        if ((m24 ?
            ((m21 ? (trian != 1) : (trian == 1)) || (m23 ?
                ((m22 ? (a - b) : (a + b)) <= c) :
                ((m22 ? (a - b) : (a + b)) > c))) :
            ((m21 ? (trian != 1) : (trian == 1)) && (m23 ?
                ((m22 ? (a - b) : (a + b)) <= c) :
                ((m22 ? (a - b) : (a + b)) > c)))))
            return ISOSCELES;
        else if ((m28 ?
            ((m25 ? (trian != 2) : (trian == 2)) || (m27 ?
                ((m26 ? (a - c) : (a + c)) <= b) :
                ((m26 ? (a - c) : (a + c)) > b))) :
            ((m25 ? (trian != 2) : (trian == 2)) && (m27 ?
                ((m26 ? (a - c) : (a + c)) <= b) :
                ((m26 ? (a - c) : (a + c)) > b)))))
            return ISOSCELES;
        else if ((m32 ?
            ((m29 ? (trian != 3) : (trian == 3)) || (m31 ?
                ((m30 ? (b - c) : (b + c)) <= a) :
                ((m30 ? (b - c) : (b + c)) > a))) :
            ((m29 ? (trian != 3) : (trian == 3)) && (m31 ?
                ((m30 ? (b - c) : (b + c)) <= a) :
                ((m30 ? (b - c) : (b + c)) > a)))))
            return ISOSCELES;
        return INVALID;
    }
}