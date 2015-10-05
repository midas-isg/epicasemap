package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import models.entities.SeriesPermission;
import models.filters.GenericFilter;
import models.filters.Restriction;
import controllers.security.Restricted.Access;

public class PermissionDao extends DataAccessObject<SeriesPermission> {
	public PermissionDao(EntityManager em){
		this(new JpaAdaptor(em));
	}

	private PermissionDao(JpaAdaptor helper){
		super(SeriesPermission.class, helper);
	}
	
	public List<SeriesPermission> query(GenericFilter filter) {
		final Restriction restriction = filter.getRestriction();
		final Map<String, Object> equalities = filter.getEqualities();
		equalities.put("account.id", restriction.accountId);
		equalities.put("series.id", restriction.seriesId);

		final Map<String, Object> disjunctions = filter.getDisjunctiveEqualities();
		if (restriction.accesses != null){
			for (Access access : restriction.accesses){
				disjunctions.put(access.name().toLowerCase(), true);
			}
		}
		return super.query(filter);
	}
}
