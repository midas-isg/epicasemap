package controllers;

import gateways.configuration.ConfReader;
import gateways.database.GeotagDao;
import interactors.ConfRule;
import interactors.GeotagRule;

import javax.persistence.EntityManager;

public class Factory {
	private Factory() {
	}

	public static GeotagRule makeGeotagRule(EntityManager em) {
		GeotagDao dao = new GeotagDao(em);
		return new GeotagRule(dao);
	}

	public static ConfRule makeConfRule() {
		return new ConfRule(makeConfReader());
	}

	private static ConfReader makeConfReader() {
		return new ConfReader();
	}

	public static GeotagDao makeGeotagDao(EntityManager em) {
		return new GeotagDao(em);
	}
}

