package integrations.app.controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;
import gateways.database.jpa.JpaAdaptor;
import integrations.app.App;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.EntityManager;

import models.entities.Coordinate;
import models.filters.CoordinateFilter;
import models.filters.Filter;

import org.junit.BeforeClass;
import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.Result;
import _imperfactcoverage.Detour;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.ApiTimeCoordinateSeries;

public class TestCoordinate {
	private static int numberCoordinates = 6;

	@BeforeClass
	public static void populateDatabase(){
		runWithTransaction(() -> createCoordinates());
	}
	
	private static void createCoordinates() {
		EntityManager em = JPA.em();
		Instant t = Instant.EPOCH;
		for (int i = 0; i < numberCoordinates; i++){
			Coordinate data = new Coordinate();
			t = t.plus(1, ChronoUnit.DAYS);
			data.setTimestamp(Date.from(t));
			em.persist(data);
		}
	}
	
	@Test
	public void testDateRanges() throws Exception {
		runWithTransaction(() -> {
			Instant t = Instant.EPOCH;
			String start = t.toString();
			int n = 5;
			String end = t.plus(n, ChronoUnit.DAYS).toString();
    		Result result = ApiTimeCoordinateSeries.get(null, start, end, null, 0);
    		assertCoordinates(result, n - 1);
    	});
	}

    @Test
    public void testDefaultParameters() {
    	runWithTransaction(() -> {
    		Result result = ApiTimeCoordinateSeries.get(null, null, null, null, 0);
    		assertCoordinates(result, numberCoordinates);
    	});
    }

    @Test
    public void testLimits() {
    	runWithTransaction(() -> {
    		final int N = numberCoordinates;
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
    		final int N = numberCoordinates;
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
		Result result = ApiTimeCoordinateSeries.get(null, null, null, limit, offset);
		assertCoordinates(result, expected);
	}

	private void assertCoordinates(Result result, int n) {
		String content = contentAsString(result);
		JsonNode root = Json.parse(content);
		JsonNode results = root.get("results");
		assertThat(results).hasSize(n);
	}

    private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}
    
    @Test
	public void testOrder() throws Exception {
    	runWithTransaction(() -> {
    		final JpaAdaptor jpa = new JpaAdaptor(JPA.em());
    		CoordinateFilter filter = new CoordinateFilter();
    		final String timestamp = "timestamp";
    		filter.setTimestampAttribute(timestamp);
    		filter.setOffset(0);
    		filter.setEqualities(java.util.Collections.emptyMap());
    		LinkedHashMap<String, Filter.Order> order = new LinkedHashMap<>();
			order.put(timestamp, Filter.Order.DESC);
    		filter.setOrder(order);
    		List<Coordinate> results = jpa.query(Coordinate.class, filter);
    		Coordinate privious = results.get(0);
    		for (Coordinate result : results){
    			assertThat(result.getTimestamp().getTime()).isLessThanOrEqualTo(privious.getTimestamp().getTime());
    		}
    	});
	}
}
