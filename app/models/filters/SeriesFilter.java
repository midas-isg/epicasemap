package models.filters;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class SeriesFilter extends GenericFilter implements Filter {
	@JsonIgnore
	private List<Long> ids;

	@JsonIgnore
	private Long OwnerId;
	

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	public Long getOwnerId() {
		return OwnerId;
	}

	public void setOwnerId(Long ownerId) {
		OwnerId = ownerId;
	}
}