package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import models.entities.Permission;
import models.filters.GenericFilter;
import models.filters.Restriction;
import controllers.security.Restricted.Access;

public class PermissionDao extends DataAccessObject<Permission> {
	public PermissionDao(EntityManager em){
		this(new JpaAdaptor(em));
	}

	private PermissionDao(JpaAdaptor helper){
		super(Permission.class, helper);
	}
	
	public List<Permission> query(GenericFilter filter) {
		final Restriction restriction = filter.getRestriction();
		final Map<String, Object> equalities = filter.getEqualities();
		equalities.put("account.id", restriction.accountId);

		final Map<String, Object> disjunctions = filter.getDisjunctiveEqualities();
		if (restriction.accesses != null){
			for (Access access : restriction.accesses){
				disjunctions.put(access.name().toLowerCase(), true);
			}
		}
		return super.query(filter);
	}
}
