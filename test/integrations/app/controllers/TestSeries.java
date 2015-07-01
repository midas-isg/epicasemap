package integrations.app.controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;
import integrations.app.App;

import javax.persistence.EntityManager;

import models.entities.Series;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.ApiSeries;
import play.db.jpa.JPA;
import play.libs.Json;
import play.libs.F.Callback0;
import play.mvc.Result;

public class TestSeries {
	private static Series theData;
	
	@BeforeClass
	public static void populateDatabase(){
		runWithTransaction(() -> theData = persistThenDetachNewSeries());
	}
	
	static Series persistThenDetachNewSeries() {
		EntityManager em = JPA.em();
		final Series data = new Series();
		data.setName("name");
		data.setDescription("description");
		em.persist(data);
		em.detach(data);
		return data;
	}
	
    @Test
    public void createSeries() {
		runWithTransaction(() -> testCreateSeries());
    }
    
	private void testCreateSeries() {
		testReadSeries(theData);
	}

	private void testReadSeries( Series expected) {
		long id = expected.getId();
		final Result response = ApiSeries.get();
		final String content = contentAsString(response);
		final JsonNode root = Json.parse(content);
		final JsonNode results = root.get("results");
		final JsonNode data0 = results.get(0);
		assertThat(data0.get("id").asLong()).isEqualTo(id);
	}

    private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}
}