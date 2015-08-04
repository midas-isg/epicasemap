package models.view;

import java.util.List;

import models.entities.MetaData;

public class VizInput extends MetaData {
	private Long id;
	private List<Long> seriesIds;
	private List<Long> series2Ids;
	private String uiSetting;

	public List<Long> getSeriesIds() {
		return seriesIds;
	}

	public void setSeriesIds(List<Long> seriesIds) {
		this.seriesIds = seriesIds;
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
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
}
