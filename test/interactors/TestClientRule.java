package interactors;

import static play.mvc.Http.Status.OK;
import static suites.Helper.assertAreEqual;
import gateways.configuration.AppKey;
import integrations.server.Server;

import org.junit.Test;

import play.libs.ws.WSResponse;
import controllers.Factory;

public class TestClientRule {

	private String baseUrl;

	@Test
	public void test() {
		Runnable test = testGetById();
		Server.run(test);
	}

	private static Runnable testGetById() {
		return () -> newInstance().testGetResponse();
	}

	private void testGetResponse() {
		readUrl();
		ClientRule clientRule = new ClientRule(this.baseUrl);
		WSResponse wsResponse = clientRule.getById(1L);

		assertStatus(wsResponse, OK);

	}

	private void readUrl() {
		ConfRule confRule = Factory.makeConfRule();
		this.baseUrl = confRule.readString(AppKey.ALS_WS_URL.key()) + "/api/locations";
	}

	private void assertStatus(WSResponse wsResponse, int expected) {
		assertAreEqual(wsResponse.getStatus(), expected);
	}

	private static TestClientRule newInstance() {
		return new TestClientRule();
	}
}
