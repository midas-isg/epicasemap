package integrations.server;

import static com.fasterxml.jackson.databind.node.JsonNodeType.ARRAY;
import static com.fasterxml.jackson.databind.node.JsonNodeType.NULL;
import static com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT;
import static org.fest.assertions.Assertions.assertThat;
import static play.libs.Json.toJson;
import static play.mvc.Http.HeaderNames.LOCATION;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.NO_CONTENT;
import static play.mvc.Http.Status.OK;
import static suites.Helper.assertAreEqual;
import static suites.Helper.assertNodeType;
import static suites.Helper.assertTextNode;
import static suites.Helper.testJsonArrayResponse;
import static suites.Helper.testJsonObjectResponse;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import models.entities.Series;
import models.view.VizInput;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import suites.Helper;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.ApiSeries;

public class VizEndpointTester {
	private static final int timeout = 100_000;
	private final String basePath = "/api/vizs";

	public static Runnable test() {
		return () -> newInstance().testOnServer();
	}

	private void testOnServer() {
		testUpdateUiSetting();
		testCrud();
		testFindAll();
	}

	private void testUpdateUiSetting() {
		Tuple data = testCreate();
		long id = data.id;
		final String subpath = "/ui-setting";
		final String seriesUrl = urlWithId(id);
		final String url = seriesUrl + subpath;
		testRead(data);
		Map<String, Object> setting = new HashMap<>();
		setting.put("key", "value");
		final JsonNode json = toJson(setting);
		final String input = json.toString();
		data.input.setUiSetting(input);
		final WSResponse update = WS.url(url).put(json).get(timeout);
		assertAreEqual(update.getStatus(), NO_CONTENT);
		testRead(data);
		final String location = update.getHeader("LOCATION");
		assertThat(location).endsWith(id + subpath);
		final WSResponse read = assertStatusOfGet(url, OK);
		assertJsonString(read.getBody(), input);
		assertStatusOfGet(urlWithId(0) + subpath, NOT_FOUND);
		WS.url(url).put(input).get(timeout);
	}

	private WSResponse assertStatusOfGet(String url, final int expected) {
		final WSResponse wsResponse = WS.url(url).get().get(timeout);
		assertAreEqual(wsResponse.getStatus(), expected);
		return wsResponse;
	}

	private void assertJsonString(final String actual, final String expected) {
		assertAreEqual(actual, expected);
	}

	private long testFindAll() {
		final JsonNode root = testJsonArrayResponse(baseUrl());
		JsonNode results = root.get("results");
		assertThat(results.size()).isGreaterThanOrEqualTo(1);
		return results.get(0).get("id").asLong();
	}

	private Tuple testCrud() {
		final Tuple data = testCreate();
		testRead(data);
		testUpdate(data);
		testDelete(data.id);
		return data;
	}

	private void testUpdate(Tuple pair) {
		final String url = urlWithId(pair.id);
		final String name = "update name";
		final VizInput data = pair.input;
		data.setTitle(name);
		final WSResponse update = WS.url(url).put(toJson(data)).get(timeout);
		assertAreEqual(update.getStatus(), NO_CONTENT);
	}

	private Tuple testCreate() {
		VizInput input = new VizInput();
		List<Series> all = Helper.wrapTransaction(() -> {
			return ApiSeries.find(null);
		});

		assertThat(all.size()).isGreaterThanOrEqualTo(2);
		input.setSeriesIds(toList(all.subList(0, 1), it -> it.getId()));
		input.setSeries2Ids(toList(all.subList(0, 2), it -> it.getId()));
		input.setTitle("Test first 2 Series");
		input.setUiSetting("{}");
		final JsonNode root = Json.toJson(input);
		final String json = root + "";
		toFile("./public/examples/" + basePath + ".json", json);
		final String url = baseUrl();
		final WSResponse create = WS.url(url).post(root).get(timeout);
		assertAreEqual(create.getStatus(), CREATED);
		final String location = create.getHeader(LOCATION);
		final long id = toId(location);
		final String path = append(basePath, id);
		assertThat(location).endsWith(path);
		final Tuple pair = new Tuple();
		pair.id = id;
		pair.input = input;
		return pair;
	}

	private void toFile(final String filePath, final String content) {
		Helper.wrapNoThrowingCheckedExecption(() -> Files.write(
				Paths.get(filePath),
				content.getBytes()));
	}

	private List<Long> toList(final List<Series> input,
			final Function<? super Series, ? extends Long> mapper) {
		return input.stream().map(mapper).collect(Collectors.toList());
	}

	private void testRead(Tuple pair) {
		long id = pair.id;
		final VizInput expected = pair.input;
		final String urlWithId = urlWithId(id);
		final JsonNode root = testJsonObjectResponse(urlWithId);
		assertNodeType(root.get("filter"), NULL);
		final JsonNode result = root.get("result");
		assertNodeType(result, OBJECT);
		assertAreEqual(result.get("id").asLong(), id);
		assertAllSeries(result.get("allSeries"), expected.getSeriesIds());
		assertTextNode(result.get("uiSetting"), expected.getUiSetting());
	}

	public void assertAllSeries(final JsonNode allSeries, List<Long> expected) {
		assertNodeType(allSeries, ARRAY);
		assertThat(allSeries).hasSize(expected.size());
		for (JsonNode series : allSeries) {
			assertNodeType(series, OBJECT);
			assertThat(series.get("id").asLong()).isIn(expected);
		}
	}

	private void testDelete(long id) {
		final WSResponse delete = WS.url(urlWithId(id)).delete().get(timeout);
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
		final String number = tokens[tokens.length - 1];
		return Long.parseLong(number);
	}

	private static class Tuple {
		public Long id;
		public VizInput input;
	}
}
