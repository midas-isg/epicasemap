package integrations.app;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

import javax.persistence.EntityManager;

import models.entities.Coordinate;

import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback0;

public class TestWithDatabase {
    @Test
    public void readEntitiesFromDatabase() {
    	Callback0 callback = () -> {
    		EntityManager em = JPA.em();
    		testReadCoordinate(em);
    	};
        running(fakeApplication(), withTransaction(callback));
    }

	private void testReadCoordinate(EntityManager em) {
		long id = 1L;
		Coordinate b = em.find(Coordinate.class, id);
		assertThat(b.getId()).isEqualTo(id);
	}

	private Runnable withTransaction(Callback0 callback) {
		return () -> {
			JPA.withTransaction(callback);
        };
	}
}
