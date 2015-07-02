package models.views;

import static java.util.stream.Collectors.toList;

import java.util.List;

import models.entities.Series;
import models.entities.Viz;
import play.db.jpa.JPA;

public class VizInput {
	private String name;
	private List<Long> seriesIds;

	public List<Long> getSeriesIds() {
		return seriesIds;
	}

	public void setSeriesIds(List<Long> seriesIds) {
		this.seriesIds = seriesIds;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static VizInput from(Viz data) {
		VizInput result = new VizInput();
		return result.fromViz(data);
	}

	private VizInput fromViz(Viz data) {
		setName(data.getName());
		setSeriesIds(toIds(data.getAllSeries()));
		return this;
	}

	private List<Long> toIds(List<Series> input) {
		if (input == null)
			return null;

		return input.stream().map(it -> it.getId()).collect(toList());
	}

	public Viz toViz() {
		Viz result = new Viz();
		result.setName(getName());
		final List<Long> ids = getSeriesIds();
		if (ids == null)
			return result;
		List<Series> allSeries = toAllSeries(ids);
		result.setAllSeries(allSeries);
		return result;
	}

	private List<Series> toAllSeries(List<Long> ids) {
		if (ids == null)
			return null;

		return ids.stream().map(id -> JPA.em().find(Series.class, id))
				.collect(toList());
	}

	@Override
	public String toString() {
		return "VizInput [seriesIds=" + seriesIds + ", name=" + name + "]";
	}
}
