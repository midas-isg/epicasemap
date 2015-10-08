package controllers;

import gateways.configuration.ConfReader;
import gateways.database.AccountDao;
import gateways.database.CoordinateDao;
import gateways.database.LocationDao;
import gateways.database.PermissionDao;
import gateways.database.SeriesDao;
import gateways.database.SeriesDataDao;
import gateways.database.VizDao;
import gateways.webservice.AlsDao;
import interactors.AccountRule;
import interactors.ConfRule;
import interactors.CoordinateRule;
import interactors.LocationRule;
import interactors.SeriesAuthorizer;
import interactors.SeriesDataRule;
import interactors.SeriesRule;
import interactors.VizAuthorizer;
import interactors.VizRule;
import interactors.security.password.Authenticator;
import interactors.security.password.PasswordFactory;
import interactors.series_data_file.Parser;
import interactors.series_data_file.Persister;
import interactors.series_data_file.Validator;

import javax.persistence.EntityManager;

import models.SeriesDataFile;
import models.entities.SeriesPermission;
import models.entities.VizPermission;
import play.db.jpa.JPA;

public class Factory {
	private Factory() {
	}

	public static CoordinateRule makeCoordinateRule(EntityManager em) {
		final CoordinateDao dao = new CoordinateDao(em);
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
		final SeriesDao dao = new SeriesDao(em);
		final SeriesRule seriesRule = new SeriesRule(dao);
		seriesRule.setCoordinateRule(makeCoordinateRule(em));
		seriesRule.setSeriesDataRule(makeSeriesDataRule(em));
		if (vizRule == null)
			vizRule = makeVizRule(em, seriesRule);
		seriesRule.setVizRule(vizRule);
		seriesRule.setAccountRule(makeAccountRule(em));
		return seriesRule;
	}

	public static VizRule makeVizRule(EntityManager em) {
		return makeVizRule(em, null);
	}

	private static VizRule makeVizRule(EntityManager em, SeriesRule seriesRule) {
		final VizDao dao = new VizDao(em);
		final VizRule vizRule = new VizRule(dao);
		if (seriesRule == null) 
			seriesRule = makeSeriesRule(em, vizRule);
		vizRule.setSeriesRule(seriesRule);
		vizRule.setAccountRule(makeAccountRule(em));

		return vizRule;
	}

	public static SeriesDataRule makeSeriesDataRule(EntityManager em) {
		final SeriesDataDao dao = new SeriesDataDao(em);
		return new SeriesDataRule(dao);
	}

	public static LocationRule makeLocationRule(EntityManager em) {
		final LocationDao dao = new LocationDao(em);
		final AlsDao alsDao = new AlsDao();
		final LocationRule locationRule = new LocationRule(dao);
		locationRule.setAlsDao(alsDao);
		return locationRule;
	}

	public static Persister makePersister(SeriesDataFile dataFile) {
		final Persister persister = new Persister();
		persister.setLocationRule(makeLocationRule(JPA.em()));
		persister.setSeriesRule(makeSeriesRule(JPA.em()));
		persister.setSeriesDataRule(makeSeriesDataRule(JPA.em()));
		persister.setParser(new Parser(dataFile));
		persister.setSeriesDataFile(dataFile);
		return persister;
	}

	public static Validator makeValidator(SeriesDataFile dataFile) {
		final Validator validator = new Validator();
		validator.setLocationRule(makeLocationRule(JPA.em()));
		validator.setDataFile(dataFile);
		validator.setParser(new Parser(dataFile));
		return validator;
	}
	
	public static AccountRule makeAccountRule(EntityManager em){
		final AccountDao dao = new AccountDao(em);
		final AccountRule rule = new AccountRule(dao);
		final Authenticator authority = PasswordFactory.makeAuthority("MidasViz");
		rule.setAuthenticator(authority);
		return rule;
	}
	
	
	public static SeriesAuthorizer makeSeriesAuthorizer(EntityManager em){
		final PermissionDao<SeriesPermission> dao = new PermissionDao<>(em, SeriesPermission.class);
		SeriesAuthorizer authorizer = new SeriesAuthorizer(dao);
		authorizer.setSeriesRule(makeSeriesRule(em));
		authorizer.setAccountRule(makeAccountRule(em));
		return authorizer;
	}

	public static VizAuthorizer makeVizAuthorizer(EntityManager em){
		final PermissionDao<VizPermission> dao = new PermissionDao<>(em, VizPermission.class);
		final VizAuthorizer authorizer = new VizAuthorizer(dao);
		authorizer.setVizRule(makeVizRule(em));
		authorizer.setAccountRule(makeAccountRule(em));
		return authorizer;
	}
}