package controllers.security;

import interactors.AuthorizationRule;

import java.util.Arrays;
import java.util.List;

import models.filters.Restriction;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.Http.Context;
import controllers.Factory;
import controllers.security.Restricted.Access;


public class AuthorizationKit {
	private static final String ID = "id";

	private AuthorizationKit() {
	}
	
	public static boolean isSeriesPermitted(Long seriesId) {
		final List<Long> permittedSeriesIds = findPermittedSeriesIds();
		if (permittedSeriesIds == null)
			return true;
		return permittedSeriesIds.contains(seriesId);
	}

	public static List<Long> findPermittedSeriesIds() {
		final Long accountId = readAccountId();
		List<Access> accesses = readAccesses();
		if (accountId == null && accesses == null)
			return null;
		final Restriction restriction = new Restriction(accountId, accesses);
		return makeRule().findSeriesIds(restriction);
	}

	static void writeAccountId(Context ctx, Object value) {
		ctx.args.put(ID, value);
	}
	
	private static Long readAccountId() {
		final String accountId = readString(ID);
		if (accountId == null)
			return null;

		return Long.parseLong(accountId);
	}

	private static String readString(String key) {
		final Object val = read(key);
		return val == null ? null : val.toString();
	}

	private static Object read(String key) {
		final Context ctx = ctx();
		if (ctx == null)
			return null;
		return ctx.args.get(key);
	}

	private static Context ctx() {
		try {
			return Controller.ctx();
		} catch (Exception e) {
			return null;
		}
	}

	static void writeAccesses(Context ctx, Access[] accesses) {
		ctx.args.put(Restricted.KEY, Arrays.asList(accesses));
	}

	@SuppressWarnings("unchecked")
	private static List<Access> readAccesses() {
		return (List<Access>)read(Restricted.KEY);
	}
	
	private static AuthorizationRule makeRule() {
		return Factory.makeAuthorizationRule(JPA.em());
	}
	
	public static boolean hasLoggedIn(){
		return Authentication.readAccountId(ctx()) != null;
	}
}