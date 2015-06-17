package _imperfactcoverage;

import static org.fest.assertions.Assertions.assertThat;
import integrations.app.controllers.TestCoordinateTime;

import org.junit.Assert;

import play.libs.F.Callback0;

public class Detour {
	private Detour(){
	}

	public static Callback0 testLimitWithNegative(TestCoordinateTime that) {
		return () -> {
			try {
				that.testLimit(-1, 0);
				Assert.fail();
			} catch (Exception e){
				assertThat(e).isInstanceOf(Exception.class);
			}
		};
	}
}
