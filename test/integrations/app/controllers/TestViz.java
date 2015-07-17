package integrations.app.controllers;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;
import static suites.Helper.assertAreEqual;
import static suites.Helper.assertArrayNode;
import static suites.Helper.assertTextNode;
import static suites.Helper.detachThenAssertWithDatabase;
import integrations.app.App;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import models.entities.Series;
import models.entities.Viz;
import models.view.VizInput;

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

	private static Viz persistThenDetachNewViz() {
		EntityManager em = JPA.em();
		Viz data = new Viz();
		em.persist(data);
		em.detach(data);
		return data;
	}

	@Test
	public void correctInputTypeName() throws Exception {
		assertAreEqual(ApiViz.inputType, VizInput.class.getName());
	}

	@Test
	public void create() {
		runWithTransaction(() -> testCreate());
	}

	@Test
	public void read() {
		runWithTransaction(() -> {
			Viz persistedData = persistThenDetachNewViz();
			testRead(persistedData);
		});
	}

	@Test
	public void update() {
		runWithTransaction(() -> {
			Viz persistedData = persistThenDetachNewViz();
			testUpdate(persistedData.getId());
		});
	}

	@Test
	public void delete() {
		runWithTransaction(() -> {
			Viz dataForDelete = persistThenDetachNewViz();
			testDelete(dataForDelete.getId());
		});
	}

	@Test
	public void crud() {
		runWithTransaction(() -> testCrud());
	}

	@Test
	public void createComplex() throws Exception {
		Viz data = new Viz();
		runWithTransaction(() -> {
			Series s1 = TestSeries.persistThenDetachNewSeries();
			List<Series> list = asList(s1);
			data.setAllSeries(list);
			data.setAllSeries2(Collections.emptyList());
			data.setName("complex");
			final long id = actCreate(data);
			data.setId(id);
		});

		runWithTransaction(() -> detachAndAssertWithDatabase(data));
	}

	private void testCrud() {
		Viz data = testCreate();
		testRead(data);
		final Long id = data.getId();
		testUpdate(id);
		testDelete(id);
	}

	private Viz testCreate() {
		return testCreate(new Viz());
	}

	private Viz testCreate(Viz newData) {
		final long id = actCreate(newData);
		newData.setId(id);
		detachAndAssertWithDatabase(newData);
		return newData;
	}

	private void detachAndAssertWithDatabase(Viz expected) {
		detachThenAssertWithDatabase(expected.getId(), expected);
	}

	private long actCreate(Viz newData) {
		VizInput input = ApiViz.from(newData);
		return ApiViz.create(input);
	}

	private void testRead(Viz expected) {
		long id = expected.getId();

		final Result response = ApiViz.read(id);

		final String content = contentAsString(response);
		final JsonNode root = Json.parse(content);
		final JsonNode data = root.get("result");
		assertAreEqual(data.get("id").asLong(), id);
		assertTextNode(data.get("name"), expected.getName());
		assertArrayNode(data.get("allSeries"), expected.getAllSeries(),
				Series.class);
	}

	private void testUpdate(long id) {
		Viz dataToUpdate = new Viz();
		dataToUpdate.setName("name");
		ApiViz.update(id, dataToUpdate);
		detachThenAssertWithDatabase(id, dataToUpdate);
	}

	private void testDelete(long id) {
		ApiViz.deleteById(id);

		final EntityManager em = JPA.em();
		Viz del = em.find(Viz.class, id);
		assertThat(del).isNull();
	}
}
