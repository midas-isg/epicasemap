package integrations.server.endpoints;

import static com.fasterxml.jackson.databind.node.JsonNodeType.ARRAY;
import static com.fasterxml.jackson.databind.node.JsonNodeType.NULL;
import static com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT;
import static java.time.Instant.EPOCH;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static suites.Helper.assertEqualTo;
import static suites.Helper.assertNodeType;

import java.util.Iterator;

import org.junit.Test;

import play.libs.ws.WSResponse;
import suites.Helper;

import com.fasterxml.jackson.databind.JsonNode;

public class TestTimeCoordinate {
	private final long seriesId = 2L;
	
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
		assertNodeType(root, OBJECT);
		JsonNode results = root.get("results");
		assertNodeType(results, ARRAY);
		int size = results.size();
		if (size > 0 )
			assertCoordinates(results);
		assertThat(size).isGreaterThanOrEqualTo(min);
		if (max != null)
			assertThat(size).isLessThanOrEqualTo(max);
		return root;
	}

	private void assertDefaultFilter(JsonNode filter) {
		assertNodeType(filter, OBJECT);
		assertNodeType(filter.get("limit"), NULL);
		assertEqualTo(filter.get("offset").asInt(), 0);
		assertNodeType(filter.get("startTimestampInclusive"), NULL);
		assertNodeType(filter.get("endTimestampExclusive"), NULL);
		assertEqualTo(filter.get("timestampAttribute").asText(), "timestamp");
		final JsonNode equalities = filter.get("equalities");
		assertNodeType(equalities, OBJECT);
		assertEqualTo(equalities.get("seriesId").asLong(), seriesId);
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
		return "http://localhost:3333" + context 
				+ "/api/series/" + seriesId + "/time-coordinate";
	}
}
