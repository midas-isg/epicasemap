package gateways.database.jpa;

import java.util.List;

import models.entities.filters.Filter;

public class DataAccessObject<T> {
	private Class<T> clazz;
	private JpaAdaptor adaptor;
	
	public DataAccessObject( Class<T> clazz, JpaAdaptor adaptor){
		this.clazz = clazz;
		this.adaptor = adaptor;
	}

	public List<T> query(Filter filter){
		return adaptor.query(clazz, filter);
	}
	
	public List<T> findAll() {
		return query(null);		
	}
	
	public T find(long id) {
		return adaptor.find(clazz, id);		
	}
}
