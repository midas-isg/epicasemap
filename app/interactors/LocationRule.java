package interactors;

import gateways.database.LocationDao;

import java.util.List;

import models.entities.Location;
import models.entities.LocationFilter;

public class LocationRule extends CrudRule<Location> {
	private LocationDao dao;

	public LocationRule(LocationDao dao) {
		this.dao = dao;
	}

	public List<Location> query(LocationFilter filter) {
		return dao.query(filter);
	}

	@Override
	protected LocationDao getDao() {
		return dao;
	}
}
