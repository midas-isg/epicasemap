package interactors;

import gateways.database.PermissionDao;
import gateways.database.jpa.DataAccessObject;

import java.util.List;

import models.entities.Account;
import models.entities.Mode;
import models.entities.Permission;
import models.filters.GenericFilter;
import models.filters.Restriction;


public abstract class Authorizer<T extends Permission> extends CrudRule<T> {
	protected final static long publicAccountId = 1L;
	private AccountRule accountRule;
	
	abstract protected T newPermission(long entityId);

	private PermissionDao<T> dao;
	
	public Authorizer(PermissionDao<T> dao){
		this.dao = dao;
	}
	
	@Override
	protected DataAccessObject<T> getDao() {
		return dao;
	}
	
	public void setAccountRule(AccountRule accountRule) {
		this.accountRule = accountRule;
	}
	
	Account readAccount(long accountId) {
		return accountRule.read(accountId);
	}

	public long permit(long accountId, Mode mode, long entityId) {
		T p = newPermission(entityId);
		p.setAccount(readAccount(accountId));
		p.copy(mode);
		return create(p);
	}

	public List<T> findPermissions(Restriction restriction) {
		GenericFilter filter = new GenericFilter();
		filter.setRestriction(restriction);
		return getDao().query(filter);
	}

	public void updateMode(long id, Mode mode) {
		T original = read(id);
		original.copy(mode);
		update(id, original);
	}
}