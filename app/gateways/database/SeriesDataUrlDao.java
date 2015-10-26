package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import models.entities.SeriesDataUrl;
import models.filters.SeriesDataUrlFilter;

public class SeriesDataUrlDao extends DataAccessObject<SeriesDataUrl> {
	public SeriesDataUrlDao(EntityManager em) {
		this(new JpaAdaptor(em));
	}

	private SeriesDataUrlDao(JpaAdaptor helper) {
		super(SeriesDataUrl.class, helper);
	}

	public List<SeriesDataUrl> queryDao(SeriesDataUrlFilter filter) {

		Map<String, Object> equalityMap = filter.getEqualities();
		//equalityMap.put("series", filter.getSeriesId());
		equalityMap.put("id", filter.getId());
		equalityMap.put("url", filter.getUrl());
		equalityMap.put("checksum", filter.getChecksum());
		return super.query(filter);
	}
}
