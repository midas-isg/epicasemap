package endpoints;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import java.util.Iterator;

import integrations.app.App;

import javax.persistence.EntityManager;

import models.entities.Geotag;

import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback;
import play.libs.F.Function0;
import play.test.FakeApplication;
import _imperfactcoverage.Helper;

import com.fasterxml.jackson.databind.JsonNode;

public class TestAPI {
	@Test
	public void testGeotags() {
		FakeApplication app = App.newWithInMemoryDb().getFakeApplication();
		running(testServer(3333, app), () -> testGeotags1());
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
		assertTrue(root.isArray());
		assertThat(root).isNotEmpty();
		JsonNode node = root.get(0);
		Iterator<String> fieldNames = node.fieldNames();
		String idName = "id";
		assertThat(fieldNames).contains(idName, "timestamp", "latitude", "longitude");
		assertThat(node.get(idName).asLong()).isPositive();
		return id;
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
		EntityManager em = JPA.em();
		em.remove(em.find(Geotag.class, id));
	}
}
