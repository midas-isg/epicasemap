package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.entities.filters.Filter;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class ResponseWrapper {
	private ResponseWrapper() {
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
		Object response = ResponseWrapper.wrap(results, filter);
		return Controller.ok(Json.toJson(response));
	}

	public static Result okAsWrappedJsonObject(Object result, Filter filter) {
		Object response = ResponseWrapper.wrap(result, filter);
		return Controller.ok(Json.toJson(response));
	}
}
