package integrations.app.controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;
import integrations.app.App;

import javax.persistence.EntityManager;

import models.entities.Viz;

import org.junit.BeforeClass;
import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.ApiViz;

public class TestViz {
	private static Viz theData;
	
	@BeforeClass
	public static void populateDatabase(){
		runWithTransaction(() -> persistNewSeries());
	}
	
	private static Viz persistNewSeries() {
		EntityManager em = JPA.em();
		theData = new Viz();
		em.persist(theData);
		return theData;
	}
	
    @Test
    public void crud() {
		runWithTransaction(() -> testCrud());
    }
    
	private void testCrud() {
		final Long theId = theData.getId();
		testRead(theId, theData);
		Viz data = new Viz();
		final long id = ApiViz.create(data);
		testRead(id, data);
	}

	private void testRead(long id, Viz expected) {
		final Result response = ApiViz.read(id);
		final String content = contentAsString(response);
		final JsonNode root = Json.parse(content);
		final JsonNode data = root.get("result");
		assertThat(data.get("id").asLong()).isEqualTo(id);
	}

    private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}
}
