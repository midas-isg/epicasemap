package integrations.app.controllers;

import static org.fest.assertions.Assertions.assertThat;
import integrations.app.App;

import javax.persistence.EntityManager;

import models.entities.Series;

import org.junit.BeforeClass;
import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback0;

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
    	EntityManager em = JPA.em();
		testReadSeries(em, theData);
	}

	private void testReadSeries(EntityManager em, Series expected) {
		long id = expected.getId();
		Series actual = em.find(expected.getClass(), id);
		assertThat(actual).isEqualTo(expected);
		assertThat(actual).isNotSameAs(expected);
	}

    private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}
}
