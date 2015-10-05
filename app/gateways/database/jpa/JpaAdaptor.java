package gateways.database.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import models.entities.Entity;
import models.exceptions.NotFound;
import models.filters.Filter;
import models.filters.Pagination;

public class JpaAdaptor {
	private EntityManager em;
	
	public JpaAdaptor(EntityManager em){
		this.em = em;
	}

	public <T> List<T> query(Class<T> clazz, Filter filter) {
		QueryMaker<T> querySupplier = new QueryMaker<T>(em, clazz);
		TypedQuery<T> query = querySupplier.make(filter);
		return getResults(query, filter);
	}

	private <T> List<T> getResults(TypedQuery<T> query, Filter filter) {
		if (filter != null && filter instanceof Pagination)
			paginate(query, (Pagination)filter);
		return query.getResultList();
	}
	
	private <T> void paginate(TypedQuery<T> query, Pagination filter) {
		Integer limit = filter.getLimit();
		if (limit != null)
			query.setMaxResults(limit);
		Integer offset = filter.getOffset();
		if (offset != null) 
			query.setFirstResult(offset);
	}

	public <T> T create(T data) {
		em.persist(data);
		return data;
	}

	public <T> T read(Class<T> clazz, long id) {
		return find(clazz, id);
	}

	public <T extends Entity> T update(Class<T> clazz, long id, T data) {
		T original = find(clazz, id);
		data.setId(original.getId());
		em.merge(data);
		return data;
	}

	public <T> void delete(Class<T> clazz, long id) {
		T data = find(clazz, id);
		em.remove(data);
	}

	private <T> T find(Class<T> clazz, long id) {
		final T t = em.find(clazz, id);
		if (t == null){
			final String message = clazz.getSimpleName() 
					+ " with ID = " + id + " was not found!";
			throw new NotFound(message);
		}
		return t;
	}
}
