package gateways.database;

import gateways.database.jpa.DataAccessObject;
import models.entities.VizTopology;
import models.filters.GenericFilter;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

public class VizTopologyDao extends DataAccessObject<VizTopology> {
	public VizTopologyDao(EntityManager em){
		super(VizTopology.class, em);
	}

    public VizTopology readByVizId(long id) {
        GenericFilter filter = new GenericFilter();
        Map<String, Object> equalityMap = filter.getEqualities();
        equalityMap.put("vizId", id);

        final List<VizTopology> list = super.query(filter);
        if (list.isEmpty())
            return null;
        return list.get(0);
    }
}
