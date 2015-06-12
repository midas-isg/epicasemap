package interactors;

import gateways.database.CoordinateTimeDao;

import java.util.List;

import models.entities.CoordinateTime;
import models.entities.CoordinateTimeFilter;

public class CoordinateTimeRule {
	private CoordinateTimeDao dao;
	
	public CoordinateTimeRule(CoordinateTimeDao dao){
		this.dao = dao;
	}
	
	public List<CoordinateTime> query(CoordinateTimeFilter filter) {
		return dao.query(filter);
	}
}
