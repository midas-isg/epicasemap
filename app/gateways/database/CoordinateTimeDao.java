package gateways.database;

import java.util.List;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import javax.persistence.EntityManager;

import models.entities.CoordinateTime;
import models.entities.CoordinateTimeFilter;

public class CoordinateTimeDao extends DataAccessObject<CoordinateTime> {
	public CoordinateTimeDao(EntityManager em){
		this(new JpaAdaptor(em));
	}

	public CoordinateTimeDao(JpaAdaptor helper){
		super(CoordinateTime.class, helper);
	}

	public List<CoordinateTime> query(CoordinateTimeFilter filter) {
		Integer limit = filter.getLimit();
		Integer offset = filter.getOffset();
		return super.query(limit, offset);
	}
}