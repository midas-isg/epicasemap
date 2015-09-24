package controllers;


import interactors.AccountRule;
import interactors.security.Credential;
import models.SignIn;
import play.Logger;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

public class Login extends Controller {
	@Transactional
	public static Result submit() {
		Form<SignIn> signInForm = Form.form(SignIn.class).bindFromRequest();

		if (signInForm.hasErrors()) {
			return badRequest(views.html.login.render(signInForm));
		}
		
		SignIn signIn = signInForm.get();
		return login(signInForm, signIn);
		//return register(signIn, signIn.getEmail());
	}
	
	private static Result register(SignIn signIn, String name) {
		Credential credential = new Credential(null, name);
		securityRule().register(credential, signIn);
		flash("message", signIn.getEmail() + " has been registed.");
		return redirect(routes.Login.login());
	}

	private static Result login(Form<SignIn> signInForm, SignIn signIn) {
		Credential credential = authenticate(signIn);
		if (credential == null) {
			flash("message", "Invalid email or password.");
			return unauthorized(views.html.login.render(signInForm));
		}
		session("id", "" + credential.getId());
		session("name", credential.getName());
		return redirect(routes.Application.manageVizs());
	}

	private static Credential authenticate(SignIn signIn) {
		try {
			final AccountRule securityRule = securityRule();
			return securityRule.authenticate(signIn);
		} catch (Exception e) {
			Logger.error(e.toString());
			return null;
		}
	}
	
	private static AccountRule securityRule() {
		return Factory.makeAccountRule(JPA.em());
	}
	
	public static Result login() {
		return ok(views.html.login.render(Form.form(SignIn.class)));
	}

	public static Result logout() {
		session().clear();
		flash("message", "You've been logged out");
		return redirect(routes.Login.login());
	}
}