package mutated.triangle;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
    testTriangle.class,
    testTriangleExhaustive.class
})

public class TriangleTestSuite {
}
