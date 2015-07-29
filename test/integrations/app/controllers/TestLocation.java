package integrations.app.controllers;

import static org.fest.assertions.Assertions.assertThat;
import integrations.app.App;

import javax.persistence.EntityManager;

import models.entities.Location;

import org.junit.BeforeClass;
import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback0;


public class TestLocation {
	private static Location theData;
	
	@BeforeClass
	public static void populateDatabase(){
		runWithTransaction(() -> persistNewLocation());
	}
	
	private static Location persistNewLocation() {
		EntityManager em = JPA.em();
		theData = new Location();
		//theData.setId(1L);
		em.persist(theData);
		return theData;
	}

	
    @Test
    public void create() {
		runWithTransaction(() -> testCreateLocation());
    }
    
	private void testCreateLocation() {
    	testReadLocation(JPA.em(), theData);
	}

	private void testReadLocation(EntityManager em, Location expected) {
		long id = expected.getId();
		Location actual = em.find(expected.getClass(), id);
		assertThat(actual.getId()).isEqualTo(expected.getId());
		assertThat(actual).isNotSameAs(expected);
	}

    private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}
}
