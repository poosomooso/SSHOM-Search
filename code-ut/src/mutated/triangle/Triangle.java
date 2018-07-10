package mutated.triangle;

import gov.nasa.jpf.annotation.Conditional;

public class Triangle {

  @Conditional
  public static boolean _M_Triangle_mut0 = false, _M_Triangle_mut1 = false, _M_Triangle_mut2 = false, _M_Triangle_mut3 = false, _M_Triangle_mut4 = false, _M_Triangle_mut5 = false, _M_Triangle_mut6 = false, _M_Triangle_mut7 = false, _M_Triangle_mut8 = false, _M_Triangle_mut9 = false, _M_Triangle_mut10 = false, _M_Triangle_mut11 = false, _M_Triangle_mut12 = false, _M_Triangle_mut13 = false, _M_Triangle_mut14 = false, _M_Triangle_mut15 = false, _M_Triangle_mut16 = false, _M_Triangle_mut17 = false, _M_Triangle_mut18 = false, _M_Triangle_mut19 = false, _M_Triangle_mut20 = false, _M_Triangle_mut21 = false, _M_Triangle_mut22 = false, _M_Triangle_mut23 = false, _M_Triangle_mut24 = false, _M_Triangle_mut25 = false, _M_Triangle_mut26 = false, _M_Triangle_mut27 = false, _M_Triangle_mut28 = false, _M_Triangle_mut29 = false, _M_Triangle_mut30 = false, _M_Triangle_mut31 = false, _M_Triangle_mut32 = false;

  static final int INVALID = 0;

  static final int ISOSCELES = 1;

  static final int SCALENE = 2;

  static final int EQUILATERAL = 3;

  public int triangle(int a, int b, int c) {
    if ((_M_Triangle_mut4 ?
        ((_M_Triangle_mut2 ?
            ((_M_Triangle_mut0 ? (a > 0) : (a <= 0)) && (_M_Triangle_mut1 ?
                (b > 0) :
                (b <= 0))) :
            ((_M_Triangle_mut0 ? (a > 0) : (a <= 0)) || (_M_Triangle_mut1 ?
                (b > 0) :
                (b <= 0)))) && (_M_Triangle_mut3 ? (c > 0) : (c <= 0))) :
        ((_M_Triangle_mut2 ?
            ((_M_Triangle_mut0 ? (a > 0) : (a <= 0)) && (_M_Triangle_mut1 ?
                (b > 0) :
                (b <= 0))) :
            ((_M_Triangle_mut0 ? (a > 0) : (a <= 0)) || (_M_Triangle_mut1 ?
                (b > 0) :
                (b <= 0)))) || (_M_Triangle_mut3 ? (c > 0) : (c <= 0))))) {
      return INVALID;
    }
    int trian = 0;
    if ((_M_Triangle_mut5 ? (a != b) : (a == b))) {
      trian = (_M_Triangle_mut6 ? (trian - 1) : (trian + 1));
    }
    if ((_M_Triangle_mut7 ? (a != c) : (a == c))) {
      trian = (_M_Triangle_mut8 ? (trian - 2) : (trian + 2));
    }
    if ((_M_Triangle_mut9 ? (b != c) : (b == c))) {
      trian = (_M_Triangle_mut10 ? (trian - 3) : (trian + 3));
    }
    if ((_M_Triangle_mut11 ? (trian != 0) : (trian == 0))) {
      if ((_M_Triangle_mut19 ?
          ((_M_Triangle_mut16 ?
              ((_M_Triangle_mut13 ?
                  ((_M_Triangle_mut12 ? (a - b) : (a + b)) >= c) :
                  ((_M_Triangle_mut12 ? (a - b) : (a + b)) < c)) && (
                  _M_Triangle_mut15 ?
                      ((_M_Triangle_mut14 ? (a - c) : (a + c)) >= b) :
                      ((_M_Triangle_mut14 ? (a - c) : (a + c)) < b))) :
              ((_M_Triangle_mut13 ?
                  ((_M_Triangle_mut12 ? (a - b) : (a + b)) >= c) :
                  ((_M_Triangle_mut12 ? (a - b) : (a + b)) < c)) || (
                  _M_Triangle_mut15 ?
                      ((_M_Triangle_mut14 ? (a - c) : (a + c)) >= b) :
                      ((_M_Triangle_mut14 ? (a - c) : (a + c)) < b)))) && (
              _M_Triangle_mut18 ?
                  ((_M_Triangle_mut17 ? (b - c) : (b + c)) >= a) :
                  ((_M_Triangle_mut17 ? (b - c) : (b + c)) < a))) :
          ((_M_Triangle_mut16 ?
              ((_M_Triangle_mut13 ?
                  ((_M_Triangle_mut12 ? (a - b) : (a + b)) >= c) :
                  ((_M_Triangle_mut12 ? (a - b) : (a + b)) < c)) && (
                  _M_Triangle_mut15 ?
                      ((_M_Triangle_mut14 ? (a - c) : (a + c)) >= b) :
                      ((_M_Triangle_mut14 ? (a - c) : (a + c)) < b))) :
              ((_M_Triangle_mut13 ?
                  ((_M_Triangle_mut12 ? (a - b) : (a + b)) >= c) :
                  ((_M_Triangle_mut12 ? (a - b) : (a + b)) < c)) || (
                  _M_Triangle_mut15 ?
                      ((_M_Triangle_mut14 ? (a - c) : (a + c)) >= b) :
                      ((_M_Triangle_mut14 ? (a - c) : (a + c)) < b)))) || (
              _M_Triangle_mut18 ?
                  ((_M_Triangle_mut17 ? (b - c) : (b + c)) >= a) :
                  ((_M_Triangle_mut17 ? (b - c) : (b + c)) < a))))) {
        return INVALID;
      } else {
        return SCALENE;
      }
    }
    if ((_M_Triangle_mut20 ? (trian <= 3) : (trian > 3)))
      return EQUILATERAL;
    if ((_M_Triangle_mut24 ?
        ((_M_Triangle_mut21 ? (trian != 1) : (trian == 1))
            || (_M_Triangle_mut23 ?
            ((_M_Triangle_mut22 ? (a - b) : (a + b)) <= c) :
            ((_M_Triangle_mut22 ? (a - b) : (a + b)) > c))) :
        ((_M_Triangle_mut21 ? (trian != 1) : (trian == 1))
            && (_M_Triangle_mut23 ?
            ((_M_Triangle_mut22 ? (a - b) : (a + b)) <= c) :
            ((_M_Triangle_mut22 ? (a - b) : (a + b)) > c)))))
      return ISOSCELES;
    else if ((_M_Triangle_mut28 ?
        ((_M_Triangle_mut25 ? (trian != 2) : (trian == 2))
            || (_M_Triangle_mut27 ?
            ((_M_Triangle_mut26 ? (a - c) : (a + c)) <= b) :
            ((_M_Triangle_mut26 ? (a - c) : (a + c)) > b))) :
        ((_M_Triangle_mut25 ? (trian != 2) : (trian == 2))
            && (_M_Triangle_mut27 ?
            ((_M_Triangle_mut26 ? (a - c) : (a + c)) <= b) :
            ((_M_Triangle_mut26 ? (a - c) : (a + c)) > b)))))
      return ISOSCELES;
    else if ((_M_Triangle_mut32 ?
        ((_M_Triangle_mut29 ? (trian != 3) : (trian == 3))
            || (_M_Triangle_mut31 ?
            ((_M_Triangle_mut30 ? (b - c) : (b + c)) <= a) :
            ((_M_Triangle_mut30 ? (b - c) : (b + c)) > a))) :
        ((_M_Triangle_mut29 ? (trian != 3) : (trian == 3))
            && (_M_Triangle_mut31 ?
            ((_M_Triangle_mut30 ? (b - c) : (b + c)) <= a) :
            ((_M_Triangle_mut30 ? (b - c) : (b + c)) > a)))))
      return ISOSCELES;
    return INVALID;
  }
}
