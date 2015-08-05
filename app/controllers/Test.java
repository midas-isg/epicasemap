package controllers;

import gateways.configuration.AppKey;
import interactors.ConfRule;
import play.mvc.Controller;
import play.mvc.Result;

public class Test extends Controller {
    private static String appVersion = "unknown Version";
   	private static String dbName = "unknown Database";
   	
   	static {
   		final ConfRule confRule = Factory.makeConfRule();
   		appVersion = "Version: " + confRule.readString(AppKey.VERSION.key());
   		dbName = "Database: " + confRule.readString("db.default.url");
   	}
   	

    public static Result example() {
        return ok(views.html.tests.example.render());
    }

    public static Result acceptance() {
        return ok(views.html.tests.acceptance.render());
    }

   public static String info() {
        return "Copyright 2015 - University of Pittsburgh, " 
        		+ appVersion + ", " + dbName;
    }

}
