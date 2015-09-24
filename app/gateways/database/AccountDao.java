package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import models.entities.Account;
import models.filters.AccountFilter;


public class AccountDao extends DataAccessObject<Account> {
	public AccountDao(EntityManager em){
		this(new JpaAdaptor(em));
	}

	private AccountDao(JpaAdaptor helper){
		super(Account.class, helper);
	}
	
	public List<Account> query(AccountFilter filter) {
		Map<String, Object> equalityMap = filter.getEqualities();
		equalityMap.put("email", filter.getEmail());
		return super.query(filter);
	}
}