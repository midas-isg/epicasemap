package interactors;

import gateways.database.SeriesDao;

import java.util.List;

import models.entities.Series;
import models.entities.filters.Filter;

public class SeriesRule extends CrudRule<Series> {
	private SeriesDao dao;

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
}
