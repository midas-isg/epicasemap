package controllers;

import gateways.configuration.ConfReader;
import gateways.database.CoordinateTimeDao;
import interactors.ConfRule;
import interactors.CoordinateTimeRule;

import javax.persistence.EntityManager;

public class Factory {
	private Factory() {
	}

	public static CoordinateTimeRule makeCoordinateTimeRule(EntityManager em) {
		CoordinateTimeDao dao = new CoordinateTimeDao(em);
		return new CoordinateTimeRule(dao);
	}

	public static ConfRule makeConfRule() {
		return new ConfRule(makeConfReader());
	}

	private static ConfReader makeConfReader() {
		return new ConfReader();
	}

	public static CoordinateTimeDao makeCoordinateTimeDao(EntityManager em) {
		return new CoordinateTimeDao(em);
	}
}

