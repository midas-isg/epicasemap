package integrations.app;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import gateways.configuration.AppKey;
import impl.Factory;
import interactors.ConfRule;

import org.junit.Test;

public class TestConf {
    @Test
    public void appNameAndVersionAreNotOverridden() {
        Runnable block = () -> {
        	ConfRule conf = Factory.makeConfRule();
    		String expectedName = "play_java_base";
			String keyName = AppKey.NAME.key();
			assertThat(conf.readString(keyName)).isEqualTo(expectedName);
			
			for (AppKey key : AppKey.values()){
				String value = conf.readString(key.key());
				assertThat(value).as("Conf key=" + key.key()).isNotEmpty();
			}
        };
		running(fakeApplication(), block);
    }
}
