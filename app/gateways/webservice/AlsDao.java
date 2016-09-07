package gateways.webservice;

import static java.net.URLEncoder.encode;
import static play.mvc.Http.Status.OK;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import controllers.Factory;
import gateways.configuration.AppKey;
import interactors.ClientRule;
import interactors.ConfRule;
import lsparser.xmlparser.ALSIDQueryInput;
import models.entities.Location;
import models.entities.NamedLocation;
import play.Logger;
import play.libs.F.Promise;
import play.libs.ws.WSResponse;

public class AlsDao {
    private static final String NAME = "name";
    private static final String BBOX = "bbox";
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";

    private static String locationsUrl;
    private static final String topoJsonUrl;
    public static final String bulkLocationsUrl;
    public static final String baseUrl;

    static {
        final ConfRule confRule = Factory.makeConfRule();
        baseUrl = confRule.readString(AppKey.ALS_WS_URL.key());
        locationsUrl = baseUrl + "/api/locations";
        bulkLocationsUrl = baseUrl + "/api/locations/find-bulk";
        topoJsonUrl = baseUrl + "/api/topojson";
    }

    public Promise<List<NamedLocation>> getLocations(ALSIDQueryInput alsIDQueryInput)
            throws URISyntaxException, UnsupportedEncodingException {
        final String encode = encode(alsIDQueryInput.locationName, "UTF-8");
        String urlQuery = "?q=" + (encode.replaceAll("\\++", "%20"));
        ClientRule clientRule = makeAlsClientRule();
        Promise<WSResponse> promisedWSResponse = clientRule.getAsynchronouslyByQuery(urlQuery);

        return promisedWSResponse.map(wsResponse -> {
            JsonNode jsonResponse = wsResponse.asJson().get("geoJSON");

            return toLocations(jsonResponse);
        });
    }

    public Location getLocationFromAls(Long id) {
        ClientRule clientRule = makeAlsClientRule();
        Location location = toLocation(clientRule.getById(id));
        location.setAlsId(id);

        return location;
    }

    public ClientRule makeAlsClientRule() {
        return new ClientRule(locationsUrl);
    }

    public ClientRule makeTopoJsonClient() {
        return new ClientRule(topoJsonUrl);
    }

    public ClientRule makeAlsClientRule(String inputUrl) {
        return new ClientRule(inputUrl);
    }

    private Location toLocation(WSResponse wsResponse) {
        if (wsResponse.getStatus() == OK) {
            return toLocation(wsResponse.asJson());
        }
        else {
            throw new RuntimeException(wsResponse.getStatusText());
        }
    }

    Location toLocation(JsonNode jsonNode) {
        Location location = new Location();
        Map<String, Double> center = toCentroid(jsonNode);

        if(center != null){
        	location.setLatitude(center.get(LATITUDE));
        	location.setLongitude(center.get(LONGITUDE));
        }
        
        location.setLabel(getName(jsonNode));
        
        return location;
    }

	private Map<String, Double> toCentroid(JsonNode jsonNode) {
        Map<String, Double> centroid = new HashMap<>();  
        JsonNode repPoint = getRepPoint(jsonNode);
        if (repPoint != null){
        	centroid.put(LONGITUDE, repPoint.get(0).asDouble());
        	centroid.put(LATITUDE, repPoint.get(1).asDouble());
        }
        else
        	centroid = toCentroid(getBbox(jsonNode));
        return centroid;

	}

	private JsonNode getRepPoint(JsonNode jsonNode) {
		JsonNode feature = getFeature(jsonNode);
		if(feature == null)
			return null;
		return feature.get("repPoint");
	}

	private JsonNode getFeature(JsonNode jsonNode) {
		JsonNode features = jsonNode.get("features");
		if(features == null)
			return null;
		return features.get(0);
	}

    public List<NamedLocation> toLocations(JsonNode geoJSONResponse) {
        List<NamedLocation> locations = new ArrayList<NamedLocation>();
        
        JsonNode features = geoJSONResponse.get("features");
        JsonNode currentFeature;
        int featureCount = features.size();
        NamedLocation location;

        for(int i = 0; i < featureCount; i++) {
            currentFeature = features.get(i);
            location = new NamedLocation();
            location.setLabel(getSpecificName(currentFeature));
            location.setAlsId(getID(currentFeature));
            location.setLocationTypeName(currentFeature.get("properties").get("locationTypeName").asText());
            locations.add(location);
        }
        
        return locations;
    }

    private ArrayNode getBbox(JsonNode jsonNode) {
    	JsonNode feature = getFeature(jsonNode);
    	if(feature == null)
    		return null;
        return (ArrayNode) feature.get(BBOX);
    }

    private Long getID(JsonNode jsonNode) {
        return jsonNode.get("properties").get("gid").asLong();
    }

    private String getSpecificName(JsonNode jsonNode) {
        ArrayList<String> names = new ArrayList<>();
        names.add(jsonNode.get("properties").get(NAME).asText());
        names.addAll(getLineageNames(jsonNode));

        return joinNames(names);
    }

    private ArrayList<String> getLineageNames(JsonNode jsonNode) {
        ArrayList<String> names = new ArrayList<>();
        ArrayNode parents = (ArrayNode)jsonNode.get("properties").get("lineage");
        
        for(int i = parents.size() - 1; i >= 0; i--) {
            names.add(parents.get(i).get(NAME).asText());
        }
        
        return names;
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

        if(parents != null)
        	for (int i = parents.size() - 1; i >= 0; i--)
        		names.add(parents.get(i).get("name").asText());

        return names;
    }

    private ArrayNode getParents(JsonNode jsonNode) {
        JsonNode properties = getProperties(jsonNode);
        if(properties == null)
        	return null;
		return (ArrayNode) properties.get("lineage");
    }

    private String getLocationName(JsonNode jsonNode) {
        JsonNode properties = getProperties(jsonNode);
        if (properties == null)
        	return null;
		return properties.get(NAME).asText();
    }

    private static JsonNode getProperties(JsonNode jsonNode) {
        JsonNode features = jsonNode.get("features");
        if (features == null)
        	return null;
		return features.get(0).get("properties");
    }

    private static Map<String, Double> toCentroid(ArrayNode bBox) {
    	if(bBox == null)
    		return null;
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
