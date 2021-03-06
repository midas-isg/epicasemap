package gateways.database;

import gateways.database.jpa.DataAccessObject;
import models.entities.SeriesTopology;
import models.filters.GenericFilter;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

public class SeriesTopologyDao extends DataAccessObject<SeriesTopology> {
	public SeriesTopologyDao(EntityManager em){
		super(SeriesTopology.class, em);
	}

    public SeriesTopology readBySeriesId(long id) {
        GenericFilter filter = new GenericFilter();
        Map<String, Object> equalityMap = filter.getEqualities();
        equalityMap.put("seriesId", id);

        final List<SeriesTopology> list = super.query(filter);
        if (list.isEmpty())
            return null;
        return list.get(0);
    }
}
