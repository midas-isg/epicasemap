package integrations.server;

import static com.fasterxml.jackson.databind.node.JsonNodeType.NULL;
import static com.fasterxml.jackson.databind.node.JsonNodeType.NUMBER;
import static com.fasterxml.jackson.databind.node.JsonNodeType.STRING;
import static org.fest.assertions.Assertions.assertThat;
import static suites.Helper.assertNodeType;
import static suites.Helper.assertValueRange;
import static suites.Helper.testJsonObjectResponse;

import java.util.Iterator;

import play.api.libs.json.Json;

import com.fasterxml.jackson.databind.JsonNode;

public class LocationEndpointTester {
	private final String path = "/api/locations";
	
	public static Runnable read() {
		return () -> newInstance().testRead();
	}

	private void testRead() {
		JsonNode root = testJsonObjectResponse(url() + "/1");
		assertDefaultFilter(root.get("filter"));
		assertLocation(root.get("result"));
	}

	private void assertDefaultFilter(JsonNode filter) {
		assertNodeType(filter, NULL);
	}

	private void assertLocation(JsonNode node) {
		Iterator<String> fields = node.fieldNames();
		final String id = "id";
		final String label = "label";
		final String alsId = "alsId";
		final String lat = "latitude";
		final String lon = "longitude";
		final String geojsonKey = "geojson";

		assertThat(fields).containsOnly(id, label, alsId, lat, lon, geojsonKey);
		
		assertThat(node.get(id).asLong()).isPositive();
		assertNodeType(node.get(label), STRING);
		final JsonNode alsIdNode = node.get(alsId);
		assertNodeType(alsIdNode, NUMBER, NULL);
		if (alsIdNode.isNumber())
			assertThat(alsIdNode.asLong()).isPositive();
		assertValueRange(node.get(lat), 90.0, -90.0);
		assertValueRange(node.get(lon), 180.0, -180.0);
		final JsonNode geojsonNode = node.get(geojsonKey);
		assertNodeType(geojsonNode, STRING, NULL);
		if (geojsonNode.isTextual())
			Json.parse(geojsonNode.asText());
	}
	
	private static LocationEndpointTester newInstance() {
		return new LocationEndpointTester();
	}
	
	private String url() {
		return Server.makeTestUrl(path);
	}
}
