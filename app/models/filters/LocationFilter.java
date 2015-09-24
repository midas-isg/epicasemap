package models.filters;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LocationFilter extends GenericFilter implements Filter {

	@JsonIgnore
	private Long alsId;

	@JsonIgnore
	private Double latitude;
	@JsonIgnore
	private Double longitude;
	
	@JsonIgnore
	private List<Long> ids;
	
	public Long getAlsId() {
		return alsId;
	}

	public void setAlsId(Long alsId) {
		this.alsId = alsId;
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

	public void setIds(List<Long> ids){
		this.ids = ids;
	}

	public List<Long> getIds(){
		return ids;
	}
}