package original.triangle;

import mutated.triangle.*;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class testTriangle {
    public testTriangle() {

    }
    @Test
    public void test1() {
        mutated.triangle.Triangle triangle = new mutated.triangle.Triangle();
        int type = triangle.triangle(3, 3, 3);
        assertEquals(mutated.triangle.Triangle.EQUILATERAL, type);
    }

    @Test
    public void test2() {
        mutated.triangle.Triangle triangle = new mutated.triangle.Triangle();
        int type = triangle.triangle(3, 4, 5);
        assertEquals(mutated.triangle.Triangle.SCALENE, type);
    }

    @Test
    public void test3() {
        mutated.triangle.Triangle triangle = new mutated.triangle.Triangle();
        int type = triangle.triangle(2, 4, 4);
        assertEquals(mutated.triangle.Triangle.ISOSCELES, type);
    }

    @Test
    public void test4() {
        mutated.triangle.Triangle triangle = new mutated.triangle.Triangle();
        int type = triangle.triangle(3, 2, 3);
        assertEquals(mutated.triangle.Triangle.ISOSCELES, type);
    }

    @Test
    public void test5() {
        mutated.triangle.Triangle triangle = new mutated.triangle.Triangle();
        int type = triangle.triangle(3, 7, 3);
        assertEquals(mutated.triangle.Triangle.INVALID, type);
    }

    @Test
    public void test6() {
        mutated.triangle.Triangle triangle = new mutated.triangle.Triangle();
        int type = triangle.triangle(3, 3, 1);
        assertEquals(mutated.triangle.Triangle.ISOSCELES, type);
    }
}