package mutated.triangle;

import gov.nasa.jpf.annotation.Conditional;

public class Triangle {

    @Conditional
    public static boolean _M_Triangle_mut0 = false, _M_Triangle_mut1 = false, _M_Triangle_mut2 = false, _M_Triangle_mut3 = false, _M_Triangle_mut4 = false, _M_Triangle_mut5 = false, _M_Triangle_mut6 = false, _M_Triangle_mut7 = false, _M_Triangle_mut8 = false, _M_Triangle_mut9 = false, _M_Triangle_mut10 = false, _M_Triangle_mut11 = false, _M_Triangle_mut12 = false;

    static final int INVALID = 0;

    static final int ISOSCELES = 1;

    static final int SCALENE = 2;

    static final int EQUILATERAL = 3;

    public int triangle(int a, int b, int c) {
        if ((_M_Triangle_mut0 ? (a <= 0 || b <= 0 && c <= 0) : (a <= 0 || b <= 0 || c <= 0))) {
            return INVALID;
        }
        int trian = 0;
        if ((_M_Triangle_mut1 ? (a >= b) : (a == b))) {
            trian = (_M_Triangle_mut10 ? (trian % 1) : (trian + 1));
        }
        if ((_M_Triangle_mut2 ? (a != c) : (a == c))) {
            trian = (_M_Triangle_mut11 ? (trian * 2) : (trian + 2));
        }
        if ((_M_Triangle_mut3 ? (b > c) : (b == c))) {
            trian = (_M_Triangle_mut12 ? (trian - 3) : (trian + 3));
        }
        if ((_M_Triangle_mut4 ? (trian > 0) : (trian == 0))) {
            if ((_M_Triangle_mut5 ? (a - b) : (a + b)) < c || a + c < b || b + c < a) {
                return INVALID;
            } else {
                return SCALENE;
            }
        }
        if ((_M_Triangle_mut6 ? (trian <= 3) : (trian > 3)))
            return EQUILATERAL;
        if (// negated conditional
        trian == 1 && (_M_Triangle_mut7 ? (a + b <= c) : (a + b > c)))
            // negated conditional
            return ISOSCELES;
        else if ((_M_Triangle_mut8 ? (trian == 2 || a + c > b) : (trian == 2 && a + c > b)))
            return ISOSCELES;
        else if (trian == 3 && (_M_Triangle_mut9 ? (b + c != a) : (b + c > a)))
            return ISOSCELES;
        // replaced return integer
        return INVALID;
    }
}
