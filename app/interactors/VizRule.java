package interactors;

import gateways.database.VizDao;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import models.entities.Account;
import models.entities.Series;
import models.entities.Visualization;
import models.entities.VizPermission;
import models.filters.MetaFilter;
import models.filters.Restriction;
import models.view.VizInput;

public class VizRule extends CrudRule<Visualization> {
	private VizDao dao;
	private SeriesRule seriesRule;
	private AccountRule accountRule;
	private VizAuthorizer vizAuthorizer; 
	
	public VizRule(VizDao dao) {
		super();
		this.dao = dao;
	}

	public void setSeriesRule(SeriesRule rule) {
		seriesRule = rule;
	}

	public void setAccountRule(AccountRule rule) {
		accountRule = rule;
	}

	public void setVizAuthorizer(VizAuthorizer vizAuthorizer) {
		this.vizAuthorizer = vizAuthorizer;
	}

	public long createFromInput(VizInput input) {
		final Visualization data = toViz(input);
		return create(data);
	}

	public Visualization updateFromInput(long id, VizInput input) {
		final Visualization data = toViz(input);
		return update(id, data);
	}
	
	@Override
	protected VizDao getDao() {
		return dao;
	}
	
	
	@Override
	public void delete(long id) {
		deleteAllSeriesPermissions(id);
		super.delete(id);
	}


	private int deleteAllSeriesPermissions(long vizId) {
		Restriction r = new Restriction(null, null, null, vizId);
		final List<VizPermission> permissions = vizAuthorizer.findPermissions(r);
		for (VizPermission permission : permissions) {
			vizAuthorizer.delete(permission.getId());
		}
		return permissions.size();
	}
	

	Visualization toViz(VizInput input) {
		if (input == null)
			return null;
		Visualization result = new Visualization();
		copy(result, input);
		final List<Long> ids = input.getSeriesIds();
		result.setAllSeries(toAllSeries(ids));
		result.setOwner(readAccount(input.getOwnerId()));
		return result;
	}

	private Account readAccount(Long id) {
		if (id == null)
			return null;
		return accountRule.read(id);
	}

	private void copy(Visualization dest, VizInput src) {
		copyMetadata(dest, src);
		dest.setUiSetting(src.getUiSetting());
	}

	private List<Series> toAllSeries(List<Long> ids) {
		return toList(ids, id -> seriesRule.read(id));
	}

	public VizInput fromViz(Visualization data) {
		if (data == null)
			return null;
		
		VizInput input = new VizInput();
		copyMetadata(data, input);
		input.setSeriesIds(toIds(data.getAllSeries()));
		input.setUiSetting(data.getUiSetting());
		return input;
	}
	
	public List<Visualization> query(MetaFilter filter) {
		return dao.query(filter);
	}

	private List<Long> toIds(List<Series> input) {
		return toList(input, it -> it.getId());
	}

	private <I, O> List<O> toList(List<I> input, Function<? super I, ? extends O> mapper) {
		if (input == null)
			return null;

		return input.stream().map(mapper).collect(Collectors.toList());
	}

	public void updateUiSetting(long id, String data) {
		Visualization original = read(id);
		original.setUiSetting(data);
		update(id, original);
	}

}