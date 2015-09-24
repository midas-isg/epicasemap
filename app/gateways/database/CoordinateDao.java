package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import models.entities.Coordinate;
import models.filters.CoordinateFilter;
import models.filters.Filter;
import models.filters.Filter.Order;

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
		Map<String, Object> equalityMap = filter.getEqualities();
		equalityMap.put("seriesId", filter.getSeriesId());
		
		LinkedHashMap<String, Order> order = filter.getOrders();
		order.put(timestamp, Filter.Order.ASC);
		return super.query(filter);
	}
}