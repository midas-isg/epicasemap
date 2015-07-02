package interactors;

import gateways.database.jpa.DataAccessObject;
import models.entities.Entity;

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
}