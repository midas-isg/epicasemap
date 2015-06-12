package suites;

import gateways.database.TestCoordinateTimeDao;
import interactors.TestConfRule;
import models.entities.TestCoordinate;
import models.entities.TestCoordinateTime;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import controllers.TestFactory;
import controllers.TestResponseWrapper;

@RunWith(Suite.class)
@SuiteClasses({
	TestResponseWrapper.class,
	TestCoordinateTimeDao.class,
	TestCoordinateTime.class,
	TestCoordinate.class,
	TestConfRule.class,
	TestFactory.class,
	CoverageBooster.class
})
public class UnitTests {
}
