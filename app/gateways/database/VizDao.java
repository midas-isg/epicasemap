package gateways.database;

import gateways.database.jpa.DataAccessObject;
import gateways.database.jpa.JpaAdaptor;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import models.entities.Visualization;
import models.filters.MetaFilter;

public class VizDao extends DataAccessObject<Visualization> {
	public VizDao(EntityManager em){
		this(new JpaAdaptor(em));
	}

	private VizDao(JpaAdaptor helper){
		super(Visualization.class, helper);
	}
	
	public List<Visualization> query(MetaFilter filter) {
		return queryWithMetaFilter(filter);
	}

	private List<Visualization> queryWithMetaFilter(MetaFilter filter) {
		if (filter != null){
			final List<Long> ids = filter.getIds();
			if (ids != null){
				final Map<String, List<?>> inOperators = filter.getInOperators();
				inOperators.put("id", ids);
			}
			
			final Long ownerId = filter.getOwnerId();
			if (ownerId != null){
				final Map<String, Object> eqMap = filter.getEqualities();
				eqMap.put("owner.id", ownerId);
			}
		}
		return super.query(filter);
	}
}