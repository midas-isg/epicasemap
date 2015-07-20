package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import javax.persistence.EntityManager;

import models.entities.Viz;

public class VizDao extends DataAccessObject<Viz> {
	public VizDao(EntityManager em){
		this(new JpaAdaptor(em));
	}

	private VizDao(JpaAdaptor helper){
		super(Viz.class, helper);
	}
}