package interactors;

import gateways.database.jpa.DataAccessObject;
import models.entities.Entity;
import models.entities.MetaData;

public abstract class CrudRule<T extends Entity> {
	protected abstract DataAccessObject<T> getDao();

	public long create(T data) {
		return getDao().create(data).getId();
	}

	public T read(long id) {
		return getDao().read(id);
	}

	public void update(long id, T data) {
		getDao().update(id, data);
	}

	public void delete(long id) {
		getDao().delete(id);
	}
	
	protected void copyMetadata(MetaData dest, MetaData src) {
		dest.setId(src.getId());
		dest.setCreator(src.getCreator());
		dest.setDescription(src.getDescription());
		dest.setIsVersionOf(src.getIsVersionOf());
		dest.setLicense(src.getLicense());
		dest.setPublisher(src.getPublisher());
		dest.setTitle(src.getTitle());
		dest.setVersion(src.getVersion());
	}
}