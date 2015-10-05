package models.filters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface Filter {

	public enum Order{
		ASC,
		DESC
	};
	
	public abstract Map<String, Object> getEqualities();
	
	public abstract Map<String, List<?>> getInOperators();
	
	public abstract LinkedHashMap<String, Order> getOrders();
	
	public abstract Map<String, Object> getDisjunctiveEqualities();

	public abstract Restriction getRestriction();
}
