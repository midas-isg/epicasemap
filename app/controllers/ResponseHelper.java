package controllers;

import static play.mvc.Controller.response;
import static play.mvc.Http.HeaderNames.LOCATION;
import static play.mvc.Http.HeaderNames.ORIGIN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.filters.Filter;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;

public class ResponseHelper {
	private ResponseHelper() {
	}

	static Map<String, Object> wrap(List<?> results, Object filter) {
		Map<String, Object> response = new HashMap<>();
		response.put("filter", filter);
		response.put("results", results);
		return response;
	}

	static Map<String, Object> wrap(Object result, Object filter) {
		Map<String, Object> response = new HashMap<>();
		response.put("filter", filter);
		response.put("result", result);
		return response;
	}

	public static Result okAsWrappedJsonArray(List<?> results, Filter filter) {
		Object response = ResponseHelper.wrap(results, filter);
		return Controller.ok(Json.toJson(response));
	}

	public static Result okAsWrappedJsonObject(Object result, Filter filter) {
		Object response = ResponseHelper.wrap(result, filter);
		return Controller.ok(Json.toJson(response));
	}

	public static void setResponseLocationFromRequest(String... tails) {
		String url = makeUriFromRequest();
		for (String tail : tails)
			url += "/" + tail;
		response().setHeader(LOCATION, url);
	}

	private static String makeUriFromRequest() {
		Request request = Context.current().request();
		return request.getHeader(ORIGIN) + request.path();
	}
}
