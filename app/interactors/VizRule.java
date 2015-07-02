package interactors;

import gateways.database.VizDao;
import models.entities.Viz;
import models.views.VizInput;

public class VizRule extends CrudRule<Viz> {
	private VizDao dao;
	
	public VizRule(VizDao dao) {
		super();
		this.dao = dao;
	}
	
	public long create(VizInput input) {
		final Viz data = input.toViz();
		return super.create(data);
	}

	@Override
	protected VizDao getDao() {
		return dao;
	}
}
