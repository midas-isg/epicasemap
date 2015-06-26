package integrations.app.controllers;

import static com.fasterxml.jackson.databind.node.JsonNodeType.NULL;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;
import static suites.Helper.assertAreEqual;
import static suites.Helper.assertNodeType;
import integrations.app.App;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import models.entities.Series;
import models.entities.Viz;

import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.ApiViz;

public class TestViz {
    private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}

	private static Viz persistNewViz() {
		EntityManager em = JPA.em();
		Viz data = new Viz();
		em.persist(data);
		em.detach(data);
		return data;
	}
	
    @Test
	public void create() {
		runWithTransaction(() -> testCreate());
	}

	@Test
	public void read() {
		runWithTransaction(() -> {
			Viz persistedData = persistNewViz();
			testRead(persistedData);
		});
	}

	@Test
	public void update() {
		runWithTransaction(() -> {
			Viz persistedData = persistNewViz();
			testUpdate(persistedData.getId());
		});
	}

	@Test
	public void delete() {
		runWithTransaction(() -> {
			Viz dataForDelete = persistNewViz();
			testDelete(dataForDelete.getId());
		});
	}

    @Test
    public void crud() {
		runWithTransaction(() -> testCrud());
    }
    
    @Test
	public void createComplex() throws Exception {
		runWithTransaction(() -> {
			Series s1 = TestSeries.persistNewSeries();
			List<Series> list = asList(s1);
			Viz data = new Viz();
			data.setAllSeries(list);
			testCreate();
		});
	}
    
    
	private <T> List<T> asList(@SuppressWarnings("unchecked") T... ts) {
		List<T> list = new ArrayList<>();
		for (T t : ts)
			list.add(t);
		return list;
	}

	private void testCrud() {
		Viz data = testCreate();
		testRead(data);
		final Long id = data.getId();
		testUpdate(id);
		testDelete(id);
	}

	private Viz testCreate() {
		Viz newData = new Viz();
		testCreate(newData);
		return newData;
	}

	public void testCreate(Viz newData) {
		final long id = ApiViz.create(newData);
		
		EntityManager em = JPA.em();
		em.detach(newData);
		assertVizIsEqaulTo(em, id, newData);
	}

	private void testRead(Viz expected) {
		long id = expected.getId();
		
		final Result response = ApiViz.read(id);
		
		final String content = contentAsString(response);
		final JsonNode root = Json.parse(content);
		final JsonNode data = root.get("result");
		assertAreEqual(data.get("id").asLong(), id);
		assertTextNode(data.get("name"), expected.getName());
	}

	private void testUpdate(long id) {
		Viz dataToUpdate = new Viz();
		dataToUpdate.setName("name");
		ApiViz.update(id, dataToUpdate);
		
		final EntityManager em = JPA.em();
		em.detach(dataToUpdate);
		assertVizIsEqaulTo(em, id, dataToUpdate);
	}

	private void testDelete(long id) {
		ApiViz.deleteById(id);

		final EntityManager em = JPA.em();
		Viz del = em.find(Viz.class, id);
		assertThat(del).isNull();
	}

	private void assertVizIsEqaulTo(EntityManager em, long id, Viz expected) {
		Viz found = em.find(Viz.class, id);
		assertAreEqual(found, expected);
	}

	private void assertTextNode(JsonNode actual, String expected) {
		if (expected == null)
			assertNodeType(actual, NULL);
		else 
			assertAreEqual(actual.asText(), expected);
	}
}
