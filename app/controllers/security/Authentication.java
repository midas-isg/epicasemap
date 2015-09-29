package controllers.security;

import interactors.security.Credential;
import play.mvc.Controller;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

public class Authentication extends Security.Authenticator {
	private static final String ID = "id";

	@Override
	public String getUsername(Context ctx) {
		return ctx.session().get(ID);
	}

	@Override
	public Result onUnauthorized(Context ctx) {
		String path = ctx.request().path();
		String message = "Please log in to use the page " + path;
		Controller.flash("message", message);
		return redirect(controllers.routes.User.promptLogin());
	}
	
	public static void cacheCredential(Credential credential) {
		Controller.session(ID, "" + credential.getId());
		Controller.session("name", credential.getName());
	}
	
	public static String readAccountId(Context ctx) {
		return ctx.session().get(ID);
	}
}