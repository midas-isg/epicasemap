package gateways.configuration;

import java.lang.reflect.Method;

import models.NotFoundException;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;

public class Global extends GlobalSettings {
	private static final String appName = "MidasViz";

	@Override
	public void onStart(Application app) {
		Logger.info(appName + " has started");
	}

	@Override
	public void onStop(Application app) {
		Logger.info(appName + " shutdown...");
	}

	/**
	 * Play 2.3.9 didn't catch Throwable and crashed so we catch them ourselves.
	 */
	@Override
	public Action<?> onRequest(Request request, Method actionMethod) {
		return new Action.Simple() {
			public F.Promise<Result> call(Context ctx) throws Throwable {
				try {
					return delegate.call(ctx);
				} catch (NotFoundException e) {
					final String message = e.getMessage();
					final Status status = notFound(message);
					return Promise.<Result>pure(status);
				} catch (RuntimeException e) {
					throw e;
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
}