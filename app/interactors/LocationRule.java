package interactors;

import gateways.database.LocationDao;
import gateways.webservice.AlsApiHelper;

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

	public Location getLocationByAlsId(Long alsId) {
		Location location = queryByAlsId(alsId);
		if (location == null)
			location = getLocationFromAls(alsId);
		return location;
	}

	public Location getLocationByCoordinate(Double latitude, Double longitude) {
		return queryByCoordinate(latitude,longitude);

	}

	private Location queryByCoordinate(Double latitude, Double longitude) {
		LocationFilter filter = buildLocationFilter(latitude, longitude);
		return findOne(filter);

	}

	private Location queryByAlsId(Long alsId) {
		LocationFilter filter = buildLocationFilter(alsId);
		return findOne(filter);
	}

	private Location getLocationFromAls(Long alsId) {
		AlsApiHelper helper = new AlsApiHelper();
		return helper.getLocationFromAls(alsId);
	}

	private Location findOne(LocationFilter filter) {
		List<Location> LocList = query(filter);
		if (isNotEmpty(LocList))
			return LocList.get(0);
		else
			return null;
	}

	private LocationFilter buildLocationFilter(Double latitude, Double longitude) {
		LocationFilter filter = new LocationFilter();
		filter.setLatitude(latitude);
		filter.setLongitude(longitude);
		return filter;
	}

	private LocationFilter buildLocationFilter(Long alsId) {
		LocationFilter filter = new LocationFilter();
		filter.setAlsId(alsId);
		return filter;
	}

	private boolean isNotEmpty(List<Location> list) {
		return !(list.isEmpty());
	}

}
