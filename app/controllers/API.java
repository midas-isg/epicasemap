package controllers;

import interactors.GeotagRule;

import java.util.List;

import models.entities.Geotag;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class API extends Controller {
	@Transactional
    public static Result getGeotags() {
		GeotagRule rule = Factory.makeGeotagRule(JPA.em());
        List<Geotag> results = rule.findAll();
		Object response = ResponseWrapper.wrap(results, null);
		return ok(Json.toJson(response));
    }
}
