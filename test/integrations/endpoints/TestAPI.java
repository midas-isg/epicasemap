package integrations.endpoints;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import integrations.app.App;

import java.util.Iterator;

import javax.persistence.EntityManager;

import models.entities.CoordinateTime;

import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback;
import play.libs.F.Function0;
import play.libs.ws.WSResponse;
import play.test.FakeApplication;
import _imperfactcoverage.Helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

public class TestAPI {
	private static int numberCoordinateTimes = 6;

	@Test
	public void testLimitCoordinateTimes() {
		testCoordinateTimes(() -> actThenAssertLimitCoordinateTimes());
	}
	
	private void actThenAssertLimitCoordinateTimes() {
		int limit = 5;
		String url = makeCoordinateTimesUrl();
		assertLimit(url + "?limit=" + limit, limit);
		String offsetWithLimitUrl = url + "?offset=" + limit + "&limit=" + limit;
		assertLimit(offsetWithLimitUrl, numberCoordinateTimes - limit);
	}

	private void assertLimit(String url, int limit) {
		JsonNode root = Helper.get(url).asJson();
		JsonNode results = root.get("results");
		assertThat(results.size()).isEqualTo(limit);
	}

	@Test
	public void testCoordinateTimes() {
		testCoordinateTimes(() -> actThenAssertCoordinateTimes());
	}

	private void testCoordinateTimes(Runnable actThenAssert) {
		FakeApplication app = App.newWithInMemoryDb().getFakeApplication();
		running(testServer(3333, app), () -> testCoordinateTime1(actThenAssert));
	}

	private void testCoordinateTime1(Runnable actThenAssert) {
		Function0<Long> tryBlock = () -> testCoordinateTimes1_1(actThenAssert);
		Callback<Long> finallyBlock = (x) -> {};
		Helper.wrapTry(tryBlock, finallyBlock);
	}

	private Long testCoordinateTimes1_1(Runnable actThenAssert) {
		Long id = createCoordinateTimes();
		actThenAssert.run();
		return id;
	}

	private void actThenAssertCoordinateTimes() {
		String url = makeCoordinateTimesUrl();
		WSResponse response = Helper.get(url);
		assertThat(response.getHeader("Content-Type")).contains("application/json");
		JsonNode root = response.asJson();
		assertThat(root.getNodeType()).isSameAs(JsonNodeType.OBJECT);
		assertDefaultFilter(root.get("filter"));
		assertCoordinateTimes(root.get("results"));
	}

	private void assertDefaultFilter(JsonNode filter) {
		assertThat(filter.getNodeType()).isSameAs(JsonNodeType.OBJECT);
		assertThat(filter.get("limit").getNodeType()).isSameAs(JsonNodeType.NULL);
		assertThat(filter.get("offset").asInt()).isEqualTo(0);
	}

	private String makeCoordinateTimesUrl() {
		String context = Helper.readContext();
		return "http://localhost:3333" + context + "/api/coordinate-times";
	}

	private void assertCoordinateTimes(JsonNode results) {
		assertThat(results.getNodeType()).isSameAs(JsonNodeType.ARRAY);
		assertThat(results).isNotEmpty();
		assertThat(results.size()).isEqualTo(numberCoordinateTimes);
		JsonNode node = results.get(0);
		Iterator<String> fieldNames = node.fieldNames();
		String idName = "id";
		assertThat(fieldNames).contains(idName, "timestamp", "latitude", "longitude");
		assertThat(node.get(idName).asLong()).isPositive();
	}

	private Long createCoordinateTimes() {
		CoordinateTime data = Helper.wrapTransaction(() -> createCoordinateTimes1());
		return data.getId();
	}

	private CoordinateTime createCoordinateTimes1() {
		EntityManager em = JPA.em();
		CoordinateTime data = null;
		for (int i = 0; i < numberCoordinateTimes; i++){
			data = new CoordinateTime();
			em.persist(data);
		}
		return data;
	}
}
