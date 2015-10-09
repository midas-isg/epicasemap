package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import models.entities.Permission;
import models.filters.Filter;
import models.filters.GenericFilter;
import models.filters.Restriction;
import controllers.security.Restricted.Access;

public class PermissionDao<T extends Permission> extends DataAccessObject<T> {
	public PermissionDao(EntityManager em, Class<T> clazz){
		this(new JpaAdaptor(em), clazz);
	}

	private PermissionDao(JpaAdaptor helper, Class<T> clazz){
		super(clazz, helper);
	}
	
	@Override
	public List<T> query(Filter filter){
		if (filter instanceof GenericFilter)
			return  query((GenericFilter)filter);
		return super.query(filter);
	}
	
	public List<T> query(GenericFilter filter) {
		final Restriction restriction = filter.getRestriction();
		final Map<String, Object> equalities = filter.getEqualities();
		equalities.put("account.id", restriction.accountId);
		equalities.put("series.id", restriction.seriesId);
		equalities.put("visualization.id", restriction.vizId);

		final Map<String, Object> disjunctions = filter.getDisjunctiveEqualities();
		if (restriction.accesses != null){
			for (Access access : restriction.accesses){
				disjunctions.put(access.name().toLowerCase(), true);
			}
		}
		return super.query(filter);
	}
}
