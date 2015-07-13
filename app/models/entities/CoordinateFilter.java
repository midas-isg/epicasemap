package models.entities;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import models.entities.filters.Filter;
import models.entities.filters.Pagination;
import models.entities.filters.TimestampRange;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class CoordinateFilter implements Pagination, TimestampRange, Filter {
	
	@JsonIgnore
	private Long seriesId;
	
	private Map<String, Object> equalities;
	
	private Date startTimestampInclusive;
	private Date endTimestampExclusive;
	private String timestampAttribute;
	
	private Integer limit;
	private Integer offset;
	
	private LinkedHashMap<String, Order> order;

	public Long getSeriesId() {
		return seriesId;
	}

	public void setSeriesId(Long seriesId) {
		this.seriesId = seriesId;
	}

	@Override
	public Map<String, Object> getEqualities() {
		return equalities;
	}

	public void setEqualities(Map<String, Object> equalityMap) {
		this.equalities = equalityMap;
	}

	@Override
	public Date getStartTimestampInclusive() {
		return startTimestampInclusive;
	}

	@Override
	public void setStartTimestampInclusive(Date startInclusive) {
		this.startTimestampInclusive = startInclusive;
	}

	@Override
	public Date getEndTimestampExclusive() {
		return endTimestampExclusive;
	}

	@Override
	public void setEndTimestampExclusive(Date endExclusive) {
		this.endTimestampExclusive = endExclusive;
	}

	@Override
	public String getTimestampAttribute() {
		return timestampAttribute;
	}

	@Override
	public void setTimestampAttribute(String timestampAttribute) {
		this.timestampAttribute = timestampAttribute;
	}

	@Override
	public Integer getLimit() {
		return limit;
	}

	@Override
	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	@Override
	public Integer getOffset() {
		return offset;
	}

	@Override
	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	@Override
	public LinkedHashMap<String, Order> getOrder() {
		return order;
	}
	
	public void setOrder(LinkedHashMap<String, Order> map) {
		order = map;
	}

}