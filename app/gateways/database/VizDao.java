package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import javax.persistence.EntityManager;

import models.entities.Visualization;

public class VizDao extends DataAccessObject<Visualization> {
	public VizDao(EntityManager em){
		this(new JpaAdaptor(em));
	}

	private VizDao(JpaAdaptor helper){
		super(Visualization.class, helper);
	}
}