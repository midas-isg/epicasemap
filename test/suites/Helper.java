package suites;

import static com.fasterxml.jackson.databind.node.JsonNodeType.ARRAY;
import static com.fasterxml.jackson.databind.node.JsonNodeType.NULL;
import static com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT;
import static org.fest.assertions.Assertions.assertThat;
import interactors.ConfRule;

import java.util.List;

import javax.persistence.EntityManager;

import play.db.jpa.JPA;
import play.libs.F.Function0;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import _imperfactcoverage.Detour;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import controllers.Factory;

public class Helper {
	public static WSResponse get(String url) {
		final long timeout = 1000000;
		final WSResponse response = WS.url(url).get().get(timeout);
		return response;
	}

	public static String readContext() {
		final ConfRule conf = Factory.makeConfRule();
		return conf.readString("application.context");
	}

	public static <T> T wrapTransaction(Function0<T> block) {
		return wrapNoThrowingCheckedExecption(() -> JPA.withTransaction(block));
	}

	public static <T> T wrapNoThrowingCheckedExecption(Function0<T> block) {
		return Detour.wrapNoThrowingCheckedExecption(block);
	}

	public static void assertNodeType(JsonNode node, JsonNodeType... expected) {
		assertThat(node).isNotNull();
		assertThat(node.getNodeType()).isIn((Object[]) expected);
	}

	public static void assertAreEqual(Object actual, Object expected) {
		assertThat(actual).isEqualTo(expected);
	}

	public static JsonNode testJsonResponseMin(String url, int min) {
		return testJsonResponseClosedInterval(url, min, null);
	}

	public static JsonNode testJsonResponseLimit(String url, int limit) {
		return testJsonResponseClosedInterval(url, limit, limit);
	}

	private static JsonNode testJsonResponseClosedInterval(String url, int min,
			Integer max) {
		final WSResponse response = Helper.get(url);
		final JsonNode root = response.asJson();
		assertNodeType(root, OBJECT);
		final JsonNode results = root.get("results");
		assertNodeType(results, ARRAY);
		final int size = results.size();
		assertThat(size).isGreaterThanOrEqualTo(min);
		if (max != null)
			assertThat(size).isLessThanOrEqualTo(max);
		return root;
	}

	public static JsonNode testJsonResponse(String url) {
		final WSResponse response = Helper.get(url);
		final JsonNode root = response.asJson();
		assertNodeType(root, OBJECT);
		final JsonNode result = root.get("result");
		assertNodeType(result, OBJECT);
		return root;
	}

	public static void assertValueRange(JsonNode node, double max, double min) {
		final double val = node.asDouble();
		assertThat(val).isLessThanOrEqualTo(max);
		assertThat(val).isGreaterThanOrEqualTo(min);
	}

	public static <T> void assertArrayNode(JsonNode actuals, List<T> expected,
			Class<T> clazz) {
		assertNodeType(actuals, ARRAY);
		for (int i = 0; i < expected.size(); i++) {
			final Object actual = Json.fromJson(actuals.get(i), clazz);
			assertAreEqual(actual, expected.get(i));
		}
	}

	public static void assertTextNode(JsonNode actual, String expected) {
		if (expected == null)
			assertNodeType(actual, NULL);
		else
			assertAreEqual(actual.asText(), expected);
	}

	public static <T> void detachThenAssertWithDatabase(long id, T expected) {
		final EntityManager em = JPA.em();
		em.detach(expected);

		@SuppressWarnings("unchecked")
		final Class<T> clazz = (Class<T>) expected.getClass();
		final T found = em.find(clazz, id);
		assertAreEqual(found, expected);
	}
}
