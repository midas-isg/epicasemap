package controllers;

import interactors.CoordinateRule;

import java.util.Date;
import java.util.List;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import models.entities.Coordinate;
import models.entities.CoordinateFilter;

import org.joda.time.DateTime;

import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Api(value = "/series/time-coordinate", description = "Endpoint for Time-Coordinate Series")
public class ApiTimeCoordinateSeries extends Controller {
	@ApiOperation(
			httpMethod = "GET", 
			nickname = "find", 
			value = "Returns Time-Coordinate Series by Series ID"
	)
	@ApiResponses(value = {
			@ApiResponse(code = OK, message = "Successful retrieval"),
	})
	@Transactional
	public static Result get(
			@ApiParam(value = "ID of the Series", required = true) 
			@PathParam("id") 
			Long seriesId,
			@ApiParam(value = "inclusive start timestamp in ISO format. "
					+ "e.g. 20015-01-01, 20015-01-01T13:59-05:00. "
					+ "For more details, see http://www.w3.org/TR/NOTE-datetime",
					required = false) 
			@QueryParam("startInclusive") 
			String startInclusive, 
			@ApiParam(value = "exclusive end timestamp in ISO format. "
					+ "For more details, see startInclusive", 
					required = false) 
			@QueryParam("endExclusive") 
			String endExclusive, 
			@ApiParam(value = "the number of resturned elements as pagination", 
					required = false) 
			@QueryParam("limit") 
			Integer limit, 
			@ApiParam(value = "the offset of resturned elements as pagination", 
					required = false) 
			@QueryParam("offset") 
			int offset
	) {
		CoordinateRule rule = Factory.makeCoordinateRule(JPA.em());
		CoordinateFilter filter = buildCoordinateFilter(seriesId, startInclusive, 
				endExclusive, limit, offset);
        List<Coordinate> results = rule.query(filter);
		return ResponseWrapper.okAsWrappedJsonArray(results, filter);
	}

	private static CoordinateFilter buildCoordinateFilter(
			Long seriesId,
			String startInclusive, 
			String endExclusive, 
			Integer limit, 
			int offset
	) {
		CoordinateFilter filter = new CoordinateFilter();
		filter.setSeriesId(seriesId);
		filter.setStartTimestampInclusive(toDate(startInclusive));
		filter.setEndTimestampExclusive(toDate(endExclusive));
		filter.setLimit(limit);
		filter.setOffset(offset);
		return filter;
	}
	
	private static Date toDate(String text) {
		if (text == null)
			return null;
		
		return DateTime.parse(text).toDate();
	}
}
