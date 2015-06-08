package suites;

import impl.TestFactory;
import interactors.TestConfRule;
import models.entities.TestCoordinate;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	TestCoordinate.class,
	TestConfRule.class,
	TestFactory.class
})
public class UnitTests {
}
