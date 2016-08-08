package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import play.Logger;
import models.entities.Location;
import models.filters.LocationFilter;

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
		Map<String, Object> equalityMap = filter.getEqualities();
		equalityMap.put("alsId", filter.getAlsId());
		equalityMap.put("latitude", filter.getLatitude());
		equalityMap.put("longitude", filter.getLongitude());
		
		return;
	}

	private void populateInOperators(LocationFilter filter) {
		Map<String, List<?>> inMap = filter.getInOperators();
		inMap.put("id", filter.getIds());
		
		return;
	}

	private void populateOrder(LocationFilter filter) {
		return;
	}
}
