package integrations.app;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;

import javax.persistence.EntityManager;

import models.entities.Coordinate;
import models.entities.Geotag;

import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.API;
import controllers.Factory;

public class TestWithDatabase {
    @Test
    public void readEntitiesFromTestDatabase() {
    	Callback0 callback = () -> {
    		EntityManager em = JPA.em();
    		testReadAllGeotagsViaAPI(em);
    		testReadCoordinate(em, 1L);
    	};
    	App.newWithTestDb().runWithTransaction(callback);
    }

	private void testReadAllGeotagsViaAPI(EntityManager em) {
		Result result = API.getGeotags();
		JsonNode root = toJsonNode(result);
		assertThat(root.size()).isGreaterThan(0);
		JsonNode node = root.get(0);
		long id = node.get("id").asLong();
		testReadGeotag(em, id);
	}

	private JsonNode toJsonNode(Result result) {
		String content = contentAsString(result);
		JsonNode root = Json.parse(content);
		return root;
	}

	private void testReadCoordinate(EntityManager em, long id) {
		Coordinate data = em.find(Coordinate.class, id);
		assertThat(data.getId()).isEqualTo(id);
	}
	
    @Test
    public void createEntitiesIntoInMemoryDatabase() {
    	Callback0 callback = () -> {
    		EntityManager em = JPA.em();
    		testCreateCoordinate(em);
    		testCreateGeotag(em);
    	};
    	App.newWithInMemoryDb().runWithTransaction(callback);
    }

	private void testCreateGeotag(EntityManager em) {
		long id = persistNewGeotag(em);
		testReadGeotag(em, id);
		testReadAllGeotagsViaAPI(em);
	}

	private void testReadGeotag(EntityManager em, long id) {
		Geotag data = Factory.makeGeotagDao(em).find(id);
		assertThat(data.getId()).isEqualTo(id);
	}

	private long persistNewGeotag(EntityManager em) {
		Geotag original = new Geotag();
		em.persist(original);
		return original.getId();
	}

	private void testCreateCoordinate(EntityManager em) {
		Coordinate original = persistNewCoordinate(em);
		testReadCoordinate(em, original.getId());
	}

	private Coordinate persistNewCoordinate(EntityManager em) {
		Coordinate original = new Coordinate();
		original.setId(1L);
		em.persist(original);
		return original;
	}
}
