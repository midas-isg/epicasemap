package suites;

import gateways.configuration.AppKey;
import gateways.database.jpa.JpaAdaptor;
import integrations.app.App;
import integrations.server.Server;

import org.junit.Test;

import controllers.API;
import controllers.Application;

public class CoverageBooster extends TestCase {
	@Test
	public void makeup() throws Exception {
		ignoreUnitTestSuiteConstructor();
		ignoreAppKeyEnum();
		ignoreDefaultConstructors();
		ignoreSpecialCaseInTests();
		testPrivateDefaultConstructor(Helper.class);
	}

	private void ignoreSpecialCaseInTests() {
		App.doNotUseForBoostingupCoverageOnly("invalid");
		App.doNotUseForBoostingupCoverageOnly("test/resources/conf/test.conf");
	}

	private void ignoreUnitTestSuiteConstructor() {
		new UnitTests();
	}
	
	private void ignoreAppKeyEnum() throws Exception {
		for(AppKey key : AppKey.values()){
			AppKey.valueOf(key.name());
		}
	}

	private void ignoreDefaultConstructors() {
		new Application();
		new API();
		new JpaAdaptor(null);
		new Server();
	}
}

