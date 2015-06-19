package integrations.server;

import static com.fasterxml.jackson.databind.node.JsonNodeType.ARRAY;
import static com.fasterxml.jackson.databind.node.JsonNodeType.NULL;
import static com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static suites.Helper.assertNodeType;

import java.util.Iterator;

import org.junit.Test;

import play.libs.ws.WSResponse;
import suites.Helper;

import com.fasterxml.jackson.databind.JsonNode;

public class TestEndpointSeries {
	private JsonNode testJsonResponseMin(String url, int min) {
		WSResponse response = Helper.get(url);
		JsonNode root = response.asJson();
		assertThat(root.getNodeType()).isSameAs(OBJECT);
		JsonNode results = root.get("results");
		assertThat(results.getNodeType()).isSameAs(ARRAY);
		int size = results.size();
		assertSeries(results);
		assertThat(size).isGreaterThanOrEqualTo(min);
		return root;
	}
	
	private void assertDefaultFilter(JsonNode filter) {
		assertNodeType(filter, NULL);
		/*assertNodeType(filter, OBJECT);
		assertNodeType(filter.get("limit"), NULL);
		Helper.assertAreEqual(filter.get("offset").asInt(), 0);*/
	}
	
	private void assertSeries(JsonNode results) {
		assertThat(results).isNotEmpty();
		JsonNode node = results.get(0);
		Iterator<String> fields = node.fieldNames();
		String idName = "id";
		assertThat(fields).containsOnly(idName, "name", "description");
		assertThat(node.get(idName).asLong()).isPositive();
	}

	@Test
	public void defaultParameters() {
		testInServer(() -> actThenAssertDefaultParameters());
	}

	private void testInServer(Runnable actThenAssert) {
		running(testServer(3333), actThenAssert);
	}

	private void actThenAssertDefaultParameters() {
		JsonNode root = testJsonResponseMin(makeTimeCoordinatesUrl(), 1);
		assertDefaultFilter(root.get("filter"));
	}

	private String makeTimeCoordinatesUrl() {
		String context = Helper.readContext();
		return "http://localhost:3333" + context + "/api/series";
	}
}
