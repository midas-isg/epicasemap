package controllers;

import static controllers.ResponseWrapper.okAsWrappedJsonObject;
import interactors.VizRule;

import javax.ws.rs.PathParam;

import models.entities.Viz;
import models.entities.filters.Filter;
import models.view.VizInput;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Api(value = "/vizs", description = "Endpoints for Vizs", hidden = false)
public class ApiViz extends Controller {
	private static final String ex = "vizs.json";
	private static final String exBody = "See an example of body at "
			+ "<a href='assets/examples/api/" + ex + "'>" + ex + "</a> ";
	public static final String type = "models.view.VizInput";

	public static Form<VizInput> vizForm = Form.form(VizInput.class);

	@ApiOperation(httpMethod = "POST", nickname = "create", value = "Creates a new Viz", 
		notes = "This endpoint creates a Viz using submitted JSON object in body "
		+ "and returns the URI via the 'Location' Header in the response. "
		+ "Currently, no content returns in the body. ")
	@ApiResponses({ 
		@ApiResponse(code = OK, message = "(Not used yet)"),
		@ApiResponse(code = CREATED, message = "Success") })
	@ApiImplicitParams({ 
		@ApiImplicitParam(required = true, value = exBody, dataType = type, paramType = "body")
	})
	@Transactional
	public static Result post() {
		VizInput data = vizForm.bindFromRequest().get();
		long id = create(data);
		setResponseLocationFromRequest(id + "");
		return created();
	}

	public static long create(VizInput data) {
		return makeRule().create(data);
	}

	@ApiOperation(httpMethod = "GET", nickname = "read", value = "Returns the Viz by ID")
	@ApiResponses({ @ApiResponse(code = OK, message = "Success") })
	@Transactional
	public static Result read(
			@ApiParam(value = "ID of the Viz", required = true) 
			@PathParam("id") 
			long id) {
		Viz data = makeRule().read(id);
		Filter filter = null;
		return okAsWrappedJsonObject(data, filter);
	}

	@ApiOperation(httpMethod = "PUT", nickname = "update", value = "Updates the Viz", 
		notes = "This endpoint does full update the given Viz "
		+ "idientified by 'id' with submitted JSON object in body "
		+ "and returns the URI via the 'Location' Header in the response. "
		+ "Currently, no content in the body. ")
	@ApiResponses({
		@ApiResponse(code = OK, message = "(Not used yet)"),
		@ApiResponse(code = NO_CONTENT, message = "Success") })
	@ApiImplicitParams({ 
		@ApiImplicitParam(required = true, value = exBody, dataType = type, paramType = "body") })
	@Transactional
	public static Result put(
			@ApiParam(value = "ID of the Viz", required = true) @PathParam("id") long id) {
		final VizInput input = vizForm.bindFromRequest().get();
		final Viz data = makeRule().toViz(input);
		update(id, data);
		setResponseLocationFromRequest();
		return noContent();
	}

	public static void update(long id, Viz data) {
		makeRule().update(id, data);
	}

	@ApiOperation(httpMethod = "DELETE", nickname = "delete", value = "Deletes the Viz", 
		notes = "This endpoint deletes the given Viz idientified by 'id' "
		+ "and returns the URI via the 'Location' Header in the response. "
		+ "Currently, no content in the body. ")
	@ApiResponses({ 
		@ApiResponse(code = OK, message = "(Not used yet)"),
		@ApiResponse(code = NO_CONTENT, message = "Success") })
	@Transactional
	public static Result delete(
			@ApiParam(value = "ID of the Viz", required = true) 
			@PathParam("id") 
			long id) {
		deleteById(id);
		setResponseLocationFromRequest();
		return noContent();
	}

	public static void deleteById(long id) {
		makeRule().delete(id);
	}

	private static void setResponseLocationFromRequest(String... tails) {
		String url = makeUriFromRequest();
		for (String tail : tails)
			url += "/" + tail;
		response().setHeader(LOCATION, url);
	}

	private static String makeUriFromRequest() {
		Request request = Context.current().request();
		return request.getHeader(ORIGIN) + request.path();
	}
	
	private static VizRule makeRule() {
		return Factory.makeVizRule(JPA.em());
	}

	public static VizInput from(Viz data) {
		return makeRule().fromViz(data);
	}
}