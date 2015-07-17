package controllers;

import static controllers.ResponseWrapper.okAsWrappedJsonObject;

import javax.persistence.EntityManager;
import javax.ws.rs.PathParam;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import models.entities.Viz;
import models.entities.VizInput;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;

@Api(value = "/vizs", description = "Endpoint for Vizs")
public class ApiViz extends Controller {
	private static final String ex = "vizs.json";
	private static final String exBody = "See an example of body at "
			+ "<a href='assets/examples/api/" + ex + "'>"+ ex + "</a> ";
	public static final String type = "models.entities.VizInput";
	
	public static Form<VizInput> vizForm = Form.form(VizInput.class);
	
	@ApiOperation(
		httpMethod = "POST", 
		nickname = "create",
		notes = "This endpoint creates a Viz using submitted JSON object in body "
		+ "and returns the URI via the 'Location' Header in the response. "
		+ "Currently, no content returns in the body. ",
		value = "Creates a new Viz"
	)
	@ApiResponses(value = {
		@ApiResponse(code = CREATED, message = "Successful creation")
	})
	@ApiImplicitParams({
		@ApiImplicitParam(
			required = true,
			value = exBody,
			dataType = type,
			paramType = "body"
		) 
	})
	@Transactional
	public static Result post() {
		VizInput data = vizForm.bindFromRequest().get();
		long id = create(data);
		setResponseLocationFromRequest(id + "");
		return created();
	}

	public static long create(VizInput input) {
		final EntityManager em = JPA.em();
		final Viz data = input.toViz();
		em.persist(data);
		return data.getId();
	}

	@ApiOperation(
		httpMethod = "GET", 
		nickname = "read", 
		value = "Returns the Viz by ID"
	)
	@ApiResponses(value = {
		@ApiResponse(code = OK, message = "Successful retrieval")
	})
	@Transactional
	public static Result read(
			@ApiParam(value = "ID of the Viz", required = true) 
			@PathParam("id") 
			long id
	) {
		Viz data = JPA.em().find(Viz.class, id);
		return okAsWrappedJsonObject(data, null);
	}
	
	@ApiOperation(
		httpMethod = "PUT", 
		nickname = "update",
		notes = "This endpoint does full update the given Viz "
		+ "idientified by 'id' with submitted JSON object in body "
		+ "and returns the URI via the 'Location' Header in the response. "
		+ "Currently, no content in the body. ",
		value = "Updates the Viz"
	)
	@ApiResponses(value = {
		@ApiResponse(code = NO_CONTENT, message = "The Viz updated")
	})
	@ApiImplicitParams({
		@ApiImplicitParam(
			required = true,
			value = exBody,
			dataType = type,
			paramType = "body"
		) 
	})
	@Transactional
	public static Result put(
			@ApiParam(value = "ID of the Viz", required = true) 
			@PathParam("id") 
			long id
	) {
		final VizInput input = vizForm.bindFromRequest().get();
		final Viz data = input.toViz();
		update(id, data);
		setResponseLocationFromRequest();
		return noContent();
	}

	public static void update(long id, Viz data) {
		final EntityManager em = JPA.em();
		Viz original = em.find(Viz.class, id);
		data.setId(original.getId());
		em.merge(data);
	}
	
	@ApiOperation(
			httpMethod = "DELETE", 
			nickname = "delete",
			notes = "This endpoint deletes the given Viz idientified by 'id' "
			+ "and returns the URI via the 'Location' Header in the response. "
			+ "Currently, no content in the body. ",
			value = "Deletes the Viz"
	)
	@ApiResponses(value = {
		@ApiResponse(code = NO_CONTENT, message = "The Viz updated")
	})
	@Transactional
	public static Result delete(
			@ApiParam(value = "ID of the Viz", required = true)
			@PathParam("id")
			long id
	) {
		deleteById(id);
		setResponseLocationFromRequest();
		return noContent();
	}

	public static void deleteById(long id) {
		final EntityManager em = JPA.em();
		Viz data = em.find(Viz.class, id);
		em.remove(data);
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
}