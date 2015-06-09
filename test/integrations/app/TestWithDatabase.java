package integrations.app;

import static org.fest.assertions.Assertions.assertThat;

import javax.persistence.EntityManager;

import models.entities.Coordinate;

import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback0;

public class TestWithDatabase {
    @Test
    public void readEntitiesFromTestDatabase() {
    	Callback0 callback = () -> {
    		EntityManager em = JPA.em();
    		testReadCoordinate(em, 1L);
    	};
    	App.newWithTestDb().runWithTransaction(callback);
    }

	private void testReadCoordinate(EntityManager em, long id) {
		Coordinate c = em.find(Coordinate.class, id);
		assertThat(c.getId()).isEqualTo(id);
	}
	
    @Test
    public void readEntitiesFromInMemoryDatabase() {
    	Callback0 callback = () -> {
    		EntityManager em = JPA.em();
    		Coordinate original = persistNewCoordinate(em);
    		testReadCoordinate(em, original.getId());
    	};
    	App.newWithInMemoryDb().runWithTransaction(callback);
    }

	private Coordinate persistNewCoordinate(EntityManager em) {
		Coordinate original = new Coordinate();
		original.setId(1L);
		em.persist(original);
		return original;
	}
}
