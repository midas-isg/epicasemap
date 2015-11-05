package controllers.security;

import interactors.Authorizer;
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
		return isPermitted(seriesId, findPermittedSeriesIds());
	}

	private static List<Long> findPermittedSeriesIds() {
		final long accountId = readAccountId();
		List<Access> accesses = readAccesses();
		if (accesses == null)
			return null;
		return findPermittedSeriesIds(accountId, accesses);
	}

	public static List<Long> findPermittedSeriesIds(long accountId,	List<Access> accesses) {
		final Restriction restriction = new Restriction(accountId, accesses);
		return makeSeriesAuthorizer().findSeriesIds(restriction);
	}

	public static boolean isVizPermitted(Long vizId) {
		return isPermitted(vizId, findPermittedVizIds());
	}

	private static List<Long> findPermittedVizIds() {
		final long accountId = readAccountId();
		List<Access> accesses = readAccesses();
		if (accesses == null)
			return null;
		final Restriction restriction = new Restriction(accountId, accesses);
		return makeVizAuthorizer().findVizIds(restriction);
	}

	private static boolean isPermitted(Long requestedId,
			final List<Long> permittedIds) {
		if (permittedIds == null)
			return true;
		return permittedIds.contains(requestedId);
	}

	private static VizAuthorizer makeVizAuthorizer() {
		return Factory.makeVizAuthorizer(JPA.em());
	}

	public static Long readAccountId() {
		final String accountId = Authentication.readAccountId(ctx());
		if (accountId == null)
			return Authorizer.publicAccountId;

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