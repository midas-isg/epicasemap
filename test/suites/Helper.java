package suites;

import static com.fasterxml.jackson.databind.node.JsonNodeType.ARRAY;
import static com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT;
import static org.fest.assertions.Assertions.assertThat;
import interactors.ConfRule;
import play.db.jpa.JPA;
import play.libs.F.Callback0;
import play.libs.F.Function0;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import _imperfactcoverage.Detour;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import controllers.Factory;

public class Helper {
	public static WSResponse get(String url) {
		long timeout = 1000000;
		WSResponse response = WS.url(url).get().get(timeout);
		return response;
	}

	public static String readContext() {
		ConfRule conf = Factory.makeConfRule();
		return conf.readString("application.context");
	}

	public static void wrapTransaction(Callback0 block) {
		Function0<Void> f = () -> {
			block.invoke();
			return null;
		};
		
		wrapNoThrowingCheckedExecption(() -> JPA.withTransaction(f));
	}

	public static <T> T wrapNoThrowingCheckedExecption(Function0<T> block) {
		return Detour.wrapNoThrowingCheckedExecption(block);
	}
	
	public static void assertNodeType(JsonNode node, JsonNodeType... expected) {
		assertThat(node.getNodeType()).isIn((Object[])expected);
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
	
	private static JsonNode testJsonResponseClosedInterval(String url, int min, Integer max) {
		WSResponse response = Helper.get(url);
		JsonNode root = response.asJson();
		assertNodeType(root, OBJECT);
		JsonNode results = root.get("results");
		assertNodeType(results, ARRAY);
		int size = results.size();
		assertThat(size).isGreaterThanOrEqualTo(min);
		if (max != null)
			assertThat(size).isLessThanOrEqualTo(max);
		return root;
	}
	
	public static JsonNode testJsonResponse(String url) {
		WSResponse response = Helper.get(url);
		JsonNode root = response.asJson();
		assertNodeType(root, OBJECT);
		JsonNode result = root.get("result");
		assertNodeType(result, OBJECT);
		return root;
	}
}
