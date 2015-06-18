package gateways.database.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.entities.filters.DateRange;
import models.entities.filters.Filter;
import models.entities.filters.Pagination;

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
		int offset = filter.getOffset();
		query.setFirstResult(offset);
	}

	/*private <T> TypedQuery<T> buildQuery(Class<T> clazz) {
		CriteriaQuery<T> criteriaQuery = createCriteriaQuery(clazz);
		return em.createQuery(criteriaQuery);
	}*/
	
	private <T> TypedQuery<T> buildQuery(Class<T> clazz, Filter filter) {
		/*if (filter == null || ! (filter instanceof DateRange))
			return buildQuery(clazz);*/
		return buildQuery(clazz, (DateRange) filter); 
	}
	
	private <T> TypedQuery<T> buildQuery(Class<T> clazz, DateRange filter) {
		CriteriaQuery<T> criteriaQuery = createCriteriaQuery(clazz);
		Root<T> root = criteriaQuery.from(clazz);
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		final String attributeName = filter.getDateAttribute();
		final Path<Date> date = root.<Date>get(attributeName);
		List<Predicate> predicates = new ArrayList<>();
		Date date1 = filter.getStartInclusive();
		if (date1 != null){
			Predicate p = criteriaBuilder.greaterThanOrEqualTo(date, date1);
			predicates.add(p);
		}
		
		Date date2 = filter.getEndExclusiveDate();
		if (date2 != null){
			Predicate p = criteriaBuilder.lessThan(date, date2);
			predicates.add(p);
		}
		criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
		TypedQuery<T> query = em.createQuery(criteriaQuery);
		return query;
	}

	private <T> CriteriaQuery<T> createCriteriaQuery(Class<T> clazz) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(clazz);
		return criteriaQuery;
	}

	public <T> T find(Class<T> clazz, long id) {
		T result = em.find(clazz, id);
		return result;
	}
}
