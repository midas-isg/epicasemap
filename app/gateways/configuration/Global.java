package gateways.configuration;

import static play.mvc.Http.Status.SERVICE_UNAVAILABLE;

import java.lang.reflect.Method;

import models.exceptions.ConstraintViolation;
import models.exceptions.NoConnectionAvailable;
import models.exceptions.NotFound;
import models.exceptions.Unauthorized;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.F;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;

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
	public Action<Void> onRequest(Request request, Method actionMethod) {
		return new Action.Simple() {
			public F.Promise<Result> call(Context ctx) throws Throwable {
				try {
					return delegate.call(ctx);
				} catch (NotFound e) {
					final Status status = notFound(toErrorMessageInJson(e));
					return Promise.<Result>pure(status);
				} catch (ConstraintViolation e) {
					final Status status = forbidden(toErrorMessageInJson(e));
					return Promise.<Result>pure(status);
				} catch (Unauthorized e) {
					final Status status = unauthorized(toErrorMessageInJson(e));
					return Promise.<Result>pure(status);
				} catch (NoConnectionAvailable e){
					final Status status = serviceUnavailable(toErrorMessageInJson(e));
					return Promise.<Result>pure(status);
				}catch (RuntimeException|Error e) {
					throw e;
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}

			private Status serviceUnavailable(JsonNode content) {
				return status(SERVICE_UNAVAILABLE, content);
			}

			private JsonNode toErrorMessageInJson(Exception e) {
				return Json.toJson(toErrorMessage(e));
			}

			private ErrorMessage toErrorMessage(Exception e) {
				ErrorMessage em = new ErrorMessage();
				em.userMessage = e.getMessage();
				em.type = e.getClass().getSimpleName();
				verbose(e, em);
				return em;
			}

			private void verbose(Exception e, ErrorMessage em) {
				em.stackTrace = e.getStackTrace();
				em.cause = e.getCause();
			}
		};
	}
}

@JsonInclude(Include.NON_NULL)
class ErrorMessage {
	public String userMessage;
	public Object develperClue;
	public String type;
	public Throwable cause;
	public StackTraceElement[] stackTrace;
}