package interactors;

import gateways.database.LocationDao;
import gateways.webservice.AlsDao;

import java.util.Iterator;
import java.util.List;

import play.Logger;
import models.entities.Location;
import models.filters.LocationFilter;

public class LocationRule extends CrudRule<Location> {
	private LocationDao dao;
	private AlsDao alsDao;

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
		
		if (location == null) {
			location = getLocationFromAls(alsId);
		}
		
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
		return alsDao.getLocationFromAls(alsId);
	}

	private Location findOne(LocationFilter filter) {
		List<Location> LocList = query(filter);
		
		if (!LocList.isEmpty()) {
			return LocList.get(0);
		}
		
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
	
	public void setAlsDao(AlsDao alsDao){
		this.alsDao = alsDao;
	}

}
