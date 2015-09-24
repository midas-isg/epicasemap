package interactors;

import gateways.database.PermissionDao;
import gateways.database.jpa.DataAccessObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import models.entities.Permission;
import models.entities.Series;
import models.filters.GenericFilter;
import models.filters.Restriction;
import models.filters.SeriesFilter;

public class AuthorizationRule extends CrudRule<Permission> {
	private final long publicAccountId = 1L;
	private PermissionDao dao;
	private SeriesRule seriesRule;

	public AuthorizationRule(PermissionDao dao){
		this.dao = dao;
	}
	
	public void setSeriesRule(SeriesRule seriesRule) {
		this.seriesRule = seriesRule;
	}

	@Override
	protected DataAccessObject<Permission> getDao() {
		return dao;
	}

	public List<Long> findSeriesIds(Restriction restriction) {
		final List<Series> ownedSeries = findOwnedSeries(restriction.accountId);
		List<Long> ids = ownedSeries.stream()
				.map(s -> s.getId())
				.collect(Collectors.toList());
		streamSeriesIds(restriction).forEach(id -> ids.add(id));
		restriction.accountId = publicAccountId;
		streamSeriesIds(restriction).forEach(id -> ids.add(id));
		
		return ids;
	}

	private Stream<Long> streamSeriesIds(Restriction restriction) {
		List<Permission> permissions = findPermissions(restriction);
		return permissions.stream()
				.map(permission -> permission.getSeries().getId());
	}

	private List<Series> findOwnedSeries(final Long accountId) {
		SeriesFilter filter = new SeriesFilter();
		filter.setOwnerId(accountId);
		return seriesRule.query(filter);
	}

	private List<Permission> findPermissions(Restriction restriction) {
		GenericFilter filter = new GenericFilter();
		filter.setRestriction(restriction);
		return dao.query(filter);
	}
}