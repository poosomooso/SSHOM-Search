package manual.triangle.evosuite.original.triangle;

import manual.triangle.Triangle;
import org.junit.Test;
import static org.junit.Assert.*;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

//@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true, useJEE = true)
public class Triangle_ESTest/* extends Triangle_ESTest_scaffolding*/ {

  @Test(timeout = 4000)
  public void test00()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(2, 1437, 1437);
      assertEquals(1, int0);
  }

  @Test(timeout = 4000)
  public void test01()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(1, 2, 1);
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test02()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(1, 1, 2);
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test03()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(3, 2, 1);
      assertEquals(2, int0);
  }

  @Test(timeout = 4000)
  public void test04()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(2, 3, 1);
      assertEquals(2, int0);
  }

  @Test(timeout = 4000)
  public void test05()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(1, 2, 3);
      assertEquals(2, int0);
  }

  @Test(timeout = 4000)
  public void test06()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(1, 3, (-1212));
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test07()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(2262, (-1), 2262);
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test08()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle((-1204), 139, (-1204));
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test09()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(482, 3, 3);
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test10()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(1, 1088, 1);
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test11()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(69, 69, 408);
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test12()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(1088, 1039, 1);
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test13()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(1, 1039, 3);
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test14()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(1, 3, 1088);
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test15()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(1088, 1088, 1);
      assertEquals(1, int0);
  }

  @Test(timeout = 4000)
  public void test16()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(1088, 3, 1088);
      assertEquals(1, int0);
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
      int int0 = triangle0.triangle(1, 0, 1088);
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test19()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(1088, 1088, 1088);
      assertEquals(3, int0);
  }

  @Test(timeout = 4000)
  public void test20()  throws Throwable  {
      Triangle triangle0 = new Triangle();
      int int0 = triangle0.triangle(0, 3, 0);
      assertEquals(0, int0);
  }
}
