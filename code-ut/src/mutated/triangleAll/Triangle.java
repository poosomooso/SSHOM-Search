package mutated.triangleAll;

import gov.nasa.jpf.annotation.Conditional;

public class Triangle {

    @Conditional
    public static boolean m0 = false, m1 = false, m2 = false, m3 = false, m4 = false;
    @Conditional
    public static boolean m5 = false, m6 = false, m7 = false, m8 = false, m9 = false, m10 = false, m11 = false, m12 = false, m13 = false, m14 = false, m15 = false, m16 = false;
    @Conditional
    public static boolean m17 = false, m18 = false, m19 = false;
    @Conditional
    public static boolean m20 = false, m21 = false, m22 = false, m23 = false, m24 = false, m25 = false, m26 = false, m27 = false, m28 = false, m29 = false;
    @Conditional
    public static boolean m30 = false, m31 = false, m32 = false, m33 = false, m34 = false, m35 = false, m36 = false, m37 = false, m38 = false, m39 = false;
    @Conditional
    public static boolean m40 = false, m41 = false, m42 = false, m43 = false, m44 = false, m45 = false, m46 = false, m47 = false, m48 = false, m49 = false, m50 = false, m51 = false, m52 = false, m53 = false, m54 = false, m55 = false, m56 = false, m57 = false;
    @Conditional
    public static boolean m58 = false, m59 = false, m60 = false, m61 = false, m62 = false, m63 = false, m64 = false, m65 = false, m66 = false, m67 = false, m68 = false, m69 = false, m70 = false, m71 = false, m72 = false, m73 = false, m74 = false, m75 = false;
    @Conditional
    public static boolean m76 = false, m77 = false, m78 = false, m79 = false, m80 = false, m81 = false, m82 = false, m83 = false, m84 = false, m85 = false, m86 = false, m87 = false, m88 = false, m89 = false, m90 = false;
    @Conditional
    public static boolean m91 = false, m92 = false, m93 = false, m94 = false, m95 = false, m96 = false, m97 = false, m98 = false, m99 = false, m100 = false;
    @Conditional
    public static boolean m101 = false, m102 = false, m103 = false, m104 = false, m105 = false, m106 = false, m107 = false, m108 = false, m109 = false, m110 = false;
    @Conditional
    public static boolean m111 = false, m112 = false, m113 = false, m114 = false;
    @Conditional
    public static boolean m115 = false, m116 = false, m117 = false, m118 = false, m119 = false, m120 = false, m121 = false, m122 = false, m123 = false, m124 = false, m125 = false, m126 = false, m127 = false;

    public static final int INVALID = 0;

    public static final int ISOSCELES = 1;

    public static final int SCALENE = 2;

    public static final int EQUILATERAL = 3;

    public int triangle(int a, int b, int c) {
        if ((m16 ? ((m10 ? ((m4 ? (a >= 0) : (m3 ? (a > 0) : (m2 ? (a < 0) : (m1 ? (a != 0) : (m0 ? (a == 0) : (a <= 0)))))) && (m9 ? (b >= 0) : (m8 ? (b > 0) : (m7 ? (b < 0) : (m6 ? (b != 0) : (m5 ? (b == 0) : (b <= 0))))))) : ((m4 ? (a >= 0) : (m3 ? (a > 0) : (m2 ? (a < 0) : (m1 ? (a != 0) : (m0 ? (a == 0) : (a <= 0)))))) || (m9 ? (b >= 0) : (m8 ? (b > 0) : (m7 ? (b < 0) : (m6 ? (b != 0) : (m5 ? (b == 0) : (b <= 0)))))))) && (m15 ? (c >= 0) : (m14 ? (c > 0) : (m13 ? (c < 0) : (m12 ? (c != 0) : (m11 ? (c == 0) : (c <= 0))))))) : ((m10 ? ((m4 ? (a >= 0) : (m3 ? (a > 0) : (m2 ? (a < 0) : (m1 ? (a != 0) : (m0 ? (a == 0) : (a <= 0)))))) && (m9 ? (b >= 0) : (m8 ? (b > 0) : (m7 ? (b < 0) : (m6 ? (b != 0) : (m5 ? (b == 0) : (b <= 0))))))) : ((m4 ? (a >= 0) : (m3 ? (a > 0) : (m2 ? (a < 0) : (m1 ? (a != 0) : (m0 ? (a == 0) : (a <= 0)))))) || (m9 ? (b >= 0) : (m8 ? (b > 0) : (m7 ? (b < 0) : (m6 ? (b != 0) : (m5 ? (b == 0) : (b <= 0)))))))) || (m15 ? (c >= 0) : (m14 ? (c > 0) : (m13 ? (c < 0) : (m12 ? (c != 0) : (m11 ? (c == 0) : (c <= 0))))))))) {
            return INVALID;
        }
        int trian = 0;
        if ((m21 ? (a >= b) : (m20 ? (a <= b) : (m19 ? (a > b) : (m18 ? (a < b) : (m17 ? (a != b) : (a == b))))))) {
            trian = (m25 ? (trian % 1) : (m24 ? (trian / 1) : (m23 ? (trian * 1) : (m22 ? (trian - 1) : (trian + 1)))));
        }
        if ((m30 ? (a >= c) : (m29 ? (a <= c) : (m28 ? (a > c) : (m27 ? (a < c) : (m26 ? (a != c) : (a == c))))))) {
            trian = (m34 ? (trian % 2) : (m33 ? (trian / 2) : (m32 ? (trian * 2) : (m31 ? (trian - 2) : (trian + 2)))));
        }
        if ((m39 ? (b >= c) : (m38 ? (b <= c) : (m37 ? (b > c) : (m36 ? (b < c) : (m35 ? (b != c) : (b == c))))))) {
            trian = (m43 ? (trian % 3) : (m42 ? (trian / 3) : (m41 ? (trian * 3) : (m40 ? (trian - 3) : (trian + 3)))));
        }
        if ((m48 ? (trian >= 0) : (m47 ? (trian <= 0) : (m46 ? (trian > 0) : (m45 ? (trian < 0) : (m44 ? (trian != 0) : (trian == 0))))))) {
            if ((m77 ? ((m67 ? ((m57 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) >= c) : (m56 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) <= c) : (m55 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) > c) : (m54 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) != c) : (m53 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) == c) : ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) < c)))))) && (m66 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) >= b) : (m65 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) <= b) : (m64 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) > b) : (m63 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) != b) : (m62 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) == b) : ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) < b))))))) : ((m57 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) >= c) : (m56 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) <= c) : (m55 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) > c) : (m54 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) != c) : (m53 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) == c) : ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) < c)))))) || (m66 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) >= b) : (m65 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) <= b) : (m64 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) > b) : (m63 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) != b) : (m62 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) == b) : ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) < b)))))))) && (m76 ? ((m71 ? (b % c) : (m70 ? (b / c) : (m69 ? (b * c) : (m68 ? (b - c) : (b + c))))) >= a) : (m75 ? ((m71 ? (b % c) : (m70 ? (b / c) : (m69 ? (b * c) : (m68 ? (b - c) : (b + c))))) <= a) : (m74 ? ((m71 ? (b % c) : (m70 ? (b / c) : (m69 ? (b * c) : (m68 ? (b - c) : (b + c))))) > a) : (m73 ? ((m71 ? (b % c) : (m70 ? (b / c) : (m69 ? (b * c) : (m68 ? (b - c) : (b + c))))) != a) : (m72 ? ((m71 ? (b % c) : (m70 ? (b / c) : (m69 ? (b * c) : (m68 ? (b - c) : (b + c))))) == a) : ((m71 ? (b % c) : (m70 ? (b / c) : (m69 ? (b * c) : (m68 ? (b - c) : (b + c))))) < a))))))) : ((m67 ? ((m57 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) >= c) : (m56 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) <= c) : (m55 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) > c) : (m54 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) != c) : (m53 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) == c) : ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) < c)))))) && (m66 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) >= b) : (m65 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) <= b) : (m64 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) > b) : (m63 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) != b) : (m62 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) == b) : ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) < b))))))) : ((m57 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) >= c) : (m56 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) <= c) : (m55 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) > c) : (m54 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) != c) : (m53 ? ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) == c) : ((m52 ? (a % b) : (m51 ? (a / b) : (m50 ? (a * b) : (m49 ? (a - b) : (a + b))))) < c)))))) || (m66 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) >= b) : (m65 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) <= b) : (m64 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) > b) : (m63 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) != b) : (m62 ? ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) == b) : ((m61 ? (a % c) : (m60 ? (a / c) : (m59 ? (a * c) : (m58 ? (a - c) : (a + c))))) < b)))))))) || (m76 ? ((m71 ? (b % c) : (m70 ? (b / c) : (m69 ? (b * c) : (m68 ? (b - c) : (b + c))))) >= a) : (m75 ? ((m71 ? (b % c) : (m70 ? (b / c) : (m69 ? (b * c) : (m68 ? (b - c) : (b + c))))) <= a) : (m74 ? ((m71 ? (b % c) : (m70 ? (b / c) : (m69 ? (b * c) : (m68 ? (b - c) : (b + c))))) > a) : (m73 ? ((m71 ? (b % c) : (m70 ? (b / c) : (m69 ? (b * c) : (m68 ? (b - c) : (b + c))))) != a) : (m72 ? ((m71 ? (b % c) : (m70 ? (b / c) : (m69 ? (b * c) : (m68 ? (b - c) : (b + c))))) == a) : ((m71 ? (b % c) : (m70 ? (b / c) : (m69 ? (b * c) : (m68 ? (b - c) : (b + c))))) < a))))))))) {
                return INVALID;
            } else {
                return SCALENE;
            }
        }
        if ((m82 ? (trian >= 3) : (m81 ? (trian <= 3) : (m80 ? (trian < 3) : (m79 ? (trian != 3) : (m78 ? (trian == 3) : (trian > 3)))))))
            return EQUILATERAL;
        if ((m97 ? ((m87 ? (trian >= 1) : (m86 ? (trian <= 1) : (m85 ? (trian > 1) : (m84 ? (trian < 1) : (m83 ? (trian != 1) : (trian == 1)))))) || (m96 ? ((m91 ? (a % b) : (m90 ? (a / b) : (m89 ? (a * b) : (m88 ? (a - b) : (a + b))))) >= c) : (m95 ? ((m91 ? (a % b) : (m90 ? (a / b) : (m89 ? (a * b) : (m88 ? (a - b) : (a + b))))) <= c) : (m94 ? ((m91 ? (a % b) : (m90 ? (a / b) : (m89 ? (a * b) : (m88 ? (a - b) : (a + b))))) < c) : (m93 ? ((m91 ? (a % b) : (m90 ? (a / b) : (m89 ? (a * b) : (m88 ? (a - b) : (a + b))))) != c) : (m92 ? ((m91 ? (a % b) : (m90 ? (a / b) : (m89 ? (a * b) : (m88 ? (a - b) : (a + b))))) == c) : ((m91 ? (a % b) : (m90 ? (a / b) : (m89 ? (a * b) : (m88 ? (a - b) : (a + b))))) > c))))))) : ((m87 ? (trian >= 1) : (m86 ? (trian <= 1) : (m85 ? (trian > 1) : (m84 ? (trian < 1) : (m83 ? (trian != 1) : (trian == 1)))))) && (m96 ? ((m91 ? (a % b) : (m90 ? (a / b) : (m89 ? (a * b) : (m88 ? (a - b) : (a + b))))) >= c) : (m95 ? ((m91 ? (a % b) : (m90 ? (a / b) : (m89 ? (a * b) : (m88 ? (a - b) : (a + b))))) <= c) : (m94 ? ((m91 ? (a % b) : (m90 ? (a / b) : (m89 ? (a * b) : (m88 ? (a - b) : (a + b))))) < c) : (m93 ? ((m91 ? (a % b) : (m90 ? (a / b) : (m89 ? (a * b) : (m88 ? (a - b) : (a + b))))) != c) : (m92 ? ((m91 ? (a % b) : (m90 ? (a / b) : (m89 ? (a * b) : (m88 ? (a - b) : (a + b))))) == c) : ((m91 ? (a % b) : (m90 ? (a / b) : (m89 ? (a * b) : (m88 ? (a - b) : (a + b))))) > c)))))))))
            return ISOSCELES;
        else if ((m112 ? ((m102 ? (trian >= 2) : (m101 ? (trian <= 2) : (m100 ? (trian > 2) : (m99 ? (trian < 2) : (m98 ? (trian != 2) : (trian == 2)))))) || (m111 ? ((m106 ? (a % c) : (m105 ? (a / c) : (m104 ? (a * c) : (m103 ? (a - c) : (a + c))))) >= b) : (m110 ? ((m106 ? (a % c) : (m105 ? (a / c) : (m104 ? (a * c) : (m103 ? (a - c) : (a + c))))) <= b) : (m109 ? ((m106 ? (a % c) : (m105 ? (a / c) : (m104 ? (a * c) : (m103 ? (a - c) : (a + c))))) < b) : (m108 ? ((m106 ? (a % c) : (m105 ? (a / c) : (m104 ? (a * c) : (m103 ? (a - c) : (a + c))))) != b) : (m107 ? ((m106 ? (a % c) : (m105 ? (a / c) : (m104 ? (a * c) : (m103 ? (a - c) : (a + c))))) == b) : ((m106 ? (a % c) : (m105 ? (a / c) : (m104 ? (a * c) : (m103 ? (a - c) : (a + c))))) > b))))))) : ((m102 ? (trian >= 2) : (m101 ? (trian <= 2) : (m100 ? (trian > 2) : (m99 ? (trian < 2) : (m98 ? (trian != 2) : (trian == 2)))))) && (m111 ? ((m106 ? (a % c) : (m105 ? (a / c) : (m104 ? (a * c) : (m103 ? (a - c) : (a + c))))) >= b) : (m110 ? ((m106 ? (a % c) : (m105 ? (a / c) : (m104 ? (a * c) : (m103 ? (a - c) : (a + c))))) <= b) : (m109 ? ((m106 ? (a % c) : (m105 ? (a / c) : (m104 ? (a * c) : (m103 ? (a - c) : (a + c))))) < b) : (m108 ? ((m106 ? (a % c) : (m105 ? (a / c) : (m104 ? (a * c) : (m103 ? (a - c) : (a + c))))) != b) : (m107 ? ((m106 ? (a % c) : (m105 ? (a / c) : (m104 ? (a * c) : (m103 ? (a - c) : (a + c))))) == b) : ((m106 ? (a % c) : (m105 ? (a / c) : (m104 ? (a * c) : (m103 ? (a - c) : (a + c))))) > b)))))))))
            return ISOSCELES;
        else if ((m127 ? ((m117 ? (trian >= 3) : (m116 ? (trian <= 3) : (m115 ? (trian > 3) : (m114 ? (trian < 3) : (m113 ? (trian != 3) : (trian == 3)))))) || (m126 ? ((m121 ? (b % c) : (m120 ? (b / c) : (m119 ? (b * c) : (m118 ? (b - c) : (b + c))))) >= a) : (m125 ? ((m121 ? (b % c) : (m120 ? (b / c) : (m119 ? (b * c) : (m118 ? (b - c) : (b + c))))) <= a) : (m124 ? ((m121 ? (b % c) : (m120 ? (b / c) : (m119 ? (b * c) : (m118 ? (b - c) : (b + c))))) < a) : (m123 ? ((m121 ? (b % c) : (m120 ? (b / c) : (m119 ? (b * c) : (m118 ? (b - c) : (b + c))))) != a) : (m122 ? ((m121 ? (b % c) : (m120 ? (b / c) : (m119 ? (b * c) : (m118 ? (b - c) : (b + c))))) == a) : ((m121 ? (b % c) : (m120 ? (b / c) : (m119 ? (b * c) : (m118 ? (b - c) : (b + c))))) > a))))))) : ((m117 ? (trian >= 3) : (m116 ? (trian <= 3) : (m115 ? (trian > 3) : (m114 ? (trian < 3) : (m113 ? (trian != 3) : (trian == 3)))))) && (m126 ? ((m121 ? (b % c) : (m120 ? (b / c) : (m119 ? (b * c) : (m118 ? (b - c) : (b + c))))) >= a) : (m125 ? ((m121 ? (b % c) : (m120 ? (b / c) : (m119 ? (b * c) : (m118 ? (b - c) : (b + c))))) <= a) : (m124 ? ((m121 ? (b % c) : (m120 ? (b / c) : (m119 ? (b * c) : (m118 ? (b - c) : (b + c))))) < a) : (m123 ? ((m121 ? (b % c) : (m120 ? (b / c) : (m119 ? (b * c) : (m118 ? (b - c) : (b + c))))) != a) : (m122 ? ((m121 ? (b % c) : (m120 ? (b / c) : (m119 ? (b * c) : (m118 ? (b - c) : (b + c))))) == a) : ((m121 ? (b % c) : (m120 ? (b / c) : (m119 ? (b * c) : (m118 ? (b - c) : (b + c))))) > a)))))))))
            return ISOSCELES;
        return INVALID;
    }
}