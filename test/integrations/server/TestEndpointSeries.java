package integrations.server;

import static com.fasterxml.jackson.databind.node.JsonNodeType.NULL;
import static org.fest.assertions.Assertions.assertThat;
import static suites.Helper.assertNodeType;
import static suites.Helper.testJsonResponseMin;

import java.util.Iterator;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class TestEndpointSeries {
	private final String path = "/api/series";
	
	@Test
	public void defaultParameters() {
		Server.run(() -> actThenAssertDefaultParameters());
	}

	private void actThenAssertDefaultParameters() {
		JsonNode root = testJsonResponseMin(Server.makeTestUrl(path), 1);
		assertSeries(root.get("results"));
		assertDefaultFilter(root.get("filter"));
	}

	private void assertSeries(JsonNode results) {
		assertThat(results).isNotEmpty();
		JsonNode node = results.get(0);
		Iterator<String> fields = node.fieldNames();
		String idName = "id";
		assertThat(fields).containsOnly(idName, "name", "description");
		assertThat(node.get(idName).asLong()).isPositive();
	}

	private void assertDefaultFilter(JsonNode filter) {
		assertNodeType(filter, NULL);
		/*assertNodeType(filter, OBJECT);
		assertNodeType(filter.get("limit"), NULL);
		Helper.assertAreEqual(filter.get("offset").asInt(), 0);*/
	}
	
}
