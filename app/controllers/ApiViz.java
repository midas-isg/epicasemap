package controllers;

import static controllers.ResponseHelper.okAsWrappedJsonObject;
import static controllers.ResponseHelper.setResponseLocationFromRequest;
import static controllers.security.AuthorizationKit.findPermittedSeriesIds;
import interactors.VizAuthorizer;
import interactors.VizRule;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.PathParam;

import models.entities.Mode;
import models.entities.Series;
import models.entities.Visualization;
import models.entities.VizPermission;
import models.exceptions.NotFound;
import models.exceptions.Unauthorized;
import models.filters.Filter;
import models.filters.MetaFilter;
import models.filters.Restriction;
import models.view.ModeWithAccountId;
import models.view.VizInput;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.mvc.Security;

import com.fasterxml.jackson.databind.JsonNode;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import controllers.security.Authentication;
import controllers.security.AuthorizationKit;
import controllers.security.Restricted;
import controllers.security.Restricted.Access;

@Api(value = "/vizs", description = "Endpoints for Vizs", hidden = false)
public class ApiViz extends Controller {
	private static final String ex = "vizs.json";
	private static final String exBody = "See an example of body at "
			+ "<a href='assets/examples/api/" + ex + "'>" + ex + "</a> ";
	public static final String inputType = "models.view.VizInput";

	public static Form<VizInput> vizForm = Form.form(VizInput.class);
	public static Form<ModeWithAccountId> modeForm = Form.form(ModeWithAccountId.class);

	@ApiOperation(httpMethod = "POST", nickname = "create", value = "Creates a new Viz", 
		notes = "This endpoint creates a Viz using submitted JSON object in body "
		+ "and returns the URI via the 'Location' Header in the response. "
		+ "Currently, no content returns in the body. ")
	@ApiResponses({ 
		@ApiResponse(code = OK, message = "(Not used yet)"),
		@ApiResponse(code = CREATED, message = "Success") })
	@ApiImplicitParams({ 
		@ApiImplicitParam(required = true, value = exBody, dataType = inputType, paramType = "body")
	})
	@Transactional
	@Security.Authenticated(Authentication.class)
	public static Result post() {
		VizInput data = vizForm.bindFromRequest().get();
		final Long accountId = AuthorizationKit.readAccountId();
		data.setOwnerId(accountId);
		long id = create(data);
		setResponseLocationFromRequest(id + "");
		return created();
	}

	public static Long create(VizInput data) {
		checkSeriesUsePermission(data.getOwnerId(), data.getSeriesIds());
		return makeRule().createFromInput(data); 
	}

	private static void checkSeriesUsePermission(Long accountId,
			final List<Long> seriesIds) {
		if (seriesIds == null || accountId == null)
			return;
		final Access access = Access.USE;
		final List<Long> permittedSeriesIds = findPermittedSeriesIds(
				accountId, Arrays.asList(access));
		if (! permittedSeriesIds.containsAll(seriesIds)){
			seriesIds.removeAll(permittedSeriesIds);
			throw new Unauthorized("Unauthorized to use the Series: " + seriesIds 
			+ ". '" + access + "' Access to the Series is required.");
		}
	}

	@ApiOperation(httpMethod = "GET", nickname = "read", value = "Returns the Viz by ID")
	@ApiResponses({ @ApiResponse(code = OK, message = "Success") })
	@Transactional
	public static Result read(
			@ApiParam(value = "ID of the Viz", required = true) 
			@PathParam("id") 
			long id) {
		Visualization data = makeRule().read(id);
		Filter filter = null;
		return okAsWrappedJsonObject(data, filter);
	}

	@ApiOperation(httpMethod = "GET", nickname = "list", value = "Lists all Vizs")
	@ApiResponses({ @ApiResponse(code = OK, message = "Success") })
	@Transactional
	public static Result list() {
		MetaFilter filter = new MetaFilter();
		List<Visualization> results = makeRule().query(filter);
		return ResponseHelper.okAsWrappedJsonArray(results, filter);

	}

	@ApiOperation(httpMethod = "GET", nickname = "readUiSetting", value = "Returns the UI Setting of the Viz by ID")
	@ApiResponses({ @ApiResponse(code = OK, message = "Success") ,
					@ApiResponse(code = NOT_FOUND, message = "Viz with the ID not found") })
	@Transactional
	public static Result readUiSetting(
			@ApiParam(value = "ID of the Viz", required = true) 
			@PathParam("id") 
			long id) {
		Visualization data = makeRule().read(id);
		if (data == null)
			return notFound("Viz with ID=" + id + " not found!");
		String uiSetting = data.getUiSetting();
		return ok(String.valueOf(uiSetting));
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
		@ApiImplicitParam(required = true, value = exBody, dataType = inputType, paramType = "body") })
	@Transactional
	@Restricted({Access.CHANGE})
	public static Result put(
			@ApiParam(value = "ID of the Viz", required = true) @PathParam("id") long id) {
		checkVizPermission(id, "update");
		
		final VizInput input = vizForm.bindFromRequest().get();
		update(id, input);
		setResponseLocationFromRequest();
		return noContent();
	}
	
	public static Visualization update(long id, final VizInput input) {
		final VizRule rule = makeRule();
		final Visualization viz = rule.read(id);
		final List<Long> permittedSeriesIds = viz.getAllSeries().stream()
				.map(s -> s.getId())
				.collect(Collectors.toList());
		final List<Long> seriesIds = input.getSeriesIds();
		if (seriesIds != null && ! seriesIds.isEmpty()){
			final List<Long> remainingIds = seriesIds.stream()
				.filter(sId-> ! permittedSeriesIds.contains(sId))
				.collect(Collectors.toList());
			final Long accountId = AuthorizationKit.readAccountId();
			checkSeriesUsePermission(accountId, remainingIds);
		}
		return rule.updateFromInput(id, input);
	}

	private static void checkVizPermission(long id, String action) {
		if (! AuthorizationKit.isVizPermitted(id))
			throw new Unauthorized("Unauthorized to " + action + " the Visualization with ID = " + id);
	}

	@ApiOperation(httpMethod = "PUT", nickname = "updateUiSetting", value = "Updates UI Setting", 
			notes = "This endpoint does full update only the given UI Setting "
			+ "of the ViZ idientified by 'id' with submitted JSON object in body "
			+ "and returns the URI via the 'Location' Header in the response. "
			+ "Currently, no content in the body. ")
		@ApiResponses({
			@ApiResponse(code = OK, message = "(Not used yet)"),
			@ApiResponse(code = NO_CONTENT, message = "Success") })
		@ApiImplicitParams({ 
			@ApiImplicitParam(required = true, paramType = "body") })
		@Transactional
		@Restricted({Access.CHANGE})
	public static Result putUiSetting(
			@ApiParam(value = "ID of the Viz", required = true) @PathParam("id") long id) {
		checkVizPermission(id, "update UI setting of");
		RequestBody body = request().body();
		final JsonNode root = body.asJson();
		String json = root.toString();
		makeRule().updateUiSetting(id, json);
		setResponseLocationFromRequest();
		return noContent();
	}
	
	@ApiOperation(httpMethod = "DELETE", nickname = "delete", value = "Deletes the Viz", 
		notes = "This endpoint deletes the given Viz idientified by 'id' "
		+ "and returns the URI via the 'Location' Header in the response. "
		+ "Currently, no content in the body. ")
	@ApiResponses({ 
		@ApiResponse(code = OK, message = "(Not used yet)"),
		@ApiResponse(code = NO_CONTENT, message = "Success") })
	@Transactional
	@Restricted({Access.CHANGE})
	public static Result delete(
			@ApiParam(value = "ID of the Viz", required = true) 
			@PathParam("id") 
			long id) {
		checkVizPermission(id, "delete");
		deleteById(id);
		setResponseLocationFromRequest();
		return noContent();
	}

	public static void deleteById(long id) {
		makeRule().delete(id);
	}

	private static VizRule makeRule() {
		return Factory.makeVizRule(JPA.em());
	}

	public static VizInput from(Visualization data) {
		return makeRule().fromViz(data);
	}

	@Transactional
	@Restricted({Access.PERMIT})
	public static Result getPermissions(long id) {
		checkVizPermission(id, "see the permissions of");
		Restriction r = new Restriction(null, null, null, id);
		List<?> results = makeVizAuthorizer().findPermissions(r);
		return ResponseHelper.okAsWrappedJsonArray(results, null);
	}

	@Transactional
	@Restricted({Access.PERMIT})
	public static Result postPermissions(long vizId) {
		checkVizPermission(vizId, "create the permission of");
		ModeWithAccountId data = modeForm.bindFromRequest().get();
		final List<Long> accountIds = data.getAccountIds();
		final VizAuthorizer authorizer = makeVizAuthorizer();
		for (Long accountId : accountIds) 
			authorizer.permit(accountId, data, vizId);
		
		return created();
	}
	
	@Transactional
	@Restricted({Access.PERMIT})
	public static Result deletePermission(long id) {
		final VizAuthorizer authorizationRule = makeVizAuthorizer();
		final Long sId = findVizIdByPermissionId(authorizationRule, id);
		checkVizPermission(sId, "delete the permission of");
		authorizationRule.delete(id);
		setResponseLocationFromRequest();
		return noContent();
	}

	private static Long findVizIdByPermissionId(
			final VizAuthorizer authorizationRule, long id) {
		final VizPermission permission = authorizationRule.read(id);
		return permission.getViz().getId();
	}
	
	@Transactional
	@Restricted({Access.PERMIT})
	public static Result putMode(long id) {
		final VizAuthorizer authorizationRule = makeVizAuthorizer();
		final Long sId = findVizIdByPermissionId(authorizationRule, id);
		checkVizPermission(sId, "update the permission of");
		Mode data = modeForm.bindFromRequest().get();
		authorizationRule.updateMode(id, data);
		setResponseLocationFromRequest();
		return noContent();
	}
	
	
	@Transactional
	@Restricted({Access.USE})
	public static Result getSeriesData(
			Long id,
			Long seriesId,
			String startInclusive,
			String endExclusive,
			Integer limit,
			int offset) {
		checkVizPermission(id, "run");
		final Visualization viz = makeRule().read(id);
		final List<Series> allSeries = viz.getAllSeries();
		boolean seriesIsInViz = allSeries.stream()
				.anyMatch(series ->series.getId().equals(seriesId));
		if (! seriesIsInViz)
			throw new NotFound(toTextIsNotIn(seriesId, id));
		return ApiTimeCoordinateSeries.get(seriesId,
				startInclusive,
				endExclusive,
				limit,
				offset);
	}

	private static String toTextIsNotIn(Long seriesId, Long id) {
		return "Series with ID = " + seriesId 
				+ " is not in the Visualization with ID = " + id;
	}

	private static VizAuthorizer makeVizAuthorizer() {
		return Factory.makeVizAuthorizer(JPA.em());
	}
}