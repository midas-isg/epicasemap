package interactors;

import java.util.HashMap;
import java.util.Map;

import models.entities.Location;

public class LocationCacheRule_ {
	private static Map<Long, Location> cache = new HashMap<>();

	public static void update(Location location) {
		cache.put(location.getAlsId(), location);
	}

	public static Location read(Long id) {
		if (cache.containsKey(id))
			return cache.get(id);
		return null;
	}

}
