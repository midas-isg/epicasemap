package interactors;

import gateways.database.GeotagDao;

import java.util.List;

import models.entities.Geotag;

public class GeotagRule {
	private GeotagDao dao;
	
	public GeotagRule(GeotagDao dao){
		this.dao = dao;
	}
	
	public List<Geotag> findAll() {
		return dao.findAll();
	}
}
