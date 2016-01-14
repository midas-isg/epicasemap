package controllers;

import javax.ws.rs.PathParam;

import models.exceptions.Unauthorized;
import play.db.jpa.Transactional;
import play.mvc.Result;

import com.wordnik.swagger.annotations.ApiParam;

import controllers.security.AuthorizationKit;
import controllers.security.Restricted;
import controllers.security.Restricted.Access;

public class APITychoSeries extends ApiSeries {
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
			
			return UploadSeries.uploadTychoViaUrl(id, url, overWrite);
	}
	
	private static void checkSeriesPermission(long id, String action) {
		if (! AuthorizationKit.isSeriesPermitted(id))
			throw new Unauthorized("Unauthorized to " + action + 
					" the Series with ID = " + id);
		
		return;
	}
}
