package integrations.server.endpoints;

import static com.fasterxml.jackson.databind.node.JsonNodeType.ARRAY;
import static com.fasterxml.jackson.databind.node.JsonNodeType.NULL;
import static com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT;
import static java.time.Instant.EPOCH;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import java.util.Iterator;

import org.junit.Test;

import play.libs.ws.WSResponse;
import suites.Helper;

import com.fasterxml.jackson.databind.JsonNode;

public class TestTimeCoordinate {
	@Test
	public void dateRange() {
		testInServer(() -> actThenAssertDateRange());
	}
	
	private void actThenAssertDateRange() {
		String time = EPOCH.toString();
		String url = makeTimeCoordinatesUrl() 
		+ "?startInclusive=" + time + "&endExclusive=" + time;
		testJsonResponseLimit(url, 0);
	}

	@Test
	public void pagination() {
		testInServer(() -> actThenAssertPagination());
	}
	
	private void actThenAssertPagination() {
		int n = 5;
		String url = makeTimeCoordinatesUrl() + "?offset=" + n + "&limit=" + n;
		testJsonResponseLimit(url, n);
	}

	private JsonNode testJsonResponseMin(String url, int min) {
		return testJsonResponseClosedInterval(url, min, null);
	}
	
	private JsonNode testJsonResponseLimit(String url, int limit) {
		return testJsonResponseClosedInterval(url, limit, limit);
	}
	
	private JsonNode testJsonResponseClosedInterval(String url, int min, Integer max) {
		WSResponse response = Helper.get(url);
		JsonNode root = response.asJson();
		Helper.assertNodeType(root, OBJECT);
		JsonNode results = root.get("results");
		Helper.assertNodeType(results, ARRAY);
		int size = results.size();
		if (size > 0 )
			assertCoordinates(results);
		assertThat(size).isGreaterThanOrEqualTo(min);
		if (max != null)
			assertThat(size).isLessThanOrEqualTo(max);
		return root;
	}

	private void assertDefaultFilter(JsonNode filter) {
		Helper.assertNodeType(filter, OBJECT);
		Helper.assertNodeType(filter.get("limit"), NULL);
		assertThat(filter.get("offset").asInt()).isEqualTo(0);
	}
	
	private void assertCoordinates(JsonNode results) {
		assertThat(results).isNotEmpty();
		JsonNode node = results.get(0);
		Iterator<String> fields = node.fieldNames();
		String idName = "id";
		assertThat(fields).containsOnly(idName, "seriesId", "value", "timestamp", "latitude", "longitude");
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
		return "http://localhost:3333" + context + "/api/series/2/time-coordinate";
	}
}
