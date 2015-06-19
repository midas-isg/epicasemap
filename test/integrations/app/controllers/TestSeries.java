package integrations.app.controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;
import integrations.app.App;

import javax.persistence.EntityManager;

import models.entities.Series;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.API;
import play.db.jpa.JPA;
import play.libs.Json;
import play.libs.F.Callback0;
import play.mvc.Result;

public class TestSeries {
	private static Series theData;
	
	@BeforeClass
	public static void populateDatabase(){
		runWithTransaction(() -> persistNewSeries());
	}
	
	private static Series persistNewSeries() {
		EntityManager em = JPA.em();
		theData = new Series();
		theData.setId(1L);
		theData.setName("name");
		theData.setDescription("description");
		em.persist(theData);
		return theData;
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
		final Result response = API.getSeries();
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
