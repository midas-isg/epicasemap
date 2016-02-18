package controllers;

import static controllers.ResponseHelper.setResponseLocationFromRequest;
import interactors.SeriesAuthorizer;
import interactors.SeriesRule;

import java.util.List;

import javax.ws.rs.PathParam;

import models.entities.Mode;
import models.entities.Series;
import models.entities.SeriesDataUrl;
import models.entities.SeriesPermission;
import models.exceptions.Unauthorized;
import models.filters.Filter;
import models.filters.MetaFilter;
import models.filters.Restriction;
import models.view.ModeWithAccountId;
import models.view.SeriesInput;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

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

@Api(value = "/series", description = "Endpoints for Series")
public class ApiSeries extends Controller {
	private static final String ex = "series.json";
	private static final String exBody = "See an example of body at "
			+ "<a href='assets/examples/api/" + ex + "'>" + ex + "</a> ";
	public static final String inputType = "models.entities.Series";
	
	public static Form<SeriesInput> seriesForm = Form.form(SeriesInput.class);
	public static Form<ModeWithAccountId> modeForm = Form.form(ModeWithAccountId.class);
	public static Form<SeriesDataUrl> seriesDataUrlForm = Form.form(SeriesDataUrl.class);

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
	@Security.Authenticated(Authentication.class)
	public static Result post() {
		SeriesInput data = seriesForm.bindFromRequest().get();
		final Long accountId = AuthorizationKit.readAccountId();
		data.setOwnerId(accountId);
		long id = makeRule().createFromInput(data);
		setResponseLocationFromRequest(id + "");
		return created();
	}
	
	@ApiOperation(httpMethod = "GET", nickname = "list", value = "Lists all Series")
	@ApiResponses({ @ApiResponse(code = OK, message = "Success") })
	@Transactional
	public static Result list() {
		MetaFilter seriesfilter = new MetaFilter();
		List<Series> results = find(seriesfilter);
		return ResponseHelper.okAsWrappedJsonArray(results, seriesfilter);
	}

	@Transactional
	public static List<Series> find(MetaFilter filter) {
		return makeRule().query(filter);
	}

	@ApiOperation(httpMethod = "GET", nickname = "read", value = "Returns the Series by ID")
	@ApiResponses({ 
		@ApiResponse(code = OK, message = "Success"), 
		@ApiResponse(code = NOT_FOUND, message = "Not found"),
		@ApiResponse(code = UNAUTHORIZED, message = "Access denied") })
	@Transactional
	public static Result read(
			@ApiParam(value = "ID of the series", required = true)
			@PathParam("id")
			long id) {
		Series result = makeRule().read(id);
		Filter filter = null;
		return ResponseHelper.okAsWrappedJsonObject(result, filter);
	}

	private static void checkSeriesPermission(long id, String action) {
		if (! AuthorizationKit.isSeriesPermitted(id))
			throw new Unauthorized("Unauthorized to " + action + 
					" the Series with ID = " + id);
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
		final SeriesInput data = seriesForm.bindFromRequest().get();
		makeRule().updateFromInput(id, data);
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
		makeRule().delete(id);
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
			@ApiParam(value = "Force overWrite the content", required = false) 
			@PathParam("overWrite") 
				boolean overWrite) {
		checkSeriesPermission(id, "upload data to");
		String url = seriesDataUrlForm.bindFromRequest().get().getUrl();
		return UploadSeries.uploadViaUrl(id, url, overWrite);
	}

	private static SeriesRule makeRule() {
		return Factory.makeSeriesRule(JPA.em());
	}

	@Transactional
	@Restricted({Access.READ_DATA, Access.CHANGE})
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
	public static Result getPermissions(long seriesId) {
		checkSeriesPermission(seriesId, "see the permissions of");
		Restriction r = new Restriction(null, null, seriesId, null);
		List<?> results = makeSeriesAuthorizer().findPermissions(r);
		return ResponseHelper.okAsWrappedJsonArray(results, null);
	}

	private static SeriesAuthorizer makeSeriesAuthorizer() {
		return Factory.makeSeriesAuthorizer(JPA.em());
	}
	
	@Transactional
	@Restricted({Access.PERMIT})
	public static Result postPermissions(long seriesId) {
		checkSeriesPermission(seriesId, "create the permission of");
		ModeWithAccountId data = modeForm.bindFromRequest().get();
		final List<Long> accountIds = data.getAccountIds();
		final SeriesAuthorizer authorizer = makeSeriesAuthorizer();
		for (Long accountId : accountIds) 
			authorizer.permit(accountId, data, seriesId);

		return created();
	}
	
	@Transactional
	@Restricted({Access.PERMIT})
	public static Result deletePermission(long id) {
		final SeriesAuthorizer authorizationRule = makeSeriesAuthorizer();
		final Long sId = findSeriesIdByPermissionId(authorizationRule, id);
		checkSeriesPermission(sId, "delete the permission of");
		authorizationRule.delete(id);
		setResponseLocationFromRequest();
		return noContent();
	}

	private static Long findSeriesIdByPermissionId(
			final SeriesAuthorizer authorizationRule, long id) {
		final SeriesPermission permission = authorizationRule.read(id);
		return permission.getSeries().getId();
	}
	
	@Transactional
	@Restricted({Access.PERMIT})
	public static Result putMode(long id) {
		final SeriesAuthorizer authorizationRule = makeSeriesAuthorizer();
		final Long sId = findSeriesIdByPermissionId(authorizationRule, id);
		checkSeriesPermission(sId, "update the permission of");
		Mode data = modeForm.bindFromRequest().get();
		authorizationRule.updateMode(id, data);
		setResponseLocationFromRequest();
		return noContent();
	}
}