package interactors;

import static org.fest.assertions.Assertions.assertThat;
import integrations.app.App;
import interactors.security.Credential;
import models.Registration;
import models.entities.Account;
import models.exceptions.Unauthorized;

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
			String name = "name";
			Registration signIn = newRegistration(email, password, name);
			final Account account = rule.register(signIn);
			assertThat(account.getId()).isPositive();
			credential = new Credential(account.getId(), name);
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
		Registration signIn = newRegistration(email, password, null);
		return authenticate(signIn);
	}

	private Credential authenticate(Registration signIn) {
		AccountRule rule = Factory.makeAccountRule(JPA.em());
		try {
			return rule.authenticate(signIn);
		} catch (Unauthorized e){
			return null;
		}
	}

	private static Registration newRegistration(String email, String passwd, String name) {
		Registration signIn = new Registration();
		signIn.setEmail(email);
		signIn.setPassword(passwd);
		signIn.setName(name);
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