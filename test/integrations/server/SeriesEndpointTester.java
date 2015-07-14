package integrations.server;

import static com.fasterxml.jackson.databind.node.JsonNodeType.NULL;
import static com.fasterxml.jackson.databind.node.JsonNodeType.STRING;
import static org.fest.assertions.Assertions.assertThat;
import static suites.Helper.assertNodeType;
import static suites.Helper.testJsonObjectResponse;
import static suites.Helper.testJsonResponseMin;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

public class SeriesEndpointTester {
	private final String path = "/api/series";
	
	public static Runnable read() {
		return () -> newInstance().testRead();
	}

	private void testRead() {
		JsonNode root = testJsonObjectResponse(url() + "/1");
		assertDefaultFilter(root.get("filter"));
		assertSeriesObject(root.get("result"));
	}

	public static Runnable defaultParameters() {
		return () -> newInstance().testDefaultParameters();
	}

	private void testDefaultParameters() {
		JsonNode root = testJsonResponseMin(url(), 1);
		assertSeriesArray(root.get("results"));
		assertDefaultFilter(root.get("filter"));
	}

	private void assertSeriesArray(JsonNode results) {
		assertThat(results).isNotEmpty();
		JsonNode node = results.get(0);
		assertSeriesObject(node);
	}

	private void assertSeriesObject(JsonNode node) {
		Iterator<String> fields = node.fieldNames();
		final String idKey = "id";
		final String nameKey = "name";
		final String desKey = "description";
		assertThat(fields).containsOnly(idKey, nameKey, desKey);
		assertThat(node.get(idKey).asLong()).isPositive();
		
		assertNodeType(node.get(nameKey), STRING);
		assertNodeType(node.get(desKey), STRING, NULL);
	}

	private void assertDefaultFilter(JsonNode filter) {
		assertNodeType(filter, NULL);
	}
	
	private static SeriesEndpointTester newInstance() {
		return new SeriesEndpointTester();
	}
	
	private String url() {
		return Server.makeTestUrl(path);
	}
}
