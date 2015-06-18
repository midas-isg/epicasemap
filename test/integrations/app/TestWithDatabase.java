package integrations.app;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;

import javax.persistence.EntityManager;

import models.entities.Coordinate;
import models.entities.CoordinateTime;

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
    		testRead1CoordinateTimeViaAPI(em);
    		testReadCoordinate(em, 1L);
    	};
    	App.newWithTestDb().runWithTransaction(callback);
    }

	private void testRead1CoordinateTimeViaAPI(EntityManager em) {
		Result result = API.getCoordinateTimes(null, null, 1, 0);
		JsonNode results = toJsonNode(result).get("results");
		assertThat(results.size()).isGreaterThan(0);
		JsonNode node = results.get(0);
		long id = node.get("id").asLong();
		testReadCoordinateTime(em, id);
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
    		testCreateCoordinateTime(em);
    	};
    	App.newWithInMemoryDb().runWithTransaction(callback);
    }

	private void testCreateCoordinateTime(EntityManager em) {
		long id = persistNewCoordinateTime(em);
		testReadCoordinateTime(em, id);
		testRead1CoordinateTimeViaAPI(em);
	}

	private void testReadCoordinateTime(EntityManager em, long id) {
		CoordinateTime data = Factory.makeCoordinateTimeDao(em).find(id);
		assertThat(data.getId()).isEqualTo(id);
	}

	private long persistNewCoordinateTime(EntityManager em) {
		CoordinateTime data = new CoordinateTime();
		em.persist(data);
		return data.getId();
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
