package models.entities.filters;

import java.util.Date;

public interface DateRange {

	public abstract Date getStartInclusive();

	public abstract void setStartInclusive(Date startInclusive);

	public abstract Date getEndExclusiveDate();

	public abstract void setEndExclusiveDate(Date endExclusiveDate);

	public abstract String getDateAttribute();

	public abstract void setDateAttribute(String dateAttribute);

}