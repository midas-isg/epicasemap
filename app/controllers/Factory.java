package controllers;

import gateways.configuration.ConfReader;
import gateways.database.CoordinateDao;
import gateways.database.SeriesDao;
import gateways.database.VizDao;
import interactors.ConfRule;
import interactors.CoordinateRule;
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

	public static CoordinateDao makeCoordinateDao(EntityManager em) {
		return new CoordinateDao(em);
	}
	
	public static SeriesRule makeSeriesRule(EntityManager em) {
		SeriesDao dao = new SeriesDao(em);
		return new SeriesRule(dao);
	}
	
	public static VizRule makeVizRule(EntityManager em) {
		VizDao dao = new VizDao(em);
		return new VizRule(dao);
	}
}

