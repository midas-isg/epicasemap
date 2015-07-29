package models.view;

import java.util.List;

public class VizInput {
	private String name;
	private List<Long> seriesIds;
	private List<Long> series2Ids;
	private String uiSetting;

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

	public List<Long> getSeries2Ids() {
		return series2Ids;
	}

	public void setSeries2Ids(List<Long> series2Ids) {
		this.series2Ids = series2Ids;
	}

	public String getUiSetting() {
		return uiSetting;
	}

	public void setUiSetting(String uiSetting) {
		this.uiSetting = uiSetting;
	}

	@Override
	public String toString() {
		return "VizInput [name=" + name + ", seriesIds=" + seriesIds
				+ ", series2Ids=" + series2Ids + ", uiSetting=" + uiSetting
				+ "]";
	}
}
