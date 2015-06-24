package controllers;

import interactors.SeriesRule;

import java.util.List;

import javax.ws.rs.PathParam;

import models.entities.Series;
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

@Api(value = "/series", description = "Endpoint for series")
public class ApiSeries extends Controller {
	@ApiOperation(
			httpMethod = "GET", 
			nickname = "find", 
			value = "Returns all series" 
	)
	@ApiResponses(value = {
			@ApiResponse(code = OK, message = "Successful retrieval"),
	})
	@Transactional
	public static Result get(){
		Filter filter = null;
		SeriesRule rule = Factory.makeSeriesRule(JPA.em());
		List<Series> results = rule.query(filter);;
		return ResponseWrapper.okAsWrappedJsonArray(results, filter);
	}
	
	@ApiOperation(
			httpMethod = "GET", 
			nickname = "read", 
			value = "Returns a series by ID" 
	)
	@ApiResponses(value = {
			@ApiResponse(code = OK, message = "Successful retrieval"),
	})
	@Transactional
	public static Result read(
			@ApiParam(value = "ID of the series", required = true) 
			@PathParam("id") 
			long id){
		Filter filter = null;
		SeriesRule rule = Factory.makeSeriesRule(JPA.em());
		Series result = rule.read(id, filter);;
		return ResponseWrapper.okAsWrappedJsonObject(result, filter);
	}
}
