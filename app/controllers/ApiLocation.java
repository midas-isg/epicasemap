package controllers;

import javax.ws.rs.PathParam;

import models.entities.Location;
import models.entities.filters.Filter;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

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
		Location result = JPA.em().find(Location.class, id);
		return ResponseWrapper.okAsWrappedJsonObject(result, filter);
	}
}