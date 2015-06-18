package models.entities.filters;

public interface Pagination extends Filter {

	public abstract Integer getLimit();

	public abstract void setLimit(Integer limit);

	public abstract Integer getOffset();

	public abstract void setOffset(Integer offset);

}