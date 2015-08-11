package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import models.entities.Location;
import models.entities.LocationFilter;
import models.entities.filters.Filter.Order;

public class LocationDao extends DataAccessObject<Location> {
	public LocationDao(EntityManager em) {
		this(new JpaAdaptor(em));
	}

	private LocationDao(JpaAdaptor helper) {
		super(Location.class, helper);
	}

	public List<Location> query(LocationFilter filter) {
		Map<String, Object> equalityMap = new HashMap<>();
		equalityMap.put("alsId", filter.getAlsId());
		equalityMap.put("latitude", filter.getLatitude());
		equalityMap.put("longitude", filter.getLongitude());
		
		filter.setEqualities(equalityMap);

		LinkedHashMap<String, Order> order = new LinkedHashMap<>();
		filter.setOrder(order);
		return super.query(filter);
	}
}
