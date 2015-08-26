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

public class LocationDao extends DataAccessObject<Location> {
	public LocationDao(EntityManager em) {
		this(new JpaAdaptor(em));
	}

	private LocationDao(JpaAdaptor helper) {
		super(Location.class, helper);
	}

	public List<Location> query(LocationFilter filter) {
		populateEqualities(filter);
		populateInOperators(filter);
		populateOrder(filter);
		return super.query(filter);
	}

	private void populateEqualities(LocationFilter filter) {
		Map<String, Object> equalityMap = new HashMap<>();
		equalityMap.put("alsId", filter.getAlsId());
		equalityMap.put("latitude", filter.getLatitude());
		equalityMap.put("longitude", filter.getLongitude());
		filter.setEqualities(equalityMap);
	}

	private void populateInOperators(LocationFilter filter) {
		Map<String, List<?>> inMap = new HashMap<>();
		inMap.put("id", filter.getIds());
		filter.setInOperators(inMap);
	}

	private void populateOrder(LocationFilter filter) {
		filter.setOrder(new LinkedHashMap<>());
	}
}
