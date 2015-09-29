package controllers;


import static play.data.Form.form;
import interactors.AccountRule;
import interactors.security.Credential;
import models.Registration;
import models.SignIn;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.security.Authentication;

public class User extends Controller {
	public static Result promptRegistration() {
		return ok(views.html.register.render(form(Registration.class)));
	}

	@Transactional
	public static Result register() {
		Form<Registration> form = form(Registration.class).bindFromRequest();
		if (form.hasErrors()) {
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
}