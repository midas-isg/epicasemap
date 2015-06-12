package gateways.database;

import java.util.List;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import javax.persistence.EntityManager;

import models.entities.Geotag;
import models.entities.GeotagFilter;

public class GeotagDao extends DataAccessObject<Geotag> {
	public GeotagDao(EntityManager em){
		this(new JpaAdaptor(em));
	}

	public GeotagDao(JpaAdaptor helper){
		super(Geotag.class, helper);
	}

	public List<Geotag> query(GeotagFilter filter) {
		Integer limit = filter.getLimit();
		Integer offset = filter.getOffset();
		return super.query(limit, offset);
	}
}