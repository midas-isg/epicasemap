package controllers;


import static controllers.Factory.makeSeriesAuthorizer;
import static controllers.Factory.makeVizAuthorizer;
import static controllers.ResponseHelper.okAsWrappedJsonArray;
import static interactors.Authorizer.publicAccountId;
import static play.data.Form.form;
import interactors.AccountRule;
import interactors.Authorizer;
import interactors.SeriesAuthorizer;
import interactors.VizAuthorizer;
import interactors.security.Credential;

import java.util.List;

import models.Registration;
import models.SignIn;
import models.entities.Permission;
import models.filters.AccountFilter;
import models.filters.Restriction;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.security.Authentication;
import controllers.security.AuthorizationKit;

public class User extends Controller {
	public static Result promptRegistration() {
		return ok(views.html.register.render(form(Registration.class)));
	}

	@Transactional
	public static Result register() {
		Form<Registration> form = form(Registration.class).bindFromRequest();
		if (form.hasErrors()) {
			flash("message", "Failed to register! There were/was " + form.errors().size() + " eror/s.");
			return badRequest(views.html.register.render(form));
		}
		
		return register(form.get());
	}
	
	private static Result register(Registration input) {
		try{
			makeRule().register(input);
			return registerSuccessfully(input);
		} catch (Throwable t) {
			flash("message", "Failed to register due to " + t.getMessage());
			return redirect(controllers.routes.User.promptRegistration());
		}
	}

	private static AccountRule makeRule() {
		return Factory.makeAccountRule(JPA.em());
	}
	
	private static Result registerSuccessfully(Registration input) {
		String msg = input.getEmail() + " has been registed. Please log in.";
		flash("message", msg);
		return redirect(routes.User.login());
	}

	public static Result promptLogin() {
		return ok(views.html.login.render(form(SignIn.class)));
	}

	@Transactional
	public static Result login() {
		Form<SignIn> signInForm = form(SignIn.class).bindFromRequest();
		if (signInForm.hasErrors()) {
			return badRequest(views.html.login.render(signInForm));
		}
		
		return login(signInForm.get());
	}
	
	private static Result login(SignIn signIn) {
		try {
			Credential credential = makeRule().authenticate(signIn);
			return loginSuccessfully(credential);
		} catch (Throwable t){
			flash("message", "Failed to log in due to " +t.getMessage());
			return redirect(controllers.routes.User.promptLogin());
		}
	}

	private static Result loginSuccessfully(Credential credential) {
		Authentication.cacheCredential(credential);
		return redirect(routes.Application.manageVizs());
	}

	public static Result logout() {
		session().clear();
		flash("message", "You've been logged out");
		return redirect(routes.User.login());
	}
	
	@Transactional
	public static Result get() {
		final AccountRule rule = Factory.makeAccountRule(JPA.em());
		AccountFilter filter = null;
		return okAsWrappedJsonArray(rule.query(filter), filter);
	}

	@Transactional
	public static Result getMyPermissions() {
		final long accountId = AuthorizationKit.readAccountId();
		final VizAuthorizer vizAuth = makeVizAuthorizer(JPA.em());
		List<Permission> permissions = findAllPermissions(vizAuth, accountId);
		final SeriesAuthorizer seriesAuth = makeSeriesAuthorizer(JPA.em());
		permissions.addAll(findAllPermissions(seriesAuth, accountId));
		return okAsWrappedJsonArray(permissions, null);
	}

	private static List<Permission> findAllPermissions(
			final Authorizer<?> authorizer, long accountId) {
		List<Permission> permissions = findPermissions(authorizer, accountId);
		permissions.addAll(findPermissions(authorizer, publicAccountId));
		return permissions;
	}

	@SuppressWarnings("unchecked")
	private static List<Permission> findPermissions(
			final Authorizer<?> authorizer, long accountId) {
		Restriction restriction = new Restriction(accountId, null, null, null);
		return (List<Permission>) authorizer.findPermissions(restriction);
	}
}	
