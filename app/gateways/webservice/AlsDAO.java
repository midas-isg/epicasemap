package gateways.webservice;

import static play.mvc.Http.Status.OK;
import gateways.configuration.AppKey;
import interactors.ClientRule;
import interactors.ConfRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import models.entities.Location;
import play.libs.ws.WSResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import controllers.Factory;

public class AlsDAO {

	private static final String NAME = "name";
	private static final String BBOX = "bbox";
	private static final String LONGITUDE = "longitude";
	private static final String LATITUDE = "latitude";

	private static String baseUrl;

	static {
		final ConfRule confRule = Factory.makeConfRule();
		baseUrl = confRule.readString(AppKey.ALS_WS_URL.key()) + "/api/locations";
	}

	public Location getLocationFromAls(Long id) {
		ClientRule clientRule = makeAlsClientRule();
		Location location = toLocation(clientRule.getById(id));
		location.setAlsId(id);
		return location;

	}

	private ClientRule makeAlsClientRule() {
		return new ClientRule(baseUrl);

	}

	private Location toLocation(WSResponse wsResponse) {
		if (wsResponse.getStatus() == OK)
			return toLocation(wsResponse.asJson());
		else
			throw new RuntimeException(wsResponse.getStatusText());

	}

	Location toLocation(JsonNode jsonNode) {
		Location location = new Location();
		Map<String, Double> center = centroid(getBbox(jsonNode));

		location.setLabel(getName(jsonNode));
		location.setLatitude(center.get(LATITUDE));
		location.setLongitude(center.get(LONGITUDE));

		return location;
	}

	private ArrayNode getBbox(JsonNode jsonNode) {
		return (ArrayNode) jsonNode.get(BBOX);
	}

	private String getName(JsonNode jsonNode) {

		ArrayList<String> names = new ArrayList<>();
		names.add(getLocationName(jsonNode));
		names.addAll(getParentsNames(jsonNode));
		return joinNames(names);
	}

	private String joinNames(ArrayList<String> names) {
		StringJoiner sj = new StringJoiner(", ");
		for (String name : names) {
			sj.add(name);
		}
		return sj.toString();
	}

	private ArrayList<String> getParentsNames(JsonNode jsonNode) {
		ArrayList<String> names = new ArrayList<>();
		ArrayNode parents = getParents(jsonNode);
		for (int i = parents.size() - 1; i >= 0; i--) {
			names.add(parents.get(i).get("name").asText());
		}
		return names;
	}

	private ArrayNode getParents(JsonNode jsonNode) {
		return (ArrayNode) getProperties(jsonNode).get("lineage");
	}

	private String getLocationName(JsonNode jsonNode) {
		return getProperties(jsonNode).get(NAME).asText();
	}

	private static JsonNode getProperties(JsonNode jsonNode) {
		return jsonNode.get("features").get(0).get("properties");
	}

	private static Map<String, Double> centroid(ArrayNode bBox) {
		Map<String, Double> center = new HashMap<>();

		center.put(LONGITUDE, centerX(bBox));
		center.put(LATITUDE, centerY(bBox));

		return center;
	}

	private static Double centerY(ArrayNode bBox) {
		Double y1 = bBox.get(1).asDouble();
		Double y2 = bBox.get(3).asDouble();

		Double midY = mid(y1, y2);
		return midY;
	}

	private static Double centerX(ArrayNode bBox) {
		Double x1 = bBox.get(0).asDouble();
		Double x2 = bBox.get(2).asDouble();

		Double midX = mid(x1, x2);
		return midX;
	}

	private static double mid(Double a, Double b) {
		return Math.min(a, b) + ((Math.abs(b - a)) / 2);
	}

}
