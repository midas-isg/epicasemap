package models.entities;

import java.util.Date;

import models.entities.filters.DateRange;
import models.entities.filters.Pagination;

public class CoordinateTimeFilter implements Pagination, DateRange {
	
	private Date startInclusive;
	private Date endExclusiveDate;
	private String dateAttribute;
	
	private Integer limit;
	private Integer offset;

	@Override
	public Date getStartInclusive() {
		return startInclusive;
	}

	@Override
	public void setStartInclusive(Date startInclusive) {
		this.startInclusive = startInclusive;
	}

	@Override
	public Date getEndExclusiveDate() {
		return endExclusiveDate;
	}

	@Override
	public void setEndExclusiveDate(Date endExclusiveDate) {
		this.endExclusiveDate = endExclusiveDate;
	}

	@Override
	public String getDateAttribute() {
		return dateAttribute;
	}

	@Override
	public void setDateAttribute(String dateAttribute) {
		this.dateAttribute = dateAttribute;
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