package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class Test extends Controller {
    public static Result acceptance() {
        return ok(views.html.tests.acceptance.render());
    }
}
