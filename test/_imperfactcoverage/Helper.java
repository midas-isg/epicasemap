package _imperfactcoverage;

import interactors.ConfRule;
import play.db.jpa.JPA;
import play.libs.F.Callback;
import play.libs.F.Function0;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import controllers.Factory;

public class Helper {
	private Helper(){
	}

	public static WSResponse get(String url) {
		long timeout = 1000000;
		return WS.url(url).get().get(timeout);
	}

	public static String readContext() {
		ConfRule conf = Factory.makeConfRule();
		return conf.readString("application.context");
	}

	public static <T> T wrapTransaction(Function0<T> block) {
		return wrapNoThrowingCheckedExecption(() -> JPA.withTransaction(block));
	}

	public static <T> T wrapNoThrowingCheckedExecption(Function0<T> block) {
		try {
			return block.apply();
		} catch (RuntimeException|Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T wrapTry(Function0<T> tryBlock, Callback<T> finallyBlock) {
		T t = null;
		try {
			t = Helper.wrapNoThrowingCheckedExecption(tryBlock);
			return t;
		} finally {
			Helper.wrapNoCheckedExecption(finallyBlock, t);
		}
	}

	public static <T> void wrapNoCheckedExecption(Callback<T> block, T t) {
		try {
			block.invoke(t);
		} catch (RuntimeException|Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
