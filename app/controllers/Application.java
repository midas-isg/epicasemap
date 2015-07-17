package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {
    public static Result index() {
        return ok(views.html.index.render("Your new application is ready."));
    }
    
    public static Result manageVizs() {
        return ok(views.html.vizs.render());
    }

    public static Result swagger() {
        return ok(views.html.swagger.render());
    }
}
