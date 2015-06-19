package integrations.app;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;

import javax.persistence.EntityManager;

import models.entities.Coordinate;
import models.entities.Location;
import models.entities.Series;
import models.entities.SeriesData;

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
    		long id = testRead1CoordinateViaAPI();
    		EntityManager em = JPA.em();
    		testReadCoordinate(em, id);
    		testReadSeries(em, 1L);
    		testReadLocation(em, 1L);
    		testReadSeriesData(em, 2L);
    	};
    	App.newWithTestDb().runWithTransaction(callback);
    }

	private void testReadSeriesData(EntityManager em, long id) {
		SeriesData data = em.find(SeriesData.class, id);
		assertThat(data.getId()).isEqualTo(id);
		Coordinate cs = em.find(Coordinate.class, id);
		assertThat(cs.getId()).isEqualTo(id);
	}

	private void testReadLocation(EntityManager em, long id) {
		Location data = em.find(Location.class, id);
		assertThat(data.getId()).isEqualTo(id);
	}

	private void testReadSeries(EntityManager em, long id) {
		Series data = em.find(Series.class, id);
		assertThat(data.getId()).isEqualTo(id);
	}

	private long testRead1CoordinateViaAPI() {
		Result result = API.getTimeCoordinateSeries(null, null, null, 1, 0);
		JsonNode results = toJsonNode(result).get("results");
		assertThat(results.size()).isGreaterThan(0);
		JsonNode node = results.get(0);
		long id = node.get("id").asLong();
		return id;
	}

	private JsonNode toJsonNode(Result result) {
		String content = contentAsString(result);
		JsonNode root = Json.parse(content);
		return root;
	}

    @Test
    public void createEntitiesIntoInMemoryDatabase() {
    	Callback0 callback = () -> {
    		EntityManager em = JPA.em();
    		testCreateCoordinate(em);
    	};
    	App.newWithInMemoryDb().runWithTransaction(callback);
    }

	private void testCreateCoordinate(EntityManager em) {
		long id = persistNewCoordinate(em);
		testReadCoordinate(em, id);
		testRead1CoordinateViaAPI();
	}

	private void testReadCoordinate(EntityManager em, long id) {
		Coordinate data = Factory.makeCoordinateDao(em).find(id);
		assertThat(data.getId()).isEqualTo(id);
	}

	private long persistNewCoordinate(EntityManager em) {
		Coordinate data = new Coordinate();
		em.persist(data);
		return data.getId();
	}
}
