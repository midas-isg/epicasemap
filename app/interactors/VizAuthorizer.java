package interactors;

import gateways.database.PermissionDao;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import models.entities.Visualization;
import models.entities.VizPermission;
import models.filters.MetaFilter;
import models.filters.Restriction;

public class VizAuthorizer extends Authorizer<VizPermission> {
	private VizRule vizRule;
	
	public VizAuthorizer(PermissionDao<VizPermission> dao){
		super(dao);
	}

	public void setVizRule(VizRule vizRule) {
		this.vizRule = vizRule;
	}

	public List<Long> findVizIds(Restriction restriction) {
		Long accountId = restriction.accountId;
		if (accountId == null)
			accountId = 0L;
		final List<Visualization> ownedSeries = findOwnedVizs(accountId);
		List<Long> ids = ownedSeries.stream()
				.map(s -> s.getId())
				.collect(Collectors.toList());
		streamVizIds(restriction).forEach(id -> ids.add(id));
		restriction.accountId = publicAccountId;
		streamVizIds(restriction).forEach(id -> ids.add(id));
		
		return ids;
	}
	
	private List<Visualization> findOwnedVizs(final Long accountId) {
		MetaFilter filter = new MetaFilter();
		filter.setOwnerId(accountId);
		return vizRule.query(filter);
	}

	private Stream<Long> streamVizIds(Restriction restriction) {
		List<VizPermission> permissions = findPermissions(restriction);
		return permissions.stream()
				.map(permission -> permission.getVisualization().getId());
	}

	@Override
	protected VizPermission newPermission(long entityId) {
		final VizPermission p = new VizPermission();
		p.setVisualization(vizRule.read(entityId));
		return p;
	}
}