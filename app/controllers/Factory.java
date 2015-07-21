package controllers;

import gateways.configuration.ConfReader;
import gateways.database.CoordinateDao;
import gateways.database.LocationDao;
import gateways.database.SeriesDao;
import gateways.database.SeriesDataDao;
import gateways.database.VizDao;
import interactors.ConfRule;
import interactors.CoordinateRule;
import interactors.LocationRule;
import interactors.SeriesDataRule;
import interactors.SeriesRule;
import interactors.VizRule;

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
		return new SeriesRule(dao);
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
		return new LocationRule(dao); 
	}
}
