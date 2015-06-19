package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseWrapper {
	private ResponseWrapper() {
	}

	static Map<String, Object> wrap(List<?> results, Object filter) {
		Map<String, Object> response = new HashMap<>();
		response.put("filter", filter);
		response.put("results", results);
		return response;
	}
}
