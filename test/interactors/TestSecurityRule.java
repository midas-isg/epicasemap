package interactors;

import static org.fest.assertions.Assertions.assertThat;
import integrations.app.App;
import interactors.security.Credential;
import models.SignIn;
import models.entities.Account;

import org.junit.BeforeClass;
import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback0;
import controllers.Factory;

public class TestSecurityRule {
	private static Credential credential = null; 
	private static final String email = "e@ma.il";
	private static final String password = "password";
	
	@BeforeClass
	public static void populateDb(){
		runWithTransaction(() ->{
			AccountRule rule = Factory.makeAccountRule(JPA.em());
			SignIn signIn = newSignIn(email, password);
			final Credential c = new Credential(null, "name");
			final Account account = rule.create(c, signIn);
			assertThat(account.getId()).isPositive();
			credential = new Credential(account.getId(), "name");
		});
	}
	
	@Test
	public void testBasicFlow() throws Exception {
		runWithTransaction(() ->{testAuthenticate();});
	}

	private void testAuthenticate() {
		Credential actual = authenticate(email, password);
		assertCredential(actual, credential);
		
		Credential wrongEmail =  authenticate("wrong@email.com", password);
		assertThat(wrongEmail).as("shouldn't authenticate").isNull();

		Credential wrongPassword =  authenticate(email, "wrong password");
		assertThat(wrongPassword).as("shouldn't authenticate").isNull();
	}

	private Credential authenticate(String email, String password) {
		AccountRule rule = Factory.makeAccountRule(JPA.em());
		SignIn signIn = newSignIn(email, password);
		return rule.authenticate(signIn);
	}

	private static SignIn newSignIn(String email, String password) {
		SignIn signIn = new SignIn();
		signIn.setEmail(email);
		signIn.setPassword(password);
		return signIn;
	}

	private void assertCredential(Credential actual, Credential expected) {
		assertThat(actual.getId()).isEqualTo(expected.getId());
		assertThat(actual.getName()).isEqualTo(expected.getName());
	}

	private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}
}