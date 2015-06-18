package controllers;

import interactors.CoordinateTimeRule;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import models.entities.CoordinateTime;
import models.entities.CoordinateTimeFilter;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class API extends Controller {
	@Transactional
	public static Result getCoordinateTimes(
			String startInclusive, 
			String endExclusive, 
			Integer limit, 
			int offset
	) {
		CoordinateTimeRule rule = Factory.makeCoordinateTimeRule(JPA.em());
		CoordinateTimeFilter filter = buildCoordinateTimeFilter(startInclusive, 
				endExclusive, limit, offset);
        List<CoordinateTime> results = rule.query(filter);
		Object response = ResponseWrapper.wrap(results, filter);
		return ok(Json.toJson(response));
	}

	private static CoordinateTimeFilter buildCoordinateTimeFilter(
			String startInclusive, 
			String endExclusive, 
			Integer limit, 
			int offset
	) {
		CoordinateTimeFilter filter = new CoordinateTimeFilter();
		filter.setStartInclusive(toDate(startInclusive));
		filter.setEndExclusiveDate(toDate(endExclusive));
		filter.setLimit(limit);
		filter.setOffset(offset);
		return filter;
	}

	private static Date toDate(String text) {
		if (text == null)
			return null;
		return Date.from(Instant.parse(text));
	}
}

