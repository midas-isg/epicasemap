package models.entities.filters;

import java.util.LinkedHashMap;
import java.util.Map;

public interface Filter {

	public enum Order{
		ASC,
		DESC
	};
	
	public abstract Map<String, Object> getEqualities();

	public abstract LinkedHashMap<String, Order> getOrder();
}
