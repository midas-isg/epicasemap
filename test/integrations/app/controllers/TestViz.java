package integrations.app.controllers;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;
import static suites.Helper.assertAreEqual;
import static suites.Helper.assertArrayNode;
import static suites.Helper.assertTextNode;
import static suites.Helper.detachThenAssertWithDatabase;
import integrations.app.App;

import java.util.List;

import javax.persistence.EntityManager;

import models.entities.Series;
import models.entities.Visualization;
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

	private static Visualization persistThenDetachNewViz() {
		EntityManager em = JPA.em();
		Visualization data = new Visualization();
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
			Visualization persistedData = persistThenDetachNewViz();
			testRead(persistedData);
		});
	}

	@Test
	public void update() {
		runWithTransaction(() -> {
			Visualization persistedData = persistThenDetachNewViz();
			testUpdate(persistedData.getId());
		});
	}

	@Test
	public void delete() {
		runWithTransaction(() -> {
			Visualization dataForDelete = persistThenDetachNewViz();
			testDelete(dataForDelete.getId());
		});
	}

	@Test
	public void crud() {
		runWithTransaction(() -> testCrud());
	}

	@Test
	public void createComplex() throws Exception {
		Visualization data = new Visualization();
		runWithTransaction(() -> {
			Series s1 = TestSeries.persistThenDetachNewSeries();
			List<Series> list = asList(s1);
			data.setAllSeries(list);
			data.setTitle("complex");
			final long id = actCreate(data);
			data.setId(id);
		});

		runWithTransaction(() -> detachAndAssertWithDatabase(data));
	}

	private void testCrud() {
		Visualization data = testCreate();
		testRead(data);
		final Long id = data.getId();
		testUpdate(id);
		testDelete(id);
	}

	private Visualization testCreate() {
		return testCreate(new Visualization());
	}

	private Visualization testCreate(Visualization newData) {
		final long id = actCreate(newData);
		newData.setId(id);
		detachAndAssertWithDatabase(newData);
		return newData;
	}

	private void detachAndAssertWithDatabase(Visualization expected) {
		detachThenAssertWithDatabase(expected.getId(), expected);
	}

	private long actCreate(Visualization newData) {
		VizInput input = ApiViz.from(newData);
		return ApiViz.create(input);
	}

	private void testRead(Visualization expected) {
		long id = expected.getId();

		final Result response = ApiViz.read(id);

		final String content = contentAsString(response);
		final JsonNode root = Json.parse(content);
		final JsonNode data = root.get("result");
		assertAreEqual(data.get("id").asLong(), id);
		assertTextNode(data.get("title"), expected.getTitle());
		assertArrayNode(data.get("allSeries"), expected.getAllSeries(),
				Series.class);
	}

	private void testUpdate(long id) {
		VizInput dataToUpdate = ApiViz.from(new Visualization());
		dataToUpdate.setTitle("title");
		Visualization viz = ApiViz.update(id, dataToUpdate);
		detachThenAssertWithDatabase(id, viz);
	}

	private void testDelete(long id) {
		ApiViz.deleteById(id);

		final EntityManager em = JPA.em();
		Visualization del = em.find(Visualization.class, id);
		assertThat(del).isNull();
	}
}
