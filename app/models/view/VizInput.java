package models.view;

import java.util.List;

public class VizInput extends Input {
	private List<Long> seriesIds;
	private String uiSetting;
	
	public List<Long> getSeriesIds() {
		return seriesIds;
	}

	public void setSeriesIds(List<Long> seriesIds) {
		this.seriesIds = seriesIds;
	}

	public String getUiSetting() {
		return uiSetting;
	}

	public void setUiSetting(String uiSetting) {
		this.uiSetting = uiSetting;
	}
}
