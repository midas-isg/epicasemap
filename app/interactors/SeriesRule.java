package interactors;

import gateways.database.SeriesDao;

import java.util.List;

import models.entities.Coordinate;
import models.entities.Series;
import models.filters.CoordinateFilter;
import models.filters.Filter;

public class SeriesRule extends CrudRule<Series> {
	private SeriesDao dao; 
	private CoordinateRule coordinateRule;
	private SeriesDataRule seriesDataRule;

	public SeriesRule(SeriesDao dao) {
		this.dao = dao;
	}

	public List<Series> query(Filter filter) {
		return dao.query(filter);
	}

	@Override
	protected SeriesDao getDao() {
		return dao;
	}
	
	@Override
	public void delete(long id) {
		deleteAllSeriesData(id);
		super.delete(id);
	}

	public void setCoordinateRule(CoordinateRule coordinateRule) {
		this.coordinateRule = coordinateRule;
	}

	public void setSeriesDataRule(SeriesDataRule seriesDataRule) {
		this.seriesDataRule = seriesDataRule;
	}

	public int deleteAllSeriesData(long seriesId) {
		CoordinateFilter filter = buildCoordinateFilter(seriesId);
		List<Coordinate> seriesData = coordinateRule.query(filter);
		for (Coordinate data : seriesData) {
			seriesDataRule.delete(data.getId());
		}
		return seriesData.size();
	}
	
	private CoordinateFilter buildCoordinateFilter(long seriesId) {
		CoordinateFilter filter = new CoordinateFilter();
		filter.setSeriesId(seriesId);
		filter.setOffset(0);
		return filter;
	}
}
