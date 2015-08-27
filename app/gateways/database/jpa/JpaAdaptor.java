package gateways.database.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.entities.Entity;
import models.filters.Filter;
import models.filters.Pagination;
import models.filters.TimestampRange;

public class JpaAdaptor {
	private EntityManager em;
	
	public JpaAdaptor(EntityManager em){
		this.em = em;
	}

	public <T> List<T> query(Class<T> clazz, Filter filter) {
		TypedQuery<T> query = buildQuery(clazz, filter);
		return getResults(query, filter);
	}

	private <T> List<T> getResults(TypedQuery<T> query, Filter filter) {
		if (filter != null && filter instanceof Pagination)
			filter(query, (Pagination)filter);
		return query.getResultList();
	}
	
	private <T> void filter(TypedQuery<T> query, Pagination filter) {
		Integer limit = filter.getLimit();
		if (limit != null)
			query.setMaxResults(limit);
		Integer offset = filter.getOffset();
		if (offset != null) 
			query.setFirstResult(offset);
	}

	private <T> TypedQuery<T> buildQuery(Class<T> clazz) {
		CriteriaQuery<T> criteriaQuery = createCriteriaQueryFrom(clazz);
		return em.createQuery(criteriaQuery);
	}
	
	private <T> CriteriaQuery<T> createCriteriaQueryFrom(Class<T> clazz){
		CriteriaQuery<T> criteriaQuery =  createCriteriaQuery(clazz);
		criteriaQuery.from(clazz);
		return criteriaQuery;
	}
	
	private <T> TypedQuery<T> buildQuery(Class<T> clazz, Filter filter) {
		if (filter == null)
			return buildQuery(clazz);
		if (filter instanceof TimestampRange)
			return buildQuery(clazz, (TimestampRange) filter); 
		return buildQuery_(clazz, filter);

	}
	
	private <T> TypedQuery<T> buildQuery_(Class<T> clazz, Filter filter) {
		CriteriaQuery<T> criteriaQuery = createCriteriaQuery(clazz);
		Root<T> root = criteriaQuery.from(clazz);
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		List<Predicate> predicates = new ArrayList<>();
	
		Map<String, Object> map = filter.getEqualities();
		for (Entry<String, Object> pair : map.entrySet()){
			final Object value = pair.getValue();
			if (value == null)
				continue;
			final Path<?> path = root.get(pair.getKey());
			Predicate p = criteriaBuilder.equal(path, value);
			predicates.add(p);
		}
		
		Map<String, List<?>> key2List = filter.getInOperators();
		for (Entry<String, List<?>> pair : key2List.entrySet()){
			final List<?> value = pair.getValue();
			if (value == null)
				continue;
			Path<String> exp = root.get(pair.getKey());
			Predicate predicate = exp.in(value);
			predicates.add(predicate);
		}

		criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
		final LinkedHashMap<String, Filter.Order> orders = filter.getOrder();
		for (Entry<String, Filter.Order> pair : orders.entrySet()){
			final Path<Object> path = root.get(pair.getKey());
			final Order order = toOrder(criteriaBuilder, path, pair.getValue());
			criteriaQuery.orderBy(order);
		}
		TypedQuery<T> query = em.createQuery(criteriaQuery);
		return query;
	}
	
	private <T> TypedQuery<T> buildQuery(Class<T> clazz, TimestampRange filter) {
		CriteriaQuery<T> criteriaQuery = createCriteriaQuery(clazz);
		Root<T> root = criteriaQuery.from(clazz);
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		final String attributeName = filter.getTimestampAttribute();
		final Path<Date> date = root.<Date>get(attributeName);
		List<Predicate> predicates = new ArrayList<>();
		Date date1 = filter.getStartTimestampInclusive();
		if (date1 != null){
			Predicate p = criteriaBuilder.greaterThanOrEqualTo(date, date1);
			predicates.add(p);
		}
		
		Date date2 = filter.getEndTimestampExclusive();
		if (date2 != null){
			Predicate p = criteriaBuilder.lessThan(date, date2);
			predicates.add(p);
		}
		
		Map<String, Object> map = filter.getEqualities();
		for (Entry<String, Object> pair : map.entrySet()){
			final Object value = pair.getValue();
			if (value == null)
				continue;
			final Path<?> path = root.get(pair.getKey());
			Predicate p = criteriaBuilder.equal(path, value);
			predicates.add(p);
		}
		
		criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
		final LinkedHashMap<String, Filter.Order> orders = filter.getOrder();
		for (Entry<String, Filter.Order> pair : orders.entrySet()){
			final Path<Object> path = root.get(pair.getKey());
			final Order order = toOrder(criteriaBuilder, path, pair.getValue());
			criteriaQuery.orderBy(order);
		}
		TypedQuery<T> query = em.createQuery(criteriaQuery);
		return query;
	}

	private Order toOrder(CriteriaBuilder cb, Path<?> path, Filter.Order o) {
		return (o == Filter.Order.DESC) ? cb.desc(path) : cb.asc(path);
	}

	private <T> CriteriaQuery<T> createCriteriaQuery(Class<T> clazz) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(clazz);
		return criteriaQuery;
	}
	
	public <T> T create(T data) {
		em.persist(data);
		return data;
	}

	public <T> T read(Class<T> clazz, long id) {
		return em.find(clazz, id);
	}

	public <T> void delete(Class<T> clazz, long id) {
		T data = em.find(clazz, id);
		em.remove(data);
	}

	public <T extends Entity> T update(Class<T> clazz, long id, T data) {
		T original = em.find(clazz, id);
		data.setId(original.getId());
		em.merge(data);
		return data;
	}
}
