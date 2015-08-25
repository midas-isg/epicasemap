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
		SeriesDao dao = new SeriesDao(em);
		final SeriesRule seriesRule = new SeriesRule(dao);
		seriesRule.setCoordinateRule(makeCoordinateRule(em));
		seriesRule.setSeriesDataRule(makeSeriesDataRule(em));
		return seriesRule;
	}

	public static VizRule makeVizRule(EntityManager em) {
		VizDao dao = new VizDao(em);
		VizRule vizRule = new VizRule(dao);
		final SeriesRule rule = makeSeriesRule(em);
		vizRule.setSeriesRule(rule);
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

	public static Persister makePersister(EntityManager em) {
		Persister persister = new Persister();
		persister.setLocationRule(makeLocationRule(em));
		persister.setSeriesRule(makeSeriesRule(em));
		persister.setSeriesDataRule(makeSeriesDataRule(em));
		persister.setParser(new Parser());
		return persister;
	}

	public static Validator makeValidator(EntityManager em) {
		Validator validator = new Validator();
		validator.setLocationRule(makeLocationRule(em));
		validator.setParser(new Parser());
		return validator;
	}
}
