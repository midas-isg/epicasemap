package suites;

import gateways.database.TestCoordinateDao;
import interactors.TestConfRule;
import models.entities.TestCoordinate;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	TestCoordinateDao.class,
	TestCoordinate.class,
	TestConfRule.class,
	CoverageBooster.class
})
public class UnitTests {
}
