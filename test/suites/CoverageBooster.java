package suites;

import static java.util.Arrays.asList;
import static suites.Helper.assertArrayNode;
import gateways.configuration.AppKey;
import gateways.database.jpa.JpaAdaptor;
import integrations.app.App;
import integrations.server.Server;

import org.junit.Test;

import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.ApiLocation;
import controllers.ApiSeries;
import controllers.ApiTimeCoordinateSeries;
import controllers.ApiViz;
import controllers.Application;
import controllers.Factory;
import controllers.ResponseHelper;

public class CoverageBooster extends TestCase {
	@Test
	public void makeup() throws Exception {
		ignoreAppKeyEnum();
		ignoreDefaultConstructors();
		ignoreSpecialCaseInTests();
		testPrivateConstructors(
				ResponseHelper.class,
				Factory.class
		);
		Application.swagger();
		testHelper();
	}

	private void testHelper() {
		String[] texts = new String[]{"a", "b"};
		final JsonNode json = Json.toJson(texts);
		assertArrayNode(json, asList(texts), String.class);
		
		Helper.assertTextNode(json.get(0), "a");
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
		for (AppKey key : AppKey.values()) {
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
