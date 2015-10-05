package models.filters;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GenericFilter implements Filter {
	private Map<String, Object> equalities = new HashMap<>();
	private LinkedHashMap<String, Order> order = new LinkedHashMap<>();
	private Map<String, List<?>> ins = new HashMap<>();
	private Map<String, Object> disjuncts = new HashMap<>();

	private Restriction restriction;

	@Override
	public Map<String, Object> getEqualities() {
		if (equalities == null)
			return Collections.emptyMap();
		return equalities;
	}

	@Override
	public Map<String, List<?>>  getInOperators(){
		if (ins == null)
			return Collections.emptyMap();
		return ins;
	}

	@Override
	public LinkedHashMap<String, Order> getOrders() {
		if (order == null)
			return new LinkedHashMap<>();
		return order;
	}

	@Override
	public Restriction getRestriction() {
		return restriction;
	}
	
	public void setRestriction(Restriction restriction) {
		this.restriction = restriction;
	}

	@Override
	public Map<String, Object> getDisjunctiveEqualities() {
		return disjuncts;
	}
}