package controllers;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gateways.webservice.AlsDao;

import javax.ws.rs.PathParam;

import lsparser.xmlparser.ALSIDQueryInput;
import lsparser.xmlparser.TychoParser;
import models.entities.NamedLocation;
import models.exceptions.Unauthorized;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.wordnik.swagger.annotations.ApiParam;

import controllers.security.AuthorizationKit;
import controllers.security.Restricted;
import controllers.security.Restricted.Access;

public class APITychoSeries extends ApiSeries {
	@Transactional
	@Restricted({Access.CHANGE})
	public static Result updateDataViaUrl(
		@ApiParam(value = "ID of the Series", required = true)
		@PathParam("id") 
		long id,
		@ApiParam(value = "Force overWrite the content", required = false)
		@PathParam("overWrite") 
		boolean overWrite) {
			checkSeriesPermission(id, "upload data to");
			String url = seriesDataUrlForm.bindFromRequest().get().getUrl();
			
			return UploadSeries.uploadTychoViaUrl(id, url, overWrite);
	}
	
	public static Result findLocation() throws UnsupportedEncodingException, URISyntaxException {
		AlsDao alsDAO = new AlsDao();
		ALSIDQueryInput alsIDQueryInput = new ALSIDQueryInput();
		TychoParser.Request request = new TychoParser.Request(alsIDQueryInput);
		
		//get single location query details from JSON
		JsonNode input = request().body().asJson();
		alsIDQueryInput.locationName = input.get("label").asText();
		if(input.get("date") != null) {
			alsIDQueryInput.date = new Date(input.get("date").asLong());
		}
	
		Iterator<String> fieldNames = input.fieldNames();
		String field;
		while(fieldNames.hasNext()) {
			field = fieldNames.next();
			if((field != "label") && (field != "date")) {
				alsIDQueryInput.details.put(field, input.get(field).asText());
			}
System.out.println(field + ": " + input.get(field));
		}
		
		//fetch single location search results
		Map<String, List<NamedLocation>> possibleIdentities = new HashMap<String, List<NamedLocation>>();
		possibleIdentities.put(alsIDQueryInput.locationName, request.sendRequest(alsDAO));
		
		return Results.status(MULTIPLE_CHOICES, Json.toJson(possibleIdentities));
	}
	
	private static void checkSeriesPermission(long id, String action) {
		if (! AuthorizationKit.isSeriesPermitted(id))
			throw new Unauthorized("Unauthorized to " + action + 
					" the Series with ID = " + id);
		
		return;
	}
}
