package models.filters;

import models.entities.Series;

public class SeriesDataUrlFilter extends GenericFilter implements Filter {

	private Long id;
	private String url;
	private String checksum;
	private Series series;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public Series getSeries() {
		return series;
	}

	public void setSeries(Series series) {
		this.series = series;
	}
}