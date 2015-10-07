package controllers.security;

import interactors.VizAuthorizer;
import interactors.SeriesAuthorizer;

import java.util.Arrays;
import java.util.List;

import models.filters.Restriction;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.Http.Context;
import controllers.Factory;
import controllers.security.Restricted.Access;


public class AuthorizationKit {
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
		return makeSeriesAuthorizer().findSeriesIds(restriction);
	}

	public static List<Long> findPermittedVizIds() {
		final Long accountId = readAccountId();
		List<Access> accesses = readAccesses();
		if (accountId == null && accesses == null)
			return null;
		final Restriction restriction = new Restriction(accountId, accesses);
		final List<Long> ids = makeVizAuthorizer().findVizIds(restriction);
		return ids;
	}

	private static VizAuthorizer makeVizAuthorizer() {
		return Factory.makeVizAuthorizer(JPA.em());
	}

	private static Long readAccountId() {
		final String accountId = Authentication.readAccountId(ctx());
		if (accountId == null)
			return null;

		return Long.parseLong(accountId);
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
	
	private static SeriesAuthorizer makeSeriesAuthorizer() {
		return Factory.makeSeriesAuthorizer(JPA.em());
	}
	
	public static boolean hasLoggedIn(){
		return Authentication.readAccountId(ctx()) != null;
	}
}