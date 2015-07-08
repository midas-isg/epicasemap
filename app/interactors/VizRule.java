package interactors;

import gateways.database.VizDao;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import models.entities.Series;
import models.entities.Viz;
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
		result.setName(input.getName());
		final List<Long> ids = input.getSeriesIds();
		result.setAllSeries(toAllSeries(ids));
		final List<Long> id2s = input.getSeries2Ids();
		result.setAllSeries2(toAllSeries(id2s));
		return result;
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
		input.setName(data.getName());
		input.setSeriesIds(toIds(data.getAllSeries()));
		return input;
	}

	private List<Long> toIds(List<Series> input) {
		return toList(input, it -> it.getId());
	}

	private <I, O> List<O> toList(List<I> input, Function<? super I, ? extends O> mapper) {
		if (input == null)
			return null;

		return input.stream().map(mapper).collect(Collectors.toList());
	}
}
