package controllers;

import interactors.ClientRule;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gateways.webservice.AlsDao;

import javax.ws.rs.PathParam;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import lsparser.xmlparser.ALSIDQueryInput;
import lsparser.xmlparser.TychoParser;
import models.entities.NamedLocation;
import models.exceptions.Unauthorized;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.*;
import play.mvc.Http.*;
import play.Logger;

import com.fasterxml.jackson.databind.JsonNode;
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
	
	@Transactional
	@Restricted({Access.CHANGE})
	@BodyParser.Of(value = BodyParser.Json.class, maxLength = 10485760)
	public static Result saveData(
		@ApiParam(value = "ID of the Series", required = true)
		@PathParam("id") 
		long id,
		@ApiParam(value = "Force overWrite the content", required = false)
		@PathParam("overWrite")
		boolean overWrite) {
			checkSeriesPermission(id, "upload data to");
			JsonNode jsonMappings = request().body().asJson();
			
			return UploadSeries.uploadTychoJSON(id, jsonMappings, overWrite);
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
//Logger.debug(field + ": " + input.get(field));
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
	
	public static Result getTychoJSON(String type) {
		String url = "http://www.tycho.pitt.edu/api/states?apikey=9a4c75183895f07e7776";
		List<String> resultArray;
		ClientRule clientRule;
		Document document;
		NodeList elements;
		
		switch(type) {
			case "cities":
				url = "http://www.tycho.pitt.edu/api/cities?apikey=9a4c75183895f07e7776";
				clientRule = new ClientRule(url);
				document = clientRule.get(url).asXml();
				elements = document.getElementsByTagName("loc");
				
				resultArray = new ArrayList<String>();
				for(int i = 0, length = elements.getLength(); i < length; i++) {
					resultArray.add(elements.item(i).getTextContent());
				}
			break;
			
			case "diseases":
				url = "http://www.tycho.pitt.edu/api/diseases?apikey=9a4c75183895f07e7776";
				clientRule = new ClientRule(url);
				document = clientRule.get(url).asXml();
				elements = document.getElementsByTagName("disease");
				
				resultArray = new ArrayList<String>();
				for(int i = 0, length = elements.getLength(); i < length; i++) {
					resultArray.add(elements.item(i).getTextContent());
				}
			break;
			
			case "states":
				url = "http://www.tycho.pitt.edu/api/states?apikey=9a4c75183895f07e7776";
				clientRule = new ClientRule(url);
				document = clientRule.get(url).asXml();
				elements = document.getElementsByTagName("state");
				
				resultArray = new ArrayList<String>();
				for(int i = 0, length = elements.getLength(); i < length; i++) {
					resultArray.add(elements.item(i).getTextContent());
				}
			break;
			
			default:
				return Results.badRequest();
		}
		
		return Results.ok(Json.toJson(resultArray));
	}
}
