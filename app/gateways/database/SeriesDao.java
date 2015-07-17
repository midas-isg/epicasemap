package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import javax.persistence.EntityManager;

import models.entities.Series;

public class SeriesDao extends DataAccessObject<Series> {
	public SeriesDao(EntityManager em){
		this(new JpaAdaptor(em));
	}

	private SeriesDao(JpaAdaptor helper){
		super(Series.class, helper);
	}
}
