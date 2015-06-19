package integrations.server;

import static com.fasterxml.jackson.databind.node.JsonNodeType.NULL;
import static com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT;
import static java.time.Instant.EPOCH;
import static org.fest.assertions.Assertions.assertThat;
import static suites.Helper.assertAreEqual;
import static suites.Helper.assertNodeType;
import static suites.Helper.testJsonResponseLimit;
import static suites.Helper.testJsonResponseMin;

import java.util.Iterator;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class TestEndpointTimeCoordinate {
	private final long seriesId = 2L;
	
	@Test
	public void dateRange() {
		Server.run(() -> testDateRange());
	}
	
	public void testDateRange() {
		String ts = EPOCH.toString();
		String url = makeUrl("?startInclusive=" + ts + "&endExclusive=" + ts);
		testJsonResponseLimit(url, 0);
	}

	@Test
	public void pagination() {
		Server.run(() -> testPagination());
	}
	
	private void testPagination() {
		int n = 5;
		String url = makeUrl("?offset=" + n + "&limit=" + n);
		testJsonResponseLimit(url, n);
	}

	@Test
	public void defaultParameters() {
		Server.run(() -> testDefaultParameters());
	}

	public void testDefaultParameters() {
		JsonNode root = testJsonResponseMin(makeUrl(""), 1);
		JsonNode results = root.get("results");
		assertCoordinates(results);
		assertDefaultFilter(root.get("filter"));
	}

	private String makeUrl(String queries) {
		final String path = "/api/series/" + seriesId + "/time-coordinate";
		return Server.makeTestUrl(path + queries);
	}

	private void assertCoordinates(JsonNode results) {
		assertThat(results).isNotEmpty();
		JsonNode node = results.get(0);
		Iterator<String> fields = node.fieldNames();
		String idName = "id";
		assertThat(fields).containsOnly(idName, "seriesId", "value", "timestamp", "latitude", "longitude");
		assertThat(node.get(idName).asLong()).isPositive();
	}

	private void assertDefaultFilter(JsonNode filter) {
		assertNodeType(filter, OBJECT);
		assertNodeType(filter.get("limit"), NULL);
		assertAreEqual(filter.get("offset").asInt(), 0);
		assertNodeType(filter.get("startTimestampInclusive"), NULL);
		assertNodeType(filter.get("endTimestampExclusive"), NULL);
		assertAreEqual(filter.get("timestampAttribute").asText(), "timestamp");
		final JsonNode equalities = filter.get("equalities");
		assertNodeType(equalities, OBJECT);
		assertAreEqual(equalities.get("seriesId").asLong(), seriesId);
	}
}
