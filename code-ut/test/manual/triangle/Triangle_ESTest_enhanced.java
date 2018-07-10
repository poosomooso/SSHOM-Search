package manual.triangle;

import org.junit.Test;
import static org.junit.Assert.*;

public class Triangle_ESTest_enhanced {

  @Test(timeout = 4000)
  public void test00()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(1, 688, 1);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test01()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(1, 1, 2);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test02()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(2842, 2154, 688);
    assertEquals(2, int0);
  }

  @Test(timeout = 4000)
  public void test03()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(1, 3, 2);
    assertEquals(2, int0);
  }

  @Test(timeout = 4000)
  public void test04()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(2, 1, 3);
    assertEquals(2, int0);
  }

  @Test(timeout = 4000)
  public void test05()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(1, 1, (-703));
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test06()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(688, (-2710), 2842);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test07()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle((-2710), (-419), 1);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test08()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(1720, 1618, 1618);
    assertEquals(1, int0);
  }

  @Test(timeout = 4000)
  public void test09()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(1172, 3, 3);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test10()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(1, 2, 1);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test11()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(1172, 1172, 2004);
    assertEquals(1, int0);
  }

  @Test(timeout = 4000)
  public void test12()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(1, 1, 2004);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test13()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(1124, 1, 108);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test14()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(1172, 3, 2004);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test15()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(2, 3133, 1);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test16()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(1124, 1124, 1124);
    assertEquals(3, int0);
  }

  @Test(timeout = 4000)
  public void test17()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(1, 1, 0);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test18()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(2, 0, 0);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test19()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(3133, 2, 3133);
    assertEquals(1, int0);
  }

  @Test(timeout = 4000)
  public void test20()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(0, 0, 3133);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void customTest0()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(0, 2, 0);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void customTest1()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(2, 0, 2);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void customTest2()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(0, 0, 0);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void customTest3()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(0, 2, 2);
    assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void customTest4()  throws Throwable  {
    Triangle triangle0 = new Triangle();
    int int0 = triangle0.triangle(0, 0, -2);
    assertEquals(0, int0);
  }
}
