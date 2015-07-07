package models.view;

import java.util.List;

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

	@Override
	public String toString() {
		return "VizInput [seriesIds=" + seriesIds + ", name=" + name + "]";
	}
}
