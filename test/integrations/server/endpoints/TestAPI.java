package integrations.server.endpoints;

import static com.fasterxml.jackson.databind.node.JsonNodeType.ARRAY;
import static com.fasterxml.jackson.databind.node.JsonNodeType.NULL;
import static com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import java.util.Iterator;

import org.junit.Test;

import play.libs.ws.WSResponse;
import _imperfactcoverage.Helper;

import com.fasterxml.jackson.databind.JsonNode;

public class TestAPI {
	@Test
	public void testLimitCoordinateTimes() {
		testCoordinateTimes(() -> actThenAssertLimitCoordinateTimes());
	}
	
	private void actThenAssertLimitCoordinateTimes() {
		int n = 5;
		String url = makeCoordinateTimesUrl() + "?offset=" + n + "&limit=" + n;
		assertLimit(url, n);
	}

	private JsonNode assertMin(String url, int min) {
		return assertClosedInterval(url, min, null);
	}
	
	private JsonNode assertLimit(String url, int limit) {
		return assertClosedInterval(url, limit, limit);
	}
	
	private JsonNode assertClosedInterval(String url, int min, Integer max) {
		WSResponse response = Helper.get(url);
		assertThat(response.getHeader("Content-Type")).contains("application/json");
		JsonNode root = response.asJson();
		assertThat(root.getNodeType()).isSameAs(OBJECT);
		assertCoordinateTimes(root.get("results"));
		JsonNode results = root.get("results");
		int size = results.size();
		assertThat(size).isGreaterThanOrEqualTo(min);
		if (max != null)
			assertThat(size).isLessThanOrEqualTo(max);
		return root;
	}
	
	private void assertDefaultFilter(JsonNode filter) {
		assertThat(filter.getNodeType()).isSameAs(OBJECT);
		assertThat(filter.get("limit").getNodeType()).isSameAs(NULL);
		assertThat(filter.get("offset").asInt()).isEqualTo(0);
	}
	
	private void assertCoordinateTimes(JsonNode results) {
		assertThat(results.getNodeType()).isSameAs(ARRAY);
		assertThat(results).isNotEmpty();
		JsonNode node = results.get(0);
		Iterator<String> fields = node.fieldNames();
		String idName = "id";
		assertThat(fields).containsOnly(idName, "timestamp", "latitude", "longitude");
		assertThat(node.get(idName).asLong()).isPositive();
	}


	@Test
	public void testCoordinateTimes() {
		testCoordinateTimes(() -> actThenAssertCoordinateTimes());
	}

	private void testCoordinateTimes(Runnable actThenAssert) {
		running(testServer(3333), actThenAssert);
	}

	private void actThenAssertCoordinateTimes() {
		JsonNode root = assertMin(makeCoordinateTimesUrl(), 1);
		assertDefaultFilter(root.get("filter"));
	}

	private String makeCoordinateTimesUrl() {
		String context = Helper.readContext();
		return "http://localhost:3333" + context + "/api/coordinate-times";
	}
}
