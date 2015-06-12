package gateways.database.jpa;

import java.util.List;

public class DataAccessObject<T> {
	private Class<T> clazz;
	private JpaAdaptor adaptor;
	
	public DataAccessObject( Class<T> clazz, JpaAdaptor adaptor){
		this.clazz = clazz;
		this.adaptor = adaptor;
	}

	public List<T> query(Integer limit, int offset) {
		return adaptor.query(clazz, limit, offset);		
	}
	
	public List<T> findAll() {
		return query(null, 0);		
	}
	
	public T find(long id) {
		return adaptor.find(clazz, id);		
	}
}
