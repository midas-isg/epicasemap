package integrations.app.controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;
import integrations.app.App;

import javax.persistence.EntityManager;

import models.entities.CoordinateTime;

import org.junit.BeforeClass;
import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.Result;
import _imperfactcoverage.Detour;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.API;

public class TestCoordinateTime {
	private static int numberCoordinateTimes = 6;

	@BeforeClass
	public static void populateDatabase(){
		runWithTransaction(() -> createCoordinateTimes());
	}
	
	private static void createCoordinateTimes() {
		EntityManager em = JPA.em();
		for (int i = 0; i < numberCoordinateTimes; i++){
			CoordinateTime data = new CoordinateTime();
			em.persist(data);
		}
	}

    @Test
    public void testDefaultParameters() {
    	runWithTransaction(() -> {
    		Result result = API.getCoordinateTimes(null, 0);
    		assertCoordinateTimes(result, numberCoordinateTimes);
    	});
    }

    @Test
    public void testLimits() {
    	runWithTransaction(() -> {
    		final int N = numberCoordinateTimes;
			testLimit(N, N);
    		testLimit(N + 1, N);
    		testLimit(N - 1, N - 1);
    		testLimit(0, N);
    	});
    }

    @Test
    public void testLimitWithNegative() {
		runWithTransaction(Detour.testLimitWithNegative(this));
    }
    
	@Test
    public void testLimitsAndOffsets() {
    	runWithTransaction(() -> {
    		final int N = numberCoordinateTimes;
    		testLimitAndOffset(N, 1, N - 1);

    		testLimitAndOffset(N, N - 1, 1);
    		testLimitAndOffset(null, N - 1, 1);
    	});
    }

	public void testLimit(int limit, int expected) {
		int offset = 0;
		testLimitAndOffset(limit, offset, expected);
	}

	private void testLimitAndOffset(Integer limit, int offset, int expected) {
		Result result = API.getCoordinateTimes(limit, offset);
		assertCoordinateTimes(result, expected);
	}

	private void assertCoordinateTimes(Result result, int n) {
		String content = contentAsString(result);
		JsonNode root = Json.parse(content);
		JsonNode results = root.get("results");
		assertThat(results.size()).isEqualTo(n);
	}

    private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}
}
