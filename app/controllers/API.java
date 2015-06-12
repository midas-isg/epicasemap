package controllers;

import interactors.CoordinateTimeRule;

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
    public static Result getCoordinateTimes(Integer limit, int offset) {
		CoordinateTimeRule rule = Factory.makeCoordinateTimeRule(JPA.em());
		CoordinateTimeFilter filter = buildCoordinateTimeFilter(limit, offset);
        List<CoordinateTime> results = rule.query(filter);
		Object response = ResponseWrapper.wrap(results, filter);
		return ok(Json.toJson(response));
    }

	private static CoordinateTimeFilter buildCoordinateTimeFilter(Integer limit, int offset) {
		CoordinateTimeFilter filter = new CoordinateTimeFilter();
		filter.setLimit(limit);
		filter.setOffset(offset);
		return filter;
	}
}

