package interactors;

import gateways.database.LocationDao;

import java.util.List;

import models.entities.Location;
import models.entities.filters.Filter;

public class LocationRule extends CrudRule<Location> {
	private LocationDao dao;

	public LocationRule(LocationDao dao) {
		this.dao = dao;
	}

	public List<Location> query(Filter filter) {
		return dao.query(filter);
	}

	@Override
	protected LocationDao getDao() {
		return dao;
	}
}
