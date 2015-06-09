package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import javax.persistence.EntityManager;

import models.entities.Geotag;

public class GeotagDao extends DataAccessObject<Geotag> {
	public GeotagDao(EntityManager em){
		this(new JpaAdaptor(em));
	}

	public GeotagDao(JpaAdaptor helper){
		super(Geotag.class, helper);
	}
}