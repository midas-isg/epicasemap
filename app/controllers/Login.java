package controllers;


import interactors.AccountRule;
import interactors.security.Credential;
import models.Registration;
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
	}
	
	public static Result promptRegistration() {
		return ok(views.html.register.render(Form.form(Registration.class)));
	}

	@Transactional
	public static Result register() {
		Form<Registration> form = Form.form(Registration.class).bindFromRequest();

		if (form.hasErrors()) {
			return badRequest(views.html.register.render(form));
		}
		
		Registration registration = form.get();
		return register(form, registration);
	}
	
	private static Result register(Form<Registration> form, Registration input) {
		String name = input.getName();
		Credential credential = new Credential(null, name);
		securityRule().register(credential, input);
		flash("message", input.getEmail() + " has been registed. Please log in.");
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