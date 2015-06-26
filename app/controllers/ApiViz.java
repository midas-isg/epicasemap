package controllers;

import static controllers.ResponseWrapper.okAsWrappedJsonObject;

import javax.persistence.EntityManager;

import models.entities.Viz;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

public class ApiViz extends Controller {
	public static Form<Viz> vizForm = Form.form(Viz.class);
	
	@Transactional
	public static Result post() {
		Viz data = vizForm.bindFromRequest().get();
		long id = create(data);
		setResponseLocationFromRequest(id + "");
		return created();
	}

	public static long create(Viz data) {
		final EntityManager em = JPA.em();
		em.persist(data);
		return data.getId();
	}

	@Transactional
	public static Result read(long id) {
		Viz data = JPA.em().find(Viz.class, id);
		return okAsWrappedJsonObject(data, null);
	}
	
	@Transactional
	public static Result put(long id) {
		final EntityManager em = JPA.em();
		Viz original = em.find(Viz.class, id);
		Viz data = vizForm.bindFromRequest().get();
		data.setId(original.getId());
		em.merge(data);
		setResponseLocationFromRequest();
		return noContent();
	}
	
	@Transactional
	public static Result delete(long id) {
		final EntityManager em = JPA.em();
		Viz data = em.find(Viz.class, id);
		em.remove(data);
		setResponseLocationFromRequest();
		return noContent();
	}
	
	private static void setResponseLocationFromRequest(String... tails) {
		String url = makeUriFromRequest();
		for (String tail : tails)
			url += "/" + tail;
		response().setHeader(LOCATION, url);
	}

	private static String makeUriFromRequest() {
		Request request = Context.current().request();
		return request.getHeader(ORIGIN) + request.path();
	}
}