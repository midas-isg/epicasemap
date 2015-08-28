package gateways.database.jpa;

import java.util.List;

import models.entities.Entity;
import models.filters.Filter;

public class DataAccessObject<T extends Entity> {
	private Class<T> clazz;
	private JpaAdaptor adaptor;
	
	public DataAccessObject(Class<T> clazz, JpaAdaptor adaptor){
		this.clazz = clazz;
		this.adaptor = adaptor;
	}

	public List<T> query(Filter filter){
		return adaptor.query(clazz, filter);
	}
	
	public List<T> findAll() {
		return query(null);		
	}
	public T create(T data){
		return adaptor.create(data);
	}
	public T read(long id) {
		return adaptor.read(clazz, id);		
	}
	
	public T update(long id, T data) {
		return adaptor.update(clazz, id, data);
	}

	public void delete(long id) {
		adaptor.delete(clazz, id);
	}
}
