package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import models.entities.Coordinate;
import models.entities.CoordinateFilter;

public class CoordinateDao extends DataAccessObject<Coordinate> {
	public CoordinateDao(EntityManager em){
		this(new JpaAdaptor(em));
	}

	public CoordinateDao(JpaAdaptor helper){
		super(Coordinate.class, helper);
	}

	public List<Coordinate> query(CoordinateFilter filter) {
		filter.setTimestampAttribute("timestamp");
		Map<String, Object> equalityMap = new HashMap<>();
		equalityMap.put("seriesId", filter.getSeriesId());
		filter.setEqualities(equalityMap);
		return super.query(filter);
	}
}