package interactors;

import gateways.database.PermissionDao;
import gateways.database.jpa.DataAccessObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import models.entities.Mode;
import models.entities.Series;
import models.entities.SeriesPermission;
import models.filters.GenericFilter;
import models.filters.Restriction;
import models.filters.SeriesFilter;

public class AuthorizationRule extends CrudRule<SeriesPermission> {
	private final long publicAccountId = 1L;
	private PermissionDao dao;
	private SeriesRule seriesRule;
	private AccountRule accountRule;

	public AuthorizationRule(PermissionDao dao){
		this.dao = dao;
	}
	
	public void setSeriesRule(SeriesRule seriesRule) {
		this.seriesRule = seriesRule;
	}

	public void setAccountRule(AccountRule accountRule) {
		this.accountRule = accountRule;
	}

	@Override
	protected DataAccessObject<SeriesPermission> getDao() {
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
		List<SeriesPermission> permissions = findPermissions(restriction);
		return permissions.stream()
				.map(permission -> permission.getSeries().getId());
	}

	private List<Series> findOwnedSeries(final Long accountId) {
		SeriesFilter filter = new SeriesFilter();
		filter.setOwnerId(accountId);
		return seriesRule.query(filter);
	}

	public List<SeriesPermission> findPermissions(Restriction restriction) {
		GenericFilter filter = new GenericFilter();
		filter.setRestriction(restriction);
		return dao.query(filter);
	}

	public long grantSeries(long accountId, Mode mode, long seriesId) {
		SeriesPermission p = new SeriesPermission();
		p.setAccount(accountRule.read(accountId));
		p.setSeries(seriesRule.read(seriesId));
		p.copy(mode);
		return create(p);
	}

	public void updateMode(long id, Mode mode) {
		SeriesPermission original = read(id);
		original.copy(mode);
		update(id, original);
	}
}