package controllers;

import gateways.configuration.ConfReader;
import gateways.database.CoordinateDao;
import interactors.ConfRule;
import interactors.CoordinateRule;

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
}

