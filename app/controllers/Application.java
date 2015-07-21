package controllers;

import gateways.configuration.AppKey;
import interactors.ConfRule;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {
    private static String appVersion = "unknown Version";
   	private static String dbName = "unknown Database";
   	
   	static {
   		final ConfRule confRule = Factory.makeConfRule();
   		appVersion = "Version: " + confRule.readString(AppKey.VERSION.key());
   		dbName = "Database: " + confRule.readString("db.default.url");
   	}
   	

    public static Result index() {
        return ok(views.html.index.render("Your new application is ready."));
    }
    
    public static Result manageVizs() {
        return ok(views.html.vizs.render());
    }

    public static String info() {
        return "Copyright 2015 - University of Pittsburgh, " 
        		+ appVersion + ", " + dbName;
    }

    public static Result swagger() {
        return ok(views.html.swagger.render());
    }
}
