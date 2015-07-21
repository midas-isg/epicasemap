package controllers;

import static controllers.ResponseHelper.setResponseLocationFromRequest;
import interactors.SeriesRule;

import java.util.List;

import javax.ws.rs.PathParam;

import models.entities.Series;
import models.entities.filters.Filter;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Api(value = "/series", description = "Endpoints for Series")
public class ApiSeries extends Controller {
	
	private static final String ex = "series.json";
	private static final String exBody = "See an example of body at "
			+ "<a href='assets/examples/api/" + ex + "'>" + ex + "</a> ";
	public static final String inputType = "models.entities.Series";
	
	public static Form<Series> seriesForm = Form.form(Series.class);

	@ApiOperation(httpMethod = "POST", nickname = "create", value = "Creates a new Series", 
			notes = "This endpoint creates a Series using submitted JSON object in body "
			+ "and returns the URI via the 'Location' Header in the response. "
			+ "Currently, no content returns in the body. ")
	@ApiResponses({ 
			@ApiResponse(code = OK, message = "(Not used yet)"),
			@ApiResponse(code = CREATED, message = "Success") })
	@ApiImplicitParams({ 
	@ApiImplicitParam(required = true, value = exBody, dataType = inputType, paramType = "body")
		})
	@Transactional
	public static Result post() {
		Series data = seriesForm.bindFromRequest().get();
		long id = create(data);
		setResponseLocationFromRequest(id + "");
		return created();
	}
	
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
	
	@ApiOperation(httpMethod = "PUT", nickname = "update", value = "Updates the Series", 
			notes = "This endpoint does full update the given Series "
			+ "idientified by 'id' with submitted JSON object in body "
			+ "and returns the URI via the 'Location' Header in the response. "
			+ "Currently, no content in the body. ")
	@ApiResponses({
			@ApiResponse(code = OK, message = "(Not used yet)"),
			@ApiResponse(code = NO_CONTENT, message = "Success") })
	@ApiImplicitParams({ 
	@ApiImplicitParam(required = true, value = exBody, dataType = inputType, paramType = "body") })
	@Transactional
	public static Result put(
		@ApiParam(value = "ID of the Series", required = true) @PathParam("id") long id) {
		final Series data = seriesForm.bindFromRequest().get();
		makeRule().update(id, data);
		setResponseLocationFromRequest();
		return noContent();
	}
	
	@ApiOperation(httpMethod = "DELETE", nickname = "delete", value = "Deletes the Series", 
			notes = "This endpoint deletes the given Series idientified by 'id' "
			+ "and returns the URI via the 'Location' Header in the response. "
			+ "Currently, no content in the body. ")
	@ApiResponses({ 
			@ApiResponse(code = OK, message = "(Not used yet)"),
			@ApiResponse(code = NO_CONTENT, message = "Success") })
	@Transactional
	public static Result delete(
			@ApiParam(value = "ID of the Series", required = true) 
			@PathParam("id") 
				long id) {
		deleteById(id);
		setResponseLocationFromRequest();
		return noContent();
	}

	public static void deleteById(long id) {
		makeRule().delete(id);
	}

	public static SeriesRule makeRule() {
		return Factory.makeSeriesRule(JPA.em());
	}

	public static long create(Series data) {
		return makeRule().create(data);
	}
	
}
