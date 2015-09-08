package controllers;

import gateways.configuration.ConfReader;
import gateways.database.CoordinateDao;
import gateways.database.LocationDao;
import gateways.database.SeriesDao;
import gateways.database.SeriesDataDao;
import gateways.database.VizDao;
import gateways.webservice.AlsDao;
import interactors.ConfRule;
import interactors.CoordinateRule;
import interactors.LocationRule;
import interactors.SeriesDataRule;
import interactors.SeriesRule;
import interactors.VizRule;
import interactors.series_data_file.Parser;
import interactors.series_data_file.Persister;
import interactors.series_data_file.Validator;

import javax.persistence.EntityManager;

import models.SeriesDataFile;
import play.db.jpa.JPA;

public class Factory {
	private Factory() {
	}

	public static CoordinateRule makeCoordinateRule(EntityManager em) {
		CoordinateDao dao = new CoordinateDao(em);
		return new CoordinateRule(dao);
	}

	public static ConfRule makeConfRule() {
		return new ConfRule(makeConfReader());
	}

	private static ConfReader makeConfReader() {
		return new ConfReader();
	}

	public static SeriesRule makeSeriesRule(EntityManager em) {
		return makeSeriesRule(em, null);
	}

	private static SeriesRule makeSeriesRule(EntityManager em, VizRule vizRule) {
		SeriesDao dao = new SeriesDao(em);
		SeriesRule seriesRule = new SeriesRule(dao);
		seriesRule.setCoordinateRule(makeCoordinateRule(em));
		seriesRule.setSeriesDataRule(makeSeriesDataRule(em));
		if (vizRule == null)
			vizRule = makeVizRule(em, seriesRule);
		seriesRule.setVizRule(vizRule);
		return seriesRule;
	}

	public static VizRule makeVizRule(EntityManager em) {
		return makeVizRule(em, null);
	}

	private static VizRule makeVizRule(EntityManager em, SeriesRule seriesRule) {
		VizDao dao = new VizDao(em);
		VizRule vizRule = new VizRule(dao);
		if (seriesRule == null) 
			seriesRule = makeSeriesRule(em, vizRule);
		vizRule.setSeriesRule(seriesRule);

		return vizRule;
	}

	public static SeriesDataRule makeSeriesDataRule(EntityManager em) {
		SeriesDataDao dao = new SeriesDataDao(em);
		return new SeriesDataRule(dao);
	}

	public static LocationRule makeLocationRule(EntityManager em) {
		LocationDao dao = new LocationDao(em);
		AlsDao alsDao = new AlsDao();
		LocationRule locationRule = new LocationRule(dao);
		locationRule.setAlsDao(alsDao);
		return locationRule;
	}

	public static Persister makePersister(SeriesDataFile dataFile) {
		Persister persister = new Persister();
		persister.setLocationRule(makeLocationRule(JPA.em()));
		persister.setSeriesRule(makeSeriesRule(JPA.em()));
		persister.setSeriesDataRule(makeSeriesDataRule(JPA.em()));
		persister.setParser(new Parser(dataFile));
		persister.setSeriesDataFile(dataFile);
		return persister;
	}

	public static Validator makeValidator(SeriesDataFile dataFile) {
		Validator validator = new Validator();
		validator.setLocationRule(makeLocationRule(JPA.em()));
		validator.setDataFile(dataFile);
		validator.setParser(new Parser(dataFile));
		return validator;
	}
}
