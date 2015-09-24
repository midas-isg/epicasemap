package controllers.security;

import play.mvc.Controller;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

public class Authentication extends Security.Authenticator {
	public String getUsername(Context ctx) {
		return ctx.session().get("id");
	}

	public Result onUnauthorized(Context ctx) {
		String path = ctx.request().path();
		String message = "Please log in to use the page " + path;
		Controller.flash("message", message);
		return redirect(controllers.routes.Login.login());
	}
}