package interactors;

import gateways.database.AccountDao;
import gateways.database.jpa.DataAccessObject;
import interactors.security.Credential;
import interactors.security.password.Authenticator;
import interactors.security.password.HashedPassword;

import java.util.List;

import models.Registration;
import models.SignIn;
import models.entities.Account;
import models.entities.Password;
import models.exceptions.ConstraintViolation;
import models.exceptions.Unauthorized;
import models.filters.AccountFilter;
import play.Logger;



public class AccountRule extends CrudRule<Account> {
	private AccountDao dao;
	private Authenticator authenticator;

	public AccountRule(AccountDao dao){
		this.dao = dao;
	}
	
	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}

	public Account register(Registration input) {
		checkEmailUniqueness(input.getEmail());
		Account account = newAccount(input);
		create(account);
		return account;
	}

	private void checkEmailUniqueness(final String email) {
		if (findAccount(email) != null)
			throw new ConstraintViolation(email + " was already registered.");
	}

	private Account newAccount(Registration input) {
		Account account = new Account();
		account.setPassword(newPassword(input.getPassword()));
		account.setEmail(input.getEmail());
		account.setName(input.getName());
		return account;
	}

	private Password newPassword(String passwordInPainText) {
		Password password = new Password();
		final HashedPassword hp = authenticator.hash(passwordInPainText);
		password.copy(hp);
		password.setHash(hp.getHash());
		return password;
	}

	@Override
	protected DataAccessObject<Account> getDao() {
		return dao;
	}

	public Credential authenticate(SignIn signIn) {
		try {
			final Account account = findAccount(signIn.getEmail());
			final String passwordInPainText = signIn.getPassword();
			if (authenticator.verify(passwordInPainText, account.getPassword()))
				return newCredential(account);
		} catch (Throwable t) {
			Logger.warn(signIn + ": authentication failed!", t);
		}
		throw new Unauthorized("Invalid email and/or password.");
	}

	private Credential newCredential(Account account) {
		final Long id = account.getId();
		final String name = account.getName();
		return new Credential(id, name);
	}

	private Account findAccount(String email) {
		AccountFilter filter = new AccountFilter();
		filter.setEmail(email);
		try {
			return dao.query(filter).get(0);
		} catch (Exception e){
			return null;
		}
	}

	public List<Account> query(AccountFilter filter) {
		return dao.query(filter);
	}
}