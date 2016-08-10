package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Http;

import java.util.HashMap;
import java.util.Map;

class ApiAid {
    static JsonNode toJsonNode(Http.Request request) {
        final Http.RequestBody body = request.body();
        final JsonNode json = body.asJson();
        if (json != null)
            return json;
        final Map<String, String[]> map = body.asFormUrlEncoded();
        return Json.toJson(toMapWithoutKeysEndingWithSquareBrackets(map));
    }

    private static Map<String, String[]>
    toMapWithoutKeysEndingWithSquareBrackets(Map<String, String[]> map) {
        Map<String, String[]> result = new HashMap<>(map.size());
        for(Map.Entry<String, String[]> pair : map.entrySet()){
            String key = pair.getKey().replaceAll("\\[]", "");
            result.put(key, pair.getValue());
        }
        return result;
    }
}
