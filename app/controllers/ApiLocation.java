package controllers;

import interactors.LocationRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.PathParam;

import models.entities.Location;
import models.filters.Filter;
import models.filters.LocationFilter;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Api(value = "/locations", description = "Endpoints for Locations")
public class ApiLocation extends Controller {
	@ApiOperation(httpMethod = "GET", nickname = "read", value = "Returns the Location by ID")
	@ApiResponses({ @ApiResponse(code = OK, message = "Success") })
	@Transactional
	public static Result read(
			@ApiParam(value = "ID of the location", required = true) 
			@PathParam("id") 
			long id) {
		Filter filter = null;
		Location result = makeLocationRule().read(id);
		return ResponseHelper.okAsWrappedJsonObject(result, filter);
	}

	@Transactional
	public static Result getBulkLables() {
		List<Long> ids = toIds((ArrayNode)request().body().asJson());
		LocationFilter filter = makeLocationFilter(ids);
		List<Location> locations = makeLocationRule().query(filter);
		Map<Long, String> result = toId2Label(locations);
		return ResponseHelper.okAsWrappedJsonObject(result, filter);
	}

	private static List<Long> toIds(ArrayNode array) {
		List<Long> ids = new ArrayList<>(array.size());
		for(JsonNode node: array){
			ids.add(node.asLong());
		}
		return ids;
	}

	private static Map<Long, String> toId2Label(List<Location> locations) {
		Map<Long, String> result = locations.stream()
                .collect(Collectors.toMap(Location::getId, Location::getLabel));
		return result;
	}

	private static LocationFilter makeLocationFilter(List<Long> ids) {
		LocationFilter filter = new LocationFilter();
		filter.setIds(ids);
		return filter;
	}

	private static LocationRule makeLocationRule() {
		return Factory.makeLocationRule(JPA.em());
	}
}