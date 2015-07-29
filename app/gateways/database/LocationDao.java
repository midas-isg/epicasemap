package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import javax.persistence.EntityManager;

import models.entities.Location;

public class LocationDao extends DataAccessObject<Location> {
	public LocationDao(EntityManager em){
		this(new JpaAdaptor(em));
	}

	private LocationDao(JpaAdaptor helper){
		super(Location.class, helper);
	}
}
