package suites;

import gateways.database.TestCoordinateDao;
import interactors.TestConfRule;
import models.entities.TestCoordinate;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import controllers.TestFactory;
import controllers.TestResponseWrapper;

@RunWith(Suite.class)
@SuiteClasses({
	TestResponseWrapper.class,
	TestCoordinateDao.class,
	TestCoordinate.class,
	TestConfRule.class,
	TestFactory.class,
	CoverageBooster.class
})
public class UnitTests {
}
