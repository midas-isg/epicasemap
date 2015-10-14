package controllers;

import static controllers.ResponseHelper.setResponseLocationFromRequest;
import interactors.AuthorizationRule;
import interactors.SeriesRule;

import java.util.List;

import javax.ws.rs.PathParam;

import models.entities.Mode;
import models.entities.SeriesPermission;
import models.entities.Series;
import models.exceptions.Unauthorized;
import models.filters.Filter;
import models.filters.Restriction;
import models.filters.SeriesFilter;
import models.view.ModeWithAccountId;
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

import controllers.security.AuthorizationKit;
import controllers.security.Restricted;
import controllers.security.Restricted.Access;
@Api(value = "/series", description = "Endpoints for Series")
public class ApiSeries extends Controller {
	private static final String ex = "series.json";
	private static final String exBody = "See an example of body at "
			+ "<a href='assets/examples/api/" + ex + "'>" + ex + "</a> ";
	public static final String inputType = "models.entities.Series";
	
	public static Form<Series> seriesForm = Form.form(Series.class);
	public static Form<ModeWithAccountId> modeForm = Form.form(ModeWithAccountId.class);

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
	@Restricted({Access.USE, Access.READ, Access.CHANGE})
	public static Result get() {
		List<Long> ids = AuthorizationKit.findPermittedSeriesIds();
		SeriesFilter seriesfilter = new SeriesFilter();
		seriesfilter.setIds(ids);
		List<Series> results = find(seriesfilter);
		return ResponseHelper.okAsWrappedJsonArray(results, seriesfilter);
	}

	@Transactional
	public static List<Series> find(SeriesFilter filter) {
		return makeRule().query(filter);
	}

	@ApiOperation(httpMethod = "GET", nickname = "read", value = "Returns the Series by ID")
	@ApiResponses({ 
		@ApiResponse(code = OK, message = "Success"), 
		@ApiResponse(code = NOT_FOUND, message = "Not found"),
		@ApiResponse(code = UNAUTHORIZED, message = "Access denied") })
	@Transactional
	@Restricted({Access.USE, Access.READ, Access.CHANGE})
	public static Result read(
			@ApiParam(value = "ID of the series", required = true)
			@PathParam("id")
			long id) {
		checkSeriesPermission(id, "read");
		Series result = makeRule().read(id);
		Filter filter = null;
		return ResponseHelper.okAsWrappedJsonObject(result, filter);
	}

	private static void checkSeriesPermission(long id, String action) {
		if (! AuthorizationKit.isSeriesPermitted(id))
			throw new Unauthorized("Unauthorized to " + action + " the Series with ID = " + id);
	}
	
	@ApiOperation(httpMethod = "PUT", nickname = "update", value = "Updates the Series", 
			notes = "This endpoint does full update the given Series "
			+ "idientified by 'id' with submitted JSON object in body "
			+ "and returns the URI via the 'Location' Header in the response. "
			+ "Currently, no content in the body. ")
	@ApiResponses({
			@ApiResponse(code = OK, message = "(Not used yet)"),
			@ApiResponse(code = NO_CONTENT, message = "Success"), 
			@ApiResponse(code = NOT_FOUND, message = "Not found"),
			@ApiResponse(code = UNAUTHORIZED, message = "Access denied") })
	@ApiImplicitParams({ 
	@ApiImplicitParam(required = true, value = exBody, dataType = inputType, paramType = "body") })
	@Transactional
	@Restricted({Access.CHANGE})
	public static Result put(
		@ApiParam(value = "ID of the Series", required = true) @PathParam("id") 
				long id) {
		checkSeriesPermission(id, "update");
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
			@ApiResponse(code = NO_CONTENT, message = "Success"), 
			@ApiResponse(code = NOT_FOUND, message = "Not found"),
			@ApiResponse(code = UNAUTHORIZED, message = "Access denied") })
	@Transactional
	@Restricted({Access.CHANGE})
	public static Result delete(
			@ApiParam(value = "ID of the Series", required = true) 
			@PathParam("id") 
				long id) {
		checkSeriesPermission(id, "delete");
		deleteById(id);
		setResponseLocationFromRequest();
		return noContent();
	}
	
	@ApiOperation(httpMethod = "PUT", nickname = "upload", value = "Uploads Series Data", 
			notes = "This endpoint uploads data for the given Series idientified by 'id' "
					+ "and returns the upload status in response header. "
					+ "(returns more information in response body.)")
			@ApiResponses({ 
					@ApiResponse(code = CREATED, message = "Success"),
					@ApiResponse(code = NOT_FOUND, message = "Not found"),
					@ApiResponse(code = UNAUTHORIZED, message = "Access denied") })
	@Transactional
	@Restricted({Access.CHANGE})
	public static Result uploadData(
			@ApiParam(value = "ID of the Series", required = true) 
			@PathParam("id") 
				long id) {
		checkSeriesPermission(id, "upload data to");
		return UploadSeries.upload(id);
	}
	
	@Transactional
	@Restricted({Access.CHANGE})
	public static Result updateDataViaUrl(
			@ApiParam(value = "ID of the Series", required = true) 
			@PathParam("id") 
				long id,
			@ApiParam(value = "Url of the Series Data", required = true) 
			@PathParam("url") 
				String url) {
		checkSeriesPermission(id, "upload data to");
		return UploadSeries.uploadViaUrl(id,url);
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
	
	@Transactional
	@Restricted({Access.READ, Access.CHANGE})
	public static Result getData(
			Long seriesId,
			String startInclusive,
			String endExclusive,
			Integer limit,
			int offset) {
		checkSeriesPermission(seriesId, "read the data of");
		return ApiTimeCoordinateSeries.get(seriesId,
				startInclusive,
				endExclusive,
				limit,
				offset);
	}

	@Transactional
	@Restricted({Access.PERMIT})
	public static Result getPermissions(long id) {
		checkSeriesPermission(id, "see the permissions of");
		Restriction r = new Restriction(null, null, id);
		List<?> results = makeAuthorizationRule().findPermissions(r);
		return ResponseHelper.okAsWrappedJsonArray(results, null);
	}

	private static AuthorizationRule makeAuthorizationRule() {
		return Factory.makeAuthorizationRule(JPA.em());
	}
	
	@Transactional
	@Restricted({Access.PERMIT})
	public static Result postPermissions(long seriesId) {
		checkSeriesPermission(seriesId, "create the permission of");
		ModeWithAccountId data = modeForm.bindFromRequest().get();
		final List<Long> accountIds = data.getAccountIds();
		for (Long accountId : accountIds)
			makeAuthorizationRule().grantSeries(accountId, data, seriesId);
		return created();
	}
	
	@Transactional
	@Restricted({Access.PERMIT})
	public static Result deletePermission(long id) {
		final AuthorizationRule authorizationRule = makeAuthorizationRule();
		final Long sId = findSeriesIdByPermissionId(authorizationRule, id);
		checkSeriesPermission(sId, "delete the permission of");
		authorizationRule.delete(id);
		setResponseLocationFromRequest();
		return noContent();
	}

	private static Long findSeriesIdByPermissionId(
			final AuthorizationRule authorizationRule, long id) {
		final SeriesPermission permission = authorizationRule.read(id);
		final Long sId = permission.getSeries().getId();
		return sId;
	}
	
	@Transactional
	@Restricted({Access.PERMIT})
	public static Result putMode(long id) {
		final AuthorizationRule authorizationRule = makeAuthorizationRule();
		final Long sId = findSeriesIdByPermissionId(authorizationRule, id);
		checkSeriesPermission(sId, "update the permission of");
		Mode data = modeForm.bindFromRequest().get();
		authorizationRule.updateMode(id, data);
		setResponseLocationFromRequest();
		return noContent();
	}
}