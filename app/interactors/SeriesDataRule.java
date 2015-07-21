package interactors;

import gateways.database.SeriesDataDao;

import java.util.List;

import models.entities.SeriesData;
import models.entities.filters.Filter;

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

}
