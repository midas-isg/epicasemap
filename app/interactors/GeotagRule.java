package interactors;

import gateways.database.GeotagDao;

import java.util.List;

import models.entities.Geotag;
import models.entities.GeotagFilter;

public class GeotagRule {
	private GeotagDao dao;
	
	public GeotagRule(GeotagDao dao){
		this.dao = dao;
	}
	
	public List<Geotag> query(GeotagFilter filter) {
		return dao.query(filter);
	}
}
