package models.entities;

import java.util.LinkedHashMap;
import java.util.Map;

import models.entities.filters.Filter;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LocationFilter implements Filter {

	@JsonIgnore
	private Long alsId;

	private Double latitude;
	private Double longitude;

	private Map<String, Object> equalities;

	private LinkedHashMap<String, Order> order;

	public Long getAlsId() {
		return alsId;
	}

	public void setAlsId(Long alsId) {
		this.alsId = alsId;
	}

	@Override
	public Map<String, Object> getEqualities() {
		return equalities;
	}

	public void setEqualities(Map<String, Object> equalityMap) {
		this.equalities = equalityMap;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	@Override
	public LinkedHashMap<String, Order> getOrder() {
		return order;
	}
	
	public void setOrder(LinkedHashMap<String, Order> map) {
		order = map;
	}

}