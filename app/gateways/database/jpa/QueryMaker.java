package gateways.database.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.filters.Filter;
import models.filters.TimestampRange;

class QueryMaker<T> {
	private EntityManager em;
	private Class<T> clazz;
	
	private CriteriaBuilder criteriaBuilder;
	private CriteriaQuery<T> criteriaQuery;
	private Root<T> root;


	public QueryMaker(EntityManager em, Class<T> clazz) {
		this.em = em;
		this.clazz = clazz;
	}
	
	public TypedQuery<T> make(Filter filter) {
		init();
		if (filter == null)
			return em.createQuery(criteriaQuery);
		if (filter instanceof TimestampRange)
			return makeTimestampRangeQuery((TimestampRange) filter); 
		return makeQuery(filter, () -> makePredicates(filter));
	}
	
	private void init(){
		criteriaBuilder = em.getCriteriaBuilder();
		criteriaQuery = criteriaBuilder.createQuery(clazz);
		root = criteriaQuery.from(clazz);
	}

	private TypedQuery<T> makeTimestampRangeQuery(TimestampRange filter) {
		return makeQuery(filter, () -> makeTimestampPredicates(filter));
	}
	
	private TypedQuery<T> makeQuery(Filter filter, Supplier<List<Predicate>> supplier) {
		List<Predicate> predicates = supplier.get();
		List<Order> orders = makeOrders(filter);
		return makeQuery(toArraySkippingNull(predicates), orders);
	}

	private List<Order> makeOrders(Filter filter) {
		return new OrderMaker(filter.getOrders()).make();
	}

	private TypedQuery<T> makeQuery(Predicate[] predicates, List<Order> orders) {
		criteriaQuery.where(predicates);
		criteriaQuery.orderBy(orders);
		return em.createQuery(criteriaQuery);
	}

	private Predicate[] toArraySkippingNull(List<Predicate> list) {
		return list.stream()
				.filter(it -> it != null)
				.toArray(size -> new Predicate[size]);
	}

	private List<Predicate> makeTimestampPredicates(TimestampRange filter) {
		return makePredicates(makePredicates(filter), filter);
	}

	private List<Predicate> makePredicates(List<Predicate> predicates, TimestampRange filter) {
		final String attributeName = filter.getTimestampAttribute();
		final Path<Date> date = root.<Date>get(attributeName);
		predicates.add(startInclusive(filter, date));
		predicates.add(endExclusive(filter, date));
		return predicates;
	}

	private Predicate startInclusive(TimestampRange filter, Path<Date> date) {
		Date date1 = filter.getStartTimestampInclusive();
		if (date1 == null)
			return null;
		return criteriaBuilder.greaterThanOrEqualTo(date, date1);
	}

	private Predicate endExclusive(TimestampRange filter, Path<Date> date) {
		Date date2 = filter.getEndTimestampExclusive();
		if (date2 == null)
			return null;
		return criteriaBuilder.lessThan(date, date2);
	}

	private List<Predicate> makePredicates(Filter filter) {
		List<Predicate> predicates = new ArrayList<>();
		addEqPredicates(predicates, filter.getEqualities());
	 	addDisjunctiveEqPredicates(predicates, filter.getDisjunctiveEqualities());
		addInPredicates(predicates, filter.getInOperators());
		return predicates;
	}

	private void addEqPredicates(List<Predicate> predicates, Map<String, Object> eqMap) {
		for (Entry<String, Object> pair : eqMap.entrySet()){
			final Object value = pair.getValue();
			if (value == null)
				continue;
			final Path<?> path = getPath(pair.getKey());
			final Predicate p = criteriaBuilder.equal(path, value);
			predicates.add(p);
		}
	}

	private Path<?> getPath(final String attributeName) {
		final String[] tokens = attributeName.split("\\.");
		Path<?> path = root;
		for (String token : tokens)
			path = path.get(token);
		return path;
	}

	private void addDisjunctiveEqPredicates(List<Predicate> predicates,	Map<String, Object> disjunctMap) {
		List<Predicate> disjuncts = new ArrayList<>(); 
		addEqPredicates(disjuncts, disjunctMap);
		predicates.add(or(disjuncts));
	}

	private Predicate or(List<Predicate> disjuncts) {
		if (disjuncts.isEmpty())
			return null;
		return criteriaBuilder.or(toArraySkippingNull(disjuncts));
	}
	
	private void addInPredicates(List<Predicate> predicates, Map<String, List<?>> key2List) {
		for (Entry<String, List<?>> pair : key2List.entrySet()){
			final List<?> list = pair.getValue();
			if (list == null)
				continue;
			if (list.isEmpty()){
				predicates.add(contradiction());
			} else {
				final Path<?> path = getPath(pair.getKey());
				final Predicate predicate = path.in(list);
				predicates.add(predicate);
			}
		}
	}

	private Predicate contradiction() {
		return criteriaBuilder.disjunction();
	}

	private class OrderMaker {
		private LinkedHashMap<String, Filter.Order> orderMap;
		
		private OrderMaker(LinkedHashMap<String, Filter.Order> orderMap){
			this.orderMap = orderMap; 
		}
		
		private List<Order> make() {
			List<Order> orders = new ArrayList<>(orderMap.size());
			for (Entry<String, Filter.Order> pair : orderMap.entrySet()){
				final Path<?> path = getPath(pair.getKey());
				final Order order = toOrder(path, pair.getValue());
				orders.add(order);
			}
			return orders;
		}

		private Order toOrder(Path<?> path, Filter.Order o) {
			return o == Filter.Order.DESC 
					? criteriaBuilder.desc(path) 
					: criteriaBuilder.asc(path);
		}
	}
}