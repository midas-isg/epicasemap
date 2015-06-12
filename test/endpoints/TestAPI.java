package endpoints;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import integrations.app.App;

import java.util.Iterator;

import javax.persistence.EntityManager;

import models.entities.Geotag;

import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback;
import play.libs.F.Function0;
import play.test.FakeApplication;
import _imperfactcoverage.Helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

public class TestAPI {
	private static int numberGeotags = 6;

	@Test
	public void testLimitGeotags() {
		testGeotags(() -> actThenAssertLimitGeotags());
	}
	
	private void actThenAssertLimitGeotags() {
		int limit = 5;
		String url = makeGeotagsUrl();
		assertLimit(url + "?limit=" + limit, limit);
		String offsetWithLimitUrl = url + "?offset=" + limit + "&limit=" + limit;
		assertLimit(offsetWithLimitUrl, numberGeotags - limit);
	}

	private void assertLimit(String url, int limit) {
		JsonNode root = Helper.get(url).asJson();
		JsonNode results = root.get("results");
		assertThat(results.size()).isEqualTo(limit);
	}

	@Test
	public void testGeotags() {
		testGeotags(() -> actThenAssertGeotags());
	}

	private void testGeotags(Runnable actThenAssert) {
		FakeApplication app = App.newWithInMemoryDb().getFakeApplication();
		running(testServer(3333, app), () -> testGeotags1(actThenAssert));
	}

	private void testGeotags1(Runnable actThenAssert) {
		Function0<Long> tryBlock = () -> testGeotags1_1(actThenAssert);
		Callback<Long> finallyBlock = (x) -> {};
		Helper.wrapTry(tryBlock, finallyBlock);
	}

	private Long testGeotags1_1(Runnable actThenAssert) {
		Long id = createGeotags();
		actThenAssert.run();
		return id;
	}

	private void actThenAssertGeotags() {
		String url = makeGeotagsUrl();
		JsonNode root = Helper.get(url).asJson();
		assertThat(root.getNodeType()).isSameAs(JsonNodeType.OBJECT);
		JsonNode filter = root.get("filter");
		assertDefaultFilter(filter);
		assertGeotags(root.get("results"));
	}

	private void assertDefaultFilter(JsonNode filter) {
		assertThat(filter.getNodeType()).isSameAs(JsonNodeType.OBJECT);
		assertThat(filter.get("limit").getNodeType()).isSameAs(JsonNodeType.NULL);
		assertThat(filter.get("offset").asInt()).isEqualTo(0);
	}

	private String makeGeotagsUrl() {
		String context = Helper.readContext();
		return "http://localhost:3333" + context + "/api/geotags";
	}

	private void assertGeotags(JsonNode results) {
		assertThat(results.getNodeType()).isSameAs(JsonNodeType.ARRAY);
		assertThat(results).isNotEmpty();
		assertThat(results.size()).isEqualTo(numberGeotags);
		JsonNode node = results.get(0);
		Iterator<String> fieldNames = node.fieldNames();
		String idName = "id";
		assertThat(fieldNames).contains(idName, "timestamp", "latitude", "longitude");
		assertThat(node.get(idName).asLong()).isPositive();
	}

	private Long createGeotags() {
		Geotag geotag = Helper.wrapTransaction(() -> createGeotags1());
		return geotag.getId();
	}

	private Geotag createGeotags1() {
		EntityManager em = JPA.em();
		Geotag data = null;
		for (int i = 0; i < numberGeotags; i++){
			data = new Geotag();
			em.persist(data);
		}
		return data;
	}
}
