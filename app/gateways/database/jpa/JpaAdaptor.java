package gateways.database.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

public class JpaAdaptor {
	private EntityManager em;
	
	public JpaAdaptor(EntityManager em){
		this.em = em;
	}

	public <T> List<T> query(Class<T> clazz, int limit, int offset) {
		CriteriaQuery<T> criteriaQuery = createCriteriaQueryFrom(clazz);
		TypedQuery<T> query = em.createQuery(criteriaQuery);
		return query.setMaxResults(limit).setFirstResult(offset).getResultList();
	}

	public <T> List<T> findAll(Class<T> clazz) {
		CriteriaQuery<T> criteriaQuery = createCriteriaQueryFrom(clazz);
		TypedQuery<T> query = em.createQuery(criteriaQuery);
		return query.getResultList();
	}

	private <T> CriteriaQuery<T> createCriteriaQueryFrom(Class<T> clazz) {
		CriteriaQuery<T> criteriaQuery = createCriteriaQuery(clazz);
		criteriaQuery.from(clazz);
		return criteriaQuery;
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
