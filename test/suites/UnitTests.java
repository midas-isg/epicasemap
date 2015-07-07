package suites;

import gateways.database.TestCoordinateDao;
import interactors.TestConfRule;
import interactors.TestVizRule;
import models.entities.TestCoordinate;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	TestCoordinateDao.class,
	TestCoordinate.class,
	TestConfRule.class,
	TestVizRule.class,
	CoverageBooster.class
})
public class UnitTests {
}
