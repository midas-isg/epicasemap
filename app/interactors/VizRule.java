package interactors;

import gateways.database.VizDao;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import models.entities.MetaData;
import models.entities.Series;
import models.entities.Viz;
import models.filters.Filter;
import models.view.VizInput;

public class VizRule extends CrudRule<Viz> {
	private VizDao dao;
	private SeriesRule seriesRule;
	
	public VizRule(VizDao dao) {
		super();
		this.dao = dao;
	}
	
	public long create(VizInput input) {
		final Viz data = toViz(input);
		return super.create(data);
	}

	@Override
	protected VizDao getDao() {
		return dao;
	}
	
	public Viz toViz(VizInput input) {
		if (input == null)
			return null;
		Viz result = new Viz();
		copy(result, input);
		final List<Long> ids = input.getSeriesIds();
		result.setAllSeries(toAllSeries(ids));
		result.setUiSetting(input.getUiSetting());
		return result;
	}

	private void copy(MetaData dest, MetaData src) {
		dest.setId(src.getId());
		dest.setCreator(src.getCreator());
		dest.setDescription(src.getDescription());
		dest.setIsVersionOf(src.getIsVersionOf());
		dest.setLicense(src.getLicense());
		dest.setPublisher(src.getPublisher());
		dest.setTitle(src.getTitle());
		dest.setVersion(src.getVersion());
	}

	private List<Series> toAllSeries(List<Long> ids) {
		return toList(ids, id -> seriesRule.read(id));
	}

	public void setSeriesRule(SeriesRule rule) {
		seriesRule = rule;
	}

	public VizInput fromViz(Viz data) {
		if (data == null)
			return null;
		
		VizInput input = new VizInput();
		copy(data, input);
		input.setSeriesIds(toIds(data.getAllSeries()));
		input.setUiSetting(data.getUiSetting());
		return input;
	}
	
	public List<Viz> query(Filter filter) {
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
		Viz original = read(id);
		original.setUiSetting(data);
		update(id, original);
	}
}
