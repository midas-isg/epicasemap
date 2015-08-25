package interactors;

import gateways.database.LocationDao;
import gateways.webservice.AlsDAO;

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
	
	public Location getLocation(Long alsId) {
		Location location = getLocationByAlsId(alsId);
		if (location != null && location.getId() == null)
			create(location);
		return location;
	}
	
	public Location getLocation(Double lat, Double lon) {
		Location location = queryByCoordinate(lat, lon);
		if (location == null)
			location = createNew(lat, lon);
		return location;
	}

	Location createNew(Double lat, Double lon) {
		Location location = new Location();
		location.setLongitude(lon);
		location.setLatitude(lat);
		create(location);
		return location;
	}


	public Location getLocationByAlsId(Long alsId) {
		Location location = queryByAlsId(alsId);
		if (location == null)
			location = getLocationFromAls(alsId);
		return location;
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
		AlsDAO alsDao = new AlsDAO();
		return alsDao.getLocationFromAls(alsId);
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
