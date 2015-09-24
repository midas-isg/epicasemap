package models.filters;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class CoordinateFilter
extends GenericFilter 
implements Pagination, TimestampRange, Filter {
	
	@JsonIgnore
	private Long seriesId;
	
	private Date startTimestampInclusive;
	private Date endTimestampExclusive;
	private String timestampAttribute;
	
	private Integer limit;
	private Integer offset;
	
	public Long getSeriesId() {
		return seriesId;
	}

	public void setSeriesId(Long seriesId) {
		this.seriesId = seriesId;
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
}