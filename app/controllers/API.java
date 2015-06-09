package controllers;

import interactors.GeotagRule;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class API extends Controller {
	@Transactional
    public static Result getGeotags() {
		GeotagRule rule = Factory.makeGeotagRule(JPA.em());
        return ok(Json.toJson(rule.findAll()));
    }
}
