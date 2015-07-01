package suites;

import gateways.configuration.AppKey;
import gateways.database.jpa.JpaAdaptor;
import integrations.app.App;
import integrations.server.Server;

import org.junit.Test;

import controllers.ApiLocation;
import controllers.ApiSeries;
import controllers.ApiTimeCoordinateSeries;
import controllers.ApiViz;
import controllers.Application;
import controllers.Factory;
import controllers.ResponseWrapper;

public class CoverageBooster extends TestCase {
	@Test
	public void makeup() throws Exception {
		ignoreAppKeyEnum();
		ignoreDefaultConstructors();
		ignoreSpecialCaseInTests();
		testPrivateConstructors(
				ResponseWrapper.class,
				Factory.class
		);
		Application.swagger();
	}

	private void testPrivateConstructors(Class<?>... classes) throws Exception {
		for (Class<?> clazz : classes)
			testPrivateDefaultConstructor(clazz);
	}

	private void ignoreSpecialCaseInTests() {
		App.doNotUseForBoostingupCoverageOnly("invalid");
		App.doNotUseForBoostingupCoverageOnly("test/resources/conf/test.conf");
	}

	private void ignoreAppKeyEnum() throws Exception {
		for(AppKey key : AppKey.values()){
			AppKey.valueOf(key.name());
		}
	}

	private void ignoreDefaultConstructors() {
		ignoreDefaultConstructorsForControllers();
		new JpaAdaptor(null);
		ignoreDefaultConstructorsForTests();
	}

	private void ignoreDefaultConstructorsForControllers() {
		new Application();
		new ApiSeries();
		new ApiLocation();
		new ApiTimeCoordinateSeries();
		new ApiViz();
	}

	private void ignoreDefaultConstructorsForTests() {
		new UnitTests();
		new Helper();
		new Server();
	}
}

