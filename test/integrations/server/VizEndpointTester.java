package integrations.server;

import static com.fasterxml.jackson.databind.node.JsonNodeType.NULL;
import static com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT;
import static org.fest.assertions.Assertions.assertThat;
import static play.libs.Json.toJson;
import static play.mvc.Http.HeaderNames.LOCATION;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.NO_CONTENT;
import static suites.Helper.assertAreEqual;
import static suites.Helper.assertNodeType;
import static suites.Helper.testJsonResponse;
import models.entities.Viz;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import com.fasterxml.jackson.databind.JsonNode;

public class VizEndpointTester {
	private static final int timeout = 100000;
	private final String basePath = "/api/vizs";
	
	public static Runnable crud() {
		return () -> newInstance().testCrud();
	}
	
	private void testCrud() {
		final Viz data = testCreate();
		long id = data.getId();
		testRead(id);
		testUpdate(data);
		testDelete(id);
	}

	private void testUpdate(Viz data) {
		long id = data.getId();
		final String url = urlWithId(id);
		final String name = "update name";
		data.setName(name);
		WSResponse update = WS.url(url).put(toJson(data)).get(timeout);
		assertAreEqual(update.getStatus(), NO_CONTENT);
	}

	private Viz testCreate() {
		Viz data = new Viz();
		final String url = baseUrl();
		WSResponse create = WS.url(url).post(toJson(data)).get(timeout);
		assertAreEqual(create.getStatus(), CREATED);
		final String location = create.getHeader(LOCATION);
		long id = toId(location);
		final String path = append(basePath, id);
		assertThat(location).endsWith(path);
		data.setId(id);
		return data;
	}

	private void testRead(long id) {
		final String urlWithId = urlWithId(id);
		final JsonNode root = testJsonResponse(urlWithId);
		assertNodeType(root.get("filter"), NULL);
		final JsonNode result = root.get("result");
		assertNodeType(result, OBJECT);
		assertAreEqual(result.get("id").asLong(), id);
	}

	private void testDelete(long id) {
		WSResponse delete = WS.url(urlWithId(id)).delete().get(timeout);
		assertAreEqual(delete.getStatus(), NO_CONTENT);
	}

	
	private static VizEndpointTester newInstance() {
		return new VizEndpointTester();
	}
	
	private String baseUrl() {
		return Server.makeTestUrl(basePath);
	}

	private String urlWithId(long id) {
		return Server.makeTestUrl(append(basePath, id));
	}

	private String append(final String path, long id) {
		return path + "/" + id;
	}

	private long toId(final String location) {
		final String[] tokens = location.split("/");
		String number = tokens[tokens.length - 1];
		return Long.parseLong(number);
	}
}
