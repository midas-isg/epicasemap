package models.filters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LocationFilter implements Filter {

	@JsonIgnore
	private Long alsId;

	@JsonIgnore
	private Double latitude;
	@JsonIgnore
	private Double longitude;
	
	private Map<String, Object> equalities;
	
	@JsonIgnore
	private List<Long> ids;
	
	private Map<String, List<?>> ins;
	
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
	
	public void setInOperators(Map<String, List<?>> ins){
		this.ins = ins;
	}
	
	public Map<String, List<?>>  getInOperators(){
		return ins;
	}

	public void setIds(List<Long> ids){
		this.ids = ids;
	}

	public List<Long> getIds(){
		return ids;
	}
}