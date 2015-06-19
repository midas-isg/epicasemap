package suites;

import _imperfactcoverage.Detour;
import interactors.ConfRule;
import play.db.jpa.JPA;
import play.libs.F.Callback0;
import play.libs.F.Function0;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import controllers.Factory;

public class Helper {
	private Helper(){
	}

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
}
