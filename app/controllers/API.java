package controllers;

import interactors.GeotagRule;

import java.util.List;

import models.entities.Geotag;
import models.entities.GeotagFilter;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class API extends Controller {
	@Transactional
    public static Result getGeotags(Integer limit, int offset) {
		GeotagRule rule = Factory.makeGeotagRule(JPA.em());
		GeotagFilter filter = new GeotagFilter();
		filter.setLimit(limit);
		filter.setOffset(offset);
        List<Geotag> results = rule.query(filter);
		Object response = ResponseWrapper.wrap(results, filter);
		return ok(Json.toJson(response));
    }
}

