package suites;

import impl.TestFactory;
import interactors.TestConfRule;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	TestConfRule.class,
	TestFactory.class
})
public class UnitTests {
}
