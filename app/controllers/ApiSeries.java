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

@Api(value = "/series", description = "Endpoints for Series")
public class ApiSeries extends Controller {
	@ApiOperation(httpMethod = "GET", nickname = "list", value = "Lists all Series")
	@ApiResponses({ @ApiResponse(code = OK, message = "Success") })
	@Transactional
	public static Result get() {
		Filter filter = null;
		List<Series> results = find(filter);
		return ResponseHelper.okAsWrappedJsonArray(results, filter);
	}

	@Transactional
	public static List<Series> find(Filter filter) {
		return makeRule().query(filter);
	}

	@ApiOperation(httpMethod = "GET", nickname = "read", value = "Returns the Series by ID")
	@ApiResponses({ @ApiResponse(code = OK, message = "Success") })
	@Transactional
	public static Result read(
			@ApiParam(value = "ID of the series", required = true)
			@PathParam("id")
			long id) {
		Series result = makeRule().read(id);
		Filter filter = null;
		return ResponseHelper.okAsWrappedJsonObject(result, filter);
	}

	public static SeriesRule makeRule() {
		return Factory.makeSeriesRule(JPA.em());
	}
}
