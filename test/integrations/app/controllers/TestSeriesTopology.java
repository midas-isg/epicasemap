package integrations.app.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.ApiTopology;
import gateways.database.SeriesTopologyDao;
import integrations.app.App;
import models.entities.SeriesTopology;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.db.jpa.JPA;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.Result;

import javax.persistence.EntityManager;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;

public class TestSeriesTopology {
	private static final long theSeriesId = 1L;
	private static SeriesTopology theData;

	@BeforeClass
	public static void populateDatabase() {
		runWithTransaction(() -> theData = persistThenDetachNewSeries());
	}

	static SeriesTopology persistThenDetachNewSeries() {
		EntityManager em = JPA.em();
		final SeriesTopology data = new SeriesTopology();
		data.setSeriesId(theSeriesId);
		data.setTopoJson("{}");
		em.persist(data);
		em.detach(data);
		return data;
	}

	@Test
	public void readViaDao() {
		runWithTransaction(() -> testReadViaDao(theData));
	}

	private void testReadViaDao(SeriesTopology expected) {
		long id = expected.getId();
        EntityManager em = JPA.em();
        final SeriesTopology data = new SeriesTopologyDao(em).read(id);
		assertThat(data).isEqualTo(expected);
	}

    @Test
    public void readViaApi() {
        runWithTransaction(() -> testReadViaApi(theData));
    }

    private void testReadViaApi(SeriesTopology expected) {
        long id = expected.getId();
		final Result response = ApiTopology.read(theSeriesId);
        final String content = contentAsString(response);
        assertThat(content).isEqualTo(expected.getTopoJson());
    }

    @Test
    public void createViaApi() {
        runWithTransaction(() -> testCreateViaApi());
    }

    private void testCreateViaApi() {
        final JsonNode jsonNode = Json.parse("{\"gids\":[1, 2]}");
        final long seriesId = theSeriesId + 1;
        final String create = ApiTopology.linkToSeries(seriesId, jsonNode);
        assertTopoJson(create);
        final Result read = ApiTopology.read(seriesId);
        final String content = contentAsString(read);
        assertThat(content).isEqualTo(create);
    }

    private void assertTopoJson(String result) {
        final JsonNode parse = Json.parse(result);
        assertThat(parse.get("type").textValue()).isEqualTo("Topology");
    }

    private static void runWithTransaction(Callback0 callback) {
		final String className = TestSeriesTopology.class.getName();
		App.newWithInMemoryDb(className).runWithTransaction(callback);
	}

    @AfterClass
    public static void noNeedToCleanUpDatabaseDueToInMemory() throws Exception {
    }
}
