package integrations.server;

import static com.fasterxml.jackson.databind.node.JsonNodeType.NULL;
import static com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT;
import static java.time.Instant.EPOCH;
import static org.fest.assertions.Assertions.assertThat;
import static suites.Helper.assertAreEqual;
import static suites.Helper.assertNodeType;
import static suites.Helper.assertValueRange;
import static suites.Helper.testJsonResponseLimit;
import static suites.Helper.testJsonResponseMin;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

public class TimeCoordinateEndpointTester {
	private final long seriesId = 2L;
	
	private static TimeCoordinateEndpointTester newInstance() {
		return new TimeCoordinateEndpointTester();
	}
	
	public static Runnable dateRange() {
		return () -> newInstance().testDateRange();
	}

	private void testDateRange() {
		String ts = EPOCH.toString();
		String url = makeUrl("?startInclusive=" + ts + "&endExclusive=" + ts);
		testJsonResponseLimit(url, 0);
	}

	public static Runnable pagination() {
		return () -> newInstance().testPagination();
	}
	
	private void testPagination() {
		int n = 5;
		String url = makeUrl("?offset=" + n + "&limit=" + n);
		testJsonResponseLimit(url, n);
	}

	public static Runnable defaultParameters() {
		return () -> newInstance().testDefaultParameters();
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
		assertFields(results.get(0));
	}

	private void assertFields(JsonNode node) {
		Iterator<String> fields = node.fieldNames();
		final String id = "id";
		final String sid = "seriesId";
		final String val = "value";
		final String ts = "timestamp";
		final String lat = "latitude";
		final String lon = "longitude";
		assertThat(fields).containsOnly(id, sid, val, ts, lat, lon);
		assertThat(node.get(id).asLong()).isPositive();
		assertThat(node.get(sid).asLong()).isPositive();
		assertThat(node.get(val).asDouble()).isPositive();
		assertThat(node.get(ts).asLong()).isPositive();
		assertValueRange(node.get(lat), 90.0, -90.0);
		assertValueRange(node.get(lon), 180.0, -180.0);
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
