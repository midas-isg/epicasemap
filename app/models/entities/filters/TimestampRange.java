package models.entities.filters;

import java.util.Date;

public interface TimestampRange extends Filter {

	public abstract Date getStartTimestampInclusive();

	public abstract void setStartTimestampInclusive(Date startInclusive);

	public abstract Date getEndTimestampExclusive();

	public abstract void setEndTimestampExclusive(Date endExclusiveDate);

	public abstract String getTimestampAttribute();

	public abstract void setTimestampAttribute(String dateAttribute);

}