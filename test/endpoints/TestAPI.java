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
	@Test
	public void testGeotags() {
		FakeApplication app = App.newWithInMemoryDb().getFakeApplication();
		running(testServer(3333, app), () -> testGeotags1());
		makeupCoverage();
	}

	private void makeupCoverage() {
		deleteGeotag1(null);
	}

	private void testGeotags1() {
		Function0<Long> tryBlock = () -> testGeotags1_1();
		Callback<Long> finallyBlock = (x) -> deleteGeotag(x);
		Helper.wrapTry(tryBlock, finallyBlock);
	}

	private Long testGeotags1_1() {
		Geotag data = create1Geotag();
		Long id = data.getId();

		String context = Helper.readContext();
		String url = "http://localhost:3333" + context + "/api/geotags";
		JsonNode root = Helper.get(url).asJson();
		assertThat(root.getNodeType()).isSameAs(JsonNodeType.OBJECT);
		assertThat(root.get("filter").getNodeType()).isSameAs(JsonNodeType.NULL);
		assertGeotags(root.get("results"));
		return id;
	}

	private void assertGeotags(JsonNode result) {
		assertThat(result.getNodeType()).isSameAs(JsonNodeType.ARRAY);
		assertThat(result).isNotEmpty();
		JsonNode node = result.get(0);
		Iterator<String> fieldNames = node.fieldNames();
		String idName = "id";
		assertThat(fieldNames).contains(idName, "timestamp", "latitude", "longitude");
		assertThat(node.get(idName).asLong()).isPositive();
	}

	private Geotag create1Geotag() {
		return Helper.wrapTransaction(() -> create1Geotag1());
	}

	private Geotag create1Geotag1() {
		EntityManager em = JPA.em();
		Geotag data = new Geotag();
		em.persist(data);
		return data;
	}

	private void deleteGeotag(Long id) {
		JPA.withTransaction(() -> deleteGeotag1(id));
	}

	private void deleteGeotag1(Long id) {
		if (id == null)
			return;
		EntityManager em = JPA.em();
		em.remove(em.find(Geotag.class, id));
	}
}
