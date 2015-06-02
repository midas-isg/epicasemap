package unit;

import interactors.TestConfRule;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import views.TestIndexPage;

@RunWith(Suite.class)
@SuiteClasses({TestConfRule.class, TestIndexPage.class})
public class UnitTests {

}
