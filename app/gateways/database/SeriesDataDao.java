package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import javax.persistence.EntityManager;

import models.entities.SeriesData;

public class SeriesDataDao extends DataAccessObject<SeriesData> {
	public SeriesDataDao(EntityManager em){
		this(new JpaAdaptor(em));
	}

	private SeriesDataDao(JpaAdaptor helper){
		super(SeriesData.class, helper);
	}
}
