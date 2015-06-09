package suites;

import impl.TestFactory;
import interactors.TestConfRule;
import models.entities.TestCoordinate;
import models.entities.TestGeotag;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	TestGeotag.class,
	TestCoordinate.class,
	TestConfRule.class,
	TestFactory.class
})
public class UnitTests {
}
