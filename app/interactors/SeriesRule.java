package interactors;

import gateways.database.SeriesDao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.entities.Account;
import models.entities.Coordinate;
import models.entities.Series;
import models.entities.SeriesDataUrl;
import models.entities.SeriesPermission;
import models.entities.Visualization;
import models.exceptions.ConstraintViolation;
import models.filters.CoordinateFilter;
import models.filters.MetaFilter;
import models.filters.Restriction;
import models.view.SeriesInput;

public class SeriesRule extends CrudRule<Series> {
	private SeriesDao dao;
	private CoordinateRule coordinateRule;
	private SeriesDataRule seriesDataRule;

	private SeriesDataUrlRule seriesDataUrlRule;

	private SeriesAuthorizer seriesAuthorizer;
	private VizRule vizRule; 
	private AccountRule accountRule;
	
	public SeriesRule(SeriesDao dao) {
		this.dao = dao;
	}

	public void setAccountRule(AccountRule rule) {
		accountRule = rule;
	}

	public void setSeriesAuthorizer(SeriesAuthorizer seriesAuthorizer) {
		this.seriesAuthorizer = seriesAuthorizer;
	}

	public List<Series> query(MetaFilter filter) {
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
		deleteSeriesDataUrl(id);
		deleteAllSeriesPermissions(id);
		super.delete(id);
	}

	private int deleteSeriesDataUrl(long id) {
		List<SeriesDataUrl> result = seriesDataUrlRule.query(id);
		for (SeriesDataUrl seriesDataUrl : result) {
			seriesDataUrlRule.delete(seriesDataUrl.getId());
		}
		return result.size();
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
	
	private int deleteAllSeriesPermissions(long seriesId) {
		Restriction r = new Restriction(null, null, seriesId, null);
		final List<SeriesPermission> permissions = seriesAuthorizer.findPermissions(r);
		for (SeriesPermission permission : permissions) {
			seriesAuthorizer.delete(permission.getId());
		}
		return permissions.size();
	}
	
	private CoordinateFilter buildCoordinateFilter(long seriesId) {
		CoordinateFilter filter = new CoordinateFilter();
		filter.setSeriesId(seriesId);
		filter.setOffset(0);
		return filter;
	}

	public void setSeriesDataUrlRule(SeriesDataUrlRule seriesDataUrlRule) {
		this.seriesDataUrlRule = seriesDataUrlRule;
	}

	public long createFromInput(SeriesInput input) {
		return create(toSeries(input));
	}

	private Series toSeries(SeriesInput input) {
		if (input == null)
			return null;
		Series model = new Series();
		copyMetadata(model, input);
		model.setOwner(readAccount(input.getOwnerId()));
		return model;
	}

	private Account readAccount(Long id) {
		if (id == null)
			return null;
		return accountRule.read(id);
	}

	public void updateFromInput(long id, SeriesInput input) {
		update(id, toSeries(input));
	}
}
