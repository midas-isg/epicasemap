package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import models.entities.Coordinate;
import models.entities.CoordinateFilter;
import models.entities.filters.Filter;
import models.entities.filters.Filter.Order;

public class CoordinateDao extends DataAccessObject<Coordinate> {
	public CoordinateDao(EntityManager em){
		this(new JpaAdaptor(em));
	}

	public CoordinateDao(JpaAdaptor helper){
		super(Coordinate.class, helper);
	}

	public List<Coordinate> query(CoordinateFilter filter) {
		final String timestamp = "timestamp";
		filter.setTimestampAttribute(timestamp);
		Map<String, Object> equalityMap = new HashMap<>();
		equalityMap.put("seriesId", filter.getSeriesId());
		filter.setEqualities(equalityMap);
		
		LinkedHashMap<String, Order> order = new LinkedHashMap<>();
		order.put(timestamp, Filter.Order.ASC);
		filter.setOrder(order);
		return super.query(filter);
	}
}