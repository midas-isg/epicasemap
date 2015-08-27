package interactors;

import gateways.database.SeriesDataDao;

import java.util.Date;
import java.util.List;

import models.entities.Location;
import models.entities.Series;
import models.entities.SeriesData;
import models.filters.Filter;

public class SeriesDataRule extends CrudRule<SeriesData> {
	private SeriesDataDao dao;

	public SeriesDataRule(SeriesDataDao dao) {
		this.dao = dao;
	}

	public List<SeriesData> query(Filter filter) {
		return dao.query(filter);
	}

	@Override
	protected SeriesDataDao getDao() {
		return dao;
	}

	public SeriesData createNew(Series series, Location location, Date time,
			Double value) {

		final SeriesData seriesData = new SeriesData();
		seriesData.setLocation(location);
		seriesData.setSeries(series);
		seriesData.setTimestamp(time);
		seriesData.setValue(value);
		create(seriesData);
		return seriesData;
	}

}
