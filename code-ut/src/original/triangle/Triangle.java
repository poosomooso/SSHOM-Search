package original.triangle;


public class Triangle {
    public final static int INVALID = 0;
    public final static int ISOSCELES = 1;
    public final static int SCALENE = 2;
    public final static int EQUILATERAL = 3;

    public int triangle(int a, int b, int c) {
        if (a <= 0 || b <= 0 || c <= 0) {
            return INVALID;
        }
        int trian = 0;
        if (a == b) {
            trian = trian + 1;
        }
        if (a == c) {
            trian = trian + 2;
        }
        if (b == c) {
            trian = trian + 3;
        }
        if (trian == 0) {
            if (a + b < c || a + c < b || b + c < a) {
                return INVALID;
            } else {
                return SCALENE;
            }
        }
        if (trian > 3) return EQUILATERAL;
        if (trian == 1 && a + b > c) return ISOSCELES;
        else if (trian == 2 && a + c > b) return ISOSCELES;
        else if (trian == 3 && b + c > a) return ISOSCELES;
        return INVALID;
    }

//    public static void main(String[] args) {
//        int i = 0;
//        String format = "@Test public void test%d() { Triangle original.triangle = new Triangle(); int type = original.triangle.original.triangle(%d, %d, %d); assertEquals(%s, type); }\n";
//
//        for (int a = -5; a <= 5; a++) {
//            for (int b = -5; b <= 5; b++) {
//                for (int c = -5; c <= 5; c++) {
//                    String res;
//                    switch (new Triangle().original.triangle(a, b, c)) {
//                    case ISOSCELES: res = "Triangle.ISOSCELES"; break;
//                    case SCALENE: res = "Triangle.SCALENE"; break;
//                    case EQUILATERAL: res = "Triangle.EQUILATERAL"; break;
//                    default: res = "Triangle.INVALID";
//                    }
//                    System.out.printf(format, i++, a, b, c, res);
//                }
//            }
//        }
//    }
}
