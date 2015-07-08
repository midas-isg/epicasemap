package integrations.server;

import static com.fasterxml.jackson.databind.node.JsonNodeType.ARRAY;
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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
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

	public static Runnable crud() {
		return () -> newInstance().testCrud();
	}

	private void testCrud() {
		final Tuple data = testCreate();
		testRead(data);
		testUpdate(data);
		testDelete(data.id);
	}

	private void testUpdate(Tuple pair) {
		final String url = urlWithId(pair.id);
		final String name = "update name";
		final VizInput data = pair.input;
		data.setName(name);
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
		input.setName("Test first 2 Series");
		final JsonNode json = Json.toJson(input);
		toFile("./public/examples/" + basePath + ".json", json + "");
		final String url = baseUrl();
		final WSResponse create = WS.url(url).post(json).get(timeout);
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
		final JsonNode root = testJsonResponse(urlWithId);
		assertNodeType(root.get("filter"), NULL);
		final JsonNode result = root.get("result");
		assertNodeType(result, OBJECT);
		assertAreEqual(result.get("id").asLong(), id);
		assertAllSeries(result.get("allSeries"), expected.getSeriesIds());
		assertAllSeries(result.get("allSeries2"), expected.getSeries2Ids());
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
