package models.entities.filters;

import java.util.Map;

public interface Filter {
	public abstract Map<String, Object> getEqualities();
}
