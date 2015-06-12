package suites;

import gateways.database.TestGeotagDao;
import interactors.TestConfRule;
import models.entities.TestCoordinate;
import models.entities.TestGeotag;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import controllers.TestFactory;
import controllers.TestResponseWrapper;

@RunWith(Suite.class)
@SuiteClasses({
	TestResponseWrapper.class,
	TestGeotagDao.class,
	TestGeotag.class,
	TestCoordinate.class,
	TestConfRule.class,
	TestFactory.class,
	CoverageBooster.class
})
public class UnitTests {
}
