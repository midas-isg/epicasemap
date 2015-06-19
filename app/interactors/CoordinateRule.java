package interactors;

import gateways.database.CoordinateDao;

import java.util.List;

import models.entities.Coordinate;
import models.entities.CoordinateFilter;

public class CoordinateRule {
	private CoordinateDao dao;
	
	public CoordinateRule(CoordinateDao dao){
		this.dao = dao;
	}
	
	public List<Coordinate> query(CoordinateFilter filter) {
		return dao.query(filter);
	}
}
