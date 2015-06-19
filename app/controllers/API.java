package controllers;

import interactors.CoordinateRule;
import interactors.SeriesRule;

import java.util.Date;
import java.util.List;

import models.entities.Coordinate;
import models.entities.CoordinateFilter;
import models.entities.Series;
import models.entities.filters.Filter;

import org.joda.time.DateTime;

import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class API extends Controller {
	@Transactional
	public static Result getTimeCoordinateSeries(
			Long seriesId,
			String startInclusive, 
			String endExclusive, 
			Integer limit, 
			int offset
	) {
		CoordinateRule rule = Factory.makeCoordinateRule(JPA.em());
		CoordinateFilter filter = buildCoordinateFilter(seriesId, startInclusive, 
				endExclusive, limit, offset);
        List<Coordinate> results = rule.query(filter);
		Object response = ResponseWrapper.wrap(results, filter);
		return ok(Json.toJson(response));
	}

	@Transactional
	public static Result getSeries(){
		Filter filter = null;
		SeriesRule rule = Factory.makeSeriesRule(JPA.em());
		List<Series> results = rule.query(filter);;
		Object response = ResponseWrapper.wrap(results, filter);
		return ok(Json.toJson(response));
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
