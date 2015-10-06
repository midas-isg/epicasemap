package integrations.server;

import static com.fasterxml.jackson.databind.node.JsonNodeType.NULL;
import static com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT;
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
		assertSeriesObject(root.get("result"));
	}

	public static Runnable defaultParameters() {
		return () -> newInstance().testDefaultParameters();
	}

	private void testDefaultParameters() {		
		JsonNode root = testJsonResponseMin(url(), 1);
		assertSeriesArray(root.get("results"));
	}

	private void assertSeriesArray(JsonNode results) {
		assertThat(results).isNotEmpty();
		JsonNode node = results.get(0);
		assertSeriesObject(node);
	}

	private void assertSeriesObject(JsonNode node) {
		Iterator<String> fields = node.fieldNames();
		final String idKey = "id";
		final String titleKey = "title";
		final String desKey = "description";
		final String creatorKey = "creator";
		final String publisherKey = "publisher";
		final String versionKey = "version";
		final String licenseKey = "license";
		final String isVersionOfKey = "isVersionOf";
		final String owner = "owner";
		
		assertThat(fields).containsOnly(idKey, titleKey, desKey, creatorKey,
				publisherKey, licenseKey, versionKey, isVersionOfKey, owner);
		assertThat(node.get(idKey).asLong()).isPositive();
		
		assertNodeType(node.get(titleKey), STRING);
		assertNodeType(node.get(desKey), STRING, NULL);
		assertNodeType(node.get(desKey), STRING, NULL);
		assertNodeType(node.get(creatorKey), STRING, NULL);
		assertNodeType(node.get(publisherKey), STRING, NULL);
		assertNodeType(node.get(licenseKey), STRING, NULL);
		assertNodeType(node.get(versionKey), STRING, NULL);
		assertNodeType(node.get(isVersionOfKey), STRING, NULL);
		assertNodeType(node.get(owner), OBJECT, NULL);
	}

	private static SeriesEndpointTester newInstance() {
		return new SeriesEndpointTester();
	}
	
	private String url() {
		return Server.makeTestUrl(path);
	}
}
