package interactors;

import gateways.database.PermissionDao;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import models.entities.Series;
import models.entities.SeriesPermission;
import models.filters.MetaFilter;
import models.filters.Restriction;

public class SeriesAuthorizer extends Authorizer<SeriesPermission> {
	private SeriesRule seriesRule;
	
	public SeriesAuthorizer(PermissionDao<SeriesPermission> dao){
		super(dao);
	}
	
	public void setSeriesRule(SeriesRule seriesRule) {
		this.seriesRule = seriesRule;
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
		MetaFilter filter = new MetaFilter();
		filter.setOwnerId(accountId);
		return seriesRule.query(filter);
	}

	@Override
	protected SeriesPermission newPermission(long entityId) {
		final SeriesPermission p = new SeriesPermission();
		p.setSeries(seriesRule.read(entityId));
		return p;
	}
}