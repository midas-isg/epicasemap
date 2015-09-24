package interactors;

import gateways.database.AccountDao;
import gateways.database.jpa.DataAccessObject;
import interactors.security.Credential;
import interactors.security.password.Authenticator;
import interactors.security.password.HashedPassword;
import models.SignIn;
import models.entities.Account;
import models.entities.Password;
import models.filters.AccountFilter;



public class AccountRule extends CrudRule<Account> {
	private AccountDao dao;
	private Authenticator authenticator;

	public AccountRule(AccountDao dao){
		this.dao = dao;
	}
	
	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}

	public Account create(Credential credential, SignIn signIn) {
		final HashedPassword hp = authenticator.hash(signIn.getPassword());
		Account account = new Account();
		Password password = new Password();
		account.setPassword(password);
		password.copy(hp);
		password.setHash(hp.getHash());
		account.setEmail(signIn.getEmail());
		account.setName(credential.getName());
		create(account);
		return account;
	}

	@Override
	protected DataAccessObject<Account> getDao() {
		return dao;
	}

	public Credential authenticate(SignIn signIn) {
		final Account account = findAccount(signIn.getEmail());
		if (account == null)
			return null;
		if (authenticator.verify(signIn.getPassword(), account.getPassword()))
			return newCredential(account);
		return null;
	}

	public Credential register(Credential credential, SignIn signIn) {
		final Account account = findAccount(signIn.getEmail());
		if (account != null){
			return null;
		}
		create(credential, signIn);
		return credential;
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
}