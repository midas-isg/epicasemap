package interactors;

import gateways.database.SeriesDao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.entities.Coordinate;
import models.entities.Series;
import models.entities.Visualization;
import models.exceptions.ConstraintViolation;
import models.filters.CoordinateFilter;
import models.filters.SeriesFilter;

public class SeriesRule extends CrudRule<Series> {
	private SeriesDao dao; 
	private CoordinateRule coordinateRule;
	private SeriesDataRule seriesDataRule;
	private VizRule vizRule; 

	public SeriesRule(SeriesDao dao) {
		this.dao = dao;
	}

	public List<Series> query(SeriesFilter filter) {
		return dao.query(filter);
	}

	@Override
	protected SeriesDao getDao() {
		return dao;
	}
	
	@Override
	public void delete(long id) {
		validateConstrains(id);
		deleteAllSeriesData(id);
		super.delete(id);
	}

	private void validateConstrains(long id) {
		final List<Visualization> vizs = vizRule.query(null);
		Set<Long> vizIds = new HashSet<>();
		for (Visualization viz : vizs){
			for (Series series : viz.getAllSeries()){
				if (series.getId().equals(id)){
					vizIds.add(viz.getId());
				}
			}
		}
		if (! vizIds.isEmpty()){
			final String message = "The Series could not be deleted "
					+ "as it is referred by the Visualization(s) "
					+ "with the following IDs " + vizIds;
			throw new ConstraintViolation(message);
		}
	}

	public void setCoordinateRule(CoordinateRule coordinateRule) {
		this.coordinateRule = coordinateRule;
	}

	public void setSeriesDataRule(SeriesDataRule seriesDataRule) {
		this.seriesDataRule = seriesDataRule;
	}

	public void setVizRule(VizRule vizRule) {
		this.vizRule = vizRule;
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
