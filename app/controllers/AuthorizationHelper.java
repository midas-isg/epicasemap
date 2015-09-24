package controllers;

import interactors.AuthorizationRule;

import java.util.ArrayList;
import java.util.List;

import models.filters.Restriction;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.Http.Context;
import controllers.security.Restricted;
import controllers.security.Restricted.Access;


public class AuthorizationHelper {
	private AuthorizationHelper() {
	}
	
	public static boolean isSeriesPermitted(Long seriesId) {
		final List<Long> permittedSeriesIds = findPermittedSeriesIds();
		if (permittedSeriesIds == null)
			return true;
		return permittedSeriesIds.contains(seriesId);
	}

	static List<Long> findPermittedSeriesIds() {
		final Long accountId = readAccountId();
		List<Access> accesses = readAccesses();
		if (accountId == null && accesses == null)
			return null;
		final Restriction restriction = new Restriction(accountId, accesses);
		return makeAuthorizationRule().findSeriesIds(restriction);
	}

	private static AuthorizationRule makeAuthorizationRule() {
		return Factory.makeAuthorizationRule(JPA.em());
	}

	private static Long readAccountId() {
		final String accountId = read("id");
		if (accountId == null)
			return null;

		return Long.parseLong(accountId);
	}

	private static String read(String key) {
		final Context ctx = ctx();
		if (ctx == null)
			return null;
		final Object val = ctx.args.get(key);
		return val == null ? null : val.toString();
	}

	private static Context ctx() {
		try {
			return Controller.ctx();
		} catch (Exception e) {
			return null;
		}
	}

	private static List<Access> readAccesses() {
		final String restriction = read(Restricted.KEY);
		if (restriction == null)
			return null;
		final String[] tokens = restriction.split(Restricted.DELIMITER);
		List<Access> accesses = new ArrayList<>(tokens.length); 
		for (String token : tokens){
			accesses.add(Access.valueOf(token));
		}
		return accesses;
	}
}